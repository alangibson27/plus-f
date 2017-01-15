package com.socialthingy.plusf.p2p

import java.net._
import java.nio.{ByteBuffer, ByteOrder}
import java.util.concurrent.{Executors, TimeUnit}

import com.codahale.metrics.{Histogram, Meter, SlidingTimeWindowReservoir}
import net.jpountz.lz4.LZ4Factory
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object Peer {
  sealed trait State
  case object Initial extends State
  case object WaitingForPeer extends State
  case object Connected extends State
  case object Closing extends State
}

class Peer(bindAddress: InetSocketAddress,
           discoveryServiceAddress: InetSocketAddress,
           callbacks: Callbacks,
           serialiser: Serialiser,
           deserialiser: Deserialiser,
           timeout: FiniteDuration) {
  import Peer._
  import PacketUtils._

  type Handler = (ByteBuffer, InetSocketAddress) => Unit

  val log = LoggerFactory.getLogger(classOf[Peer])
  var socket: DatagramSocket = _
  val socketHandlerExecutor = Executors.newSingleThreadExecutor()
  implicit val byteOrder = ByteOrder.BIG_ENDIAN

  val latencies = new Histogram(new SlidingTimeWindowReservoir(1, TimeUnit.MINUTES))
  val sizes = new Histogram(new SlidingTimeWindowReservoir(1, TimeUnit.MINUTES))
  val meter = new Meter()
  var outOfOrder = 0

  var peerConnection: Option[InetSocketAddress] = None

  val states = Map[State, Handler](
    Initial -> doNothing _,
    WaitingForPeer -> waitForResponse _,
    Connected -> connectedToPeer _
  )

  var currentState: State = Initial
  var currentSessionId: Option[String] = None
  var lastReceivedTimestamp = -1L

  def doNothing(data: ByteBuffer, source: InetSocketAddress): Unit = ()

  def connectedToPeer(data: ByteBuffer, source: InetSocketAddress): Unit = {
    Try {
      WrappedData(decompress(data), deserialiser)
    } match {
      case Success(decompressed) =>
        latencies.update(System.currentTimeMillis - decompressed.systemTime)
        sizes.update(data.remaining() / 1024)
        meter.mark()
        if (decompressed.timestamp > lastReceivedTimestamp) {
          lastReceivedTimestamp = decompressed.timestamp
          callbacks.data(decompressed.content)
        } else {
          outOfOrder = outOfOrder + 1
        }

        peerConnection foreach { x =>
          if (!x.equals(source))
            log.info(s"Switching peer connection from ${x.toString} to ${source.toString}")
        }

        peerConnection = Some(source)

      case Failure(ex) =>
        log.error(
          s"Unable to decode received message ${data.toString} from ${source.toString}",
          ex
        )
    }
  }

  def waitForResponse(data: ByteBuffer, source: InetSocketAddress): Unit = {
    val result = new String(data.array(), data.position(), data.remaining(), "UTF-8")
    result.split('|').toList match {
      case "PEER" :: peerHost :: peerPort :: Nil =>
        callbacks.connectedToPeer(peerPort.toInt)
        log.info(s"Connected to peer at $peerHost:$peerPort")
        currentState = Connected
        peerConnection = Some(new InetSocketAddress(peerHost, peerPort.toInt))

      case "WAIT" :: Nil =>
        callbacks.waitingForPeer()
        log.info("Waiting for peer to join")

      case _ =>
        log.error(
          s"Unrecognised message received from ${source.toString} with content [${data.toString}], still waiting for peer"
        )
    }

  }

  def startIfRequired(): Unit = {
    if (socket == null || socket.isClosed) {
      socket = new DatagramSocket(bindAddress)
      socket.setSoTimeout(timeout.toMillis.toInt)

      socketHandlerExecutor.submit(new Runnable {
        def run() = {
          val receivedPacket = new DatagramPacket(Array.ofDim[Byte](16384), 16384)
          while (!socket.isClosed) {
            try {
              socket.receive(receivedPacket)
              log.debug("Received packet of size {} from {}", receivedPacket.getLength, receivedPacket.getAddress.toString)
              states(currentState)(
                ByteBuffer.wrap(receivedPacket.getData, receivedPacket.getOffset, receivedPacket.getLength),
                receivedPacket.getSocketAddress.asInstanceOf[InetSocketAddress]
              )
            } catch {
              case e: SocketTimeoutException if currentState == WaitingForPeer =>
                callbacks.discoveryTimeout()
                close()

              case e: SocketException if e.getMessage == "Socket closed" => // do nothing, expected
              case NonFatal(e) => log.error("Failure in receive loop", e)
            }
          }
          log.info("Socket closed")
        }
      })
    }
  }

  def join(sessionId: String, fwdPort: Option[Int] = None): Unit = {
    startIfRequired()

    callbacks.discovering()
    val joinCommand = fwdPort match {
      case Some(p) => s"JOIN|$sessionId|$p"
      case None => s"JOIN|$sessionId"
    }

    currentState = WaitingForPeer
    currentSessionId = Some(sessionId)
    socket.send(buildPacket(joinCommand, discoveryServiceAddress))
  }

  def send(data: RawData): Unit = (peerConnection, socket) match {
    case (None, _) => log.error("Unable to send data, no peer connection")
    case (_, s) if s.isClosed => log.error("Unable to send data, socket closed")
    case (Some(conn), s) =>
      val compressed = compress(data.wrap.pack(serialiser))
      log.debug("Sending packet of size {} to {}", compressed.remaining(), conn.toString)
      s.send(new DatagramPacket(compressed.array(), compressed.position(), compressed.remaining(), conn))
  }

  def statistics(): Statistics = Statistics(
    latencies.getSnapshot.getMedian.toInt,
    outOfOrder,
    sizes.getSnapshot.getMedian.toInt,
    meter.getOneMinuteRate
  )

  def close(): Unit = (currentState, currentSessionId) match {
    case (WaitingForPeer, Some(sessionId)) =>
      socket.send(buildPacket(s"CANCEL|$sessionId", discoveryServiceAddress))
      reset()
      callbacks.discoveryCancelled()

    case _ =>
      reset()
      callbacks.closed()
  }

  private def reset() = {
    if (socket != null) {
      socket.close()
    }
    currentSessionId = None
    currentState = Initial
    peerConnection = None
  }
}

object PacketUtils {
  val lz4 = LZ4Factory.fastestJavaInstance()

  def buildPacket(data: String, destination: InetSocketAddress) = {
    val bytes = data.getBytes("UTF-8")
    new DatagramPacket(bytes, bytes.length, destination)
  }

  def decompress(compressed: ByteBuffer): ByteBuffer = {
    val decompressor = lz4.fastDecompressor()
    val decompressedLength = compressed.getInt
    val bytesOut = ByteBuffer.allocate(decompressedLength)

    decompressor.decompress(compressed, 4, bytesOut, 0, decompressedLength)
    bytesOut
  }

  def compress(data: ByteBuffer): ByteBuffer = {
    val compressor = lz4.fastCompressor()
    val maxlen = compressor.maxCompressedLength(data.limit())
    val bytesOut = ByteBuffer.allocate(4 + maxlen)
    val length = data.limit()

    bytesOut.putInt(length)
    val size = compressor.compress(data, data.position(), length, bytesOut, 4, maxlen)
    bytesOut.limit(size + 4)
    bytesOut.position(0)
    bytesOut
  }
}

case class Statistics(medianLatency: Int, outOfOrder: Int, size: Int, oneMinuteRate: Double)
