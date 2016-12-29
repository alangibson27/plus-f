package com.socialthingy.plusf.p2p

import java.net.InetSocketAddress
import java.nio.{ByteBuffer, ByteOrder}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{ActorRef, ActorSystem, FSM, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString
import akka.util.ByteString.UTF_8
import com.codahale.metrics.{Histogram, Meter, SlidingTimeWindowReservoir}
import com.socialthingy.plusf.p2p.Peer.{PeerConnection, State}
import net.jpountz.lz4.LZ4Factory

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Register {
  def apply(sessionId: String, forwardedPort: Int): Register = Register(sessionId, Some(forwardedPort))
}
case class Register(sessionId: String, forwardedPort: Option[Int] = None)
case object Cancel
case object Close
case object GetStatistics
case class Statistics(medianLatency: Int, outOfOrder: Int, medianSize: Int, m1Rate: Double)

trait Callbacks {
  def data(content: Any): Unit

  def discoveryTimeout(): Unit
  def discoveryCancelled(): Unit
  def waitingForPeer(): Unit
  def connectedToPeer(port: Int): Unit
  def discovering(): Unit
  def initialising(): Unit
  def closed(): Unit
}

object Peer {
  sealed trait State
  case object Uninitialised extends State
  case object Initialising extends State
  case object WaitingForPeer extends State
  case object Connected extends State
  case object Closing extends State

  val lz4 = LZ4Factory.fastestJavaInstance()

  case class PeerConnection(sessionId: Option[String], forwardedPort: Option[Int], socket: Option[ActorRef], peerAddress: Option[InetSocketAddress])
  val NoConnection = PeerConnection(None, None, None, None)

  def apply(bindAddress: InetSocketAddress,
            discoveryServiceAddress: InetSocketAddress,
            callbacks: Callbacks,
            serialiser: Serialiser,
            deserialiser: Deserialiser,
            timeout: FiniteDuration = 1 minute)(implicit system: ActorSystem): ActorRef = {
    val peer = system.actorOf(
      Props(new Peer(bindAddress, discoveryServiceAddress, callbacks, serialiser, deserialiser, timeout))
    )
    system.log.info("Binding peer {} to {}", peer.path, bindAddress)
    peer
  }
}

class Peer(bindAddress: InetSocketAddress,
           discoveryServiceAddress: InetSocketAddress,
           callbacks: Callbacks,
           serialiser: Serialiser,
           deserialiser: Deserialiser,
           timeout: FiniteDuration) extends FSM[State, PeerConnection] {
  import Peer._
  implicit val system = context.system
  implicit val byteOrder = ByteOrder.BIG_ENDIAN

  private def activate() = IO(Udp) ! Udp.Bind(self, bindAddress)

  val latencies = new Histogram(new SlidingTimeWindowReservoir(1, TimeUnit.MINUTES))
  val sizes = new Histogram(new SlidingTimeWindowReservoir(1, TimeUnit.MINUTES))
  val meter = new Meter()
  var outOfOrder = 0

  startWith(Uninitialised, NoConnection)

  when(Uninitialised) {
    case Event(Register(sessionId, fwdPort), _) =>
      callbacks.initialising()
      log.info(s"Initialising session $sessionId")

      activate()
      goto(Initialising) using PeerConnection(Some(sessionId), fwdPort, None, None)
  }

  when(Initialising) {
    case Event(Udp.Bound(localAddress), PeerConnection(Some(sessionId), fwdPort, _, _)) =>
      callbacks.discovering()
      log.info(s"Contacting discovery service at $discoveryServiceAddress")

      val socket = sender()
      val joinCommand = fwdPort match {
        case Some(p) => s"JOIN|$sessionId|$p"
        case None => s"JOIN|$sessionId"
      }
      socket ! Udp.Send(ByteString(joinCommand), discoveryServiceAddress)
      goto(WaitingForPeer) using PeerConnection(Some(sessionId), fwdPort, Some(socket), None)
  }

  when(WaitingForPeer, timeout) {
    case Event(Udp.Received(content, remote), PeerConnection(sessionId, fwdPort, Some(socket), _)) =>
      val result = content.decodeString(UTF_8)
      result.split('|').toList match {
        case "PEER" :: peerHost :: peerPort :: Nil =>
          callbacks.connectedToPeer(peerPort.toInt)
          log.info("Connected to peer at {}:{}", peerHost, peerPort)
          goto(Connected) using PeerConnection(sessionId, fwdPort, Some(socket), Some(new InetSocketAddress(peerHost, peerPort.toInt)))

        case "WAIT" :: Nil =>
          callbacks.waitingForPeer()
          log.info("Waiting for peer to join")
          stay()

        case _ =>
          log.info("Unrecognised message received, still waiting for peer")
          stay()
      }

    case Event(Cancel, PeerConnection(Some(sessionId), fwdPort, Some(socket), _)) =>
      callbacks.discoveryCancelled()
      log.info("Discovery cancelled, closing")

      socket ! Udp.Send(ByteString(s"CANCEL|$sessionId"), discoveryServiceAddress)
      socket ! Udp.Unbind
      goto(Closing) using NoConnection

    case Event(StateTimeout, PeerConnection(_, _, Some(socket), _)) =>
      callbacks.discoveryTimeout()
      log.info("Discovery timed out, closing")

      socket ! Udp.Unbind
      goto(Closing) using NoConnection
  }

  when(Connected) {
    case Event(Udp.Received(content, remote), _) =>
      Try {
        WrappedData(decompress(content), deserialiser)
      } match {
        case Success(data) =>
          latencies.update(System.currentTimeMillis - data.systemTime)
          sizes.update(content.size / 1024)
          meter.mark()
          if (data.timestamp > lastReceivedTimestamp) {
            lastReceivedTimestamp = data.timestamp
            callbacks.data(data.content)
          } else {
            outOfOrder = outOfOrder + 1
          }

        case Failure(ex) =>
          log.error(
            ex,
            "Unable to decode received message {}",
            content.map(x => Integer.toHexString(x)).mkString(" ")
          )
      }

      stay()

    case Event(GetStatistics, _) =>
      sender() ! Statistics(latencies.getSnapshot.getMedian.toInt, outOfOrder, sizes.getSnapshot.getMedian.toInt, meter.getOneMinuteRate)
      stay()

    case Event(data: RawData, PeerConnection(_, _, Some(socket), Some(peerAddress))) =>
      socket ! Udp.Send(compress(data.wrap.pack(serialiser)), peerAddress)
      stay()
  }

  when(Closing) {
    case Event(Udp.Unbound, _) =>
      callbacks.closed()
      log.info("Closed")

      goto(Uninitialised) using NoConnection
  }

  whenUnhandled {
    case Event(Close, PeerConnection(_, _, Some(socket), _)) =>
      log.info("Unbinding from socket")
      socket ! Udp.Unbind
      goto(Closing) using NoConnection
  }

  initialize()

  val currentTimestamp = new AtomicLong(0)
  var lastReceivedTimestamp = -1L

  private def compress(data: ByteString): ByteString = {
    val compressor = lz4.fastCompressor()
    val maxlen = compressor.maxCompressedLength(data.size)
    val bytesOut = ByteBuffer.allocate(4 + maxlen)
    val length = data.length

    bytesOut.putInt(0, length)
    compressor.compress(data.asByteBuffer, 0, length, bytesOut, 4, maxlen)
    ByteString.fromByteBuffer(bytesOut)
  }

  private def decompress(compressed: ByteString): ByteString = {
    val decompressor = lz4.fastDecompressor()
    val iter = compressed.iterator
    val decompressedLength = iter.getInt
    val bytesOut = ByteBuffer.allocate(decompressedLength)

    decompressor.decompress(compressed.asByteBuffer, 4, bytesOut, 0, decompressedLength)
    ByteString(bytesOut)
  }
}
