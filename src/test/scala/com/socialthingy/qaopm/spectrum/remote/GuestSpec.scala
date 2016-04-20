package com.socialthingy.qaopm.spectrum.remote

import java.net.{DatagramPacket, DatagramSocket}
import java.util.concurrent.atomic.AtomicLong
import java.util.function.{Consumer, Supplier}
import javafx.scene.input.{KeyCode, KeyEvent}

import org.scalatest.concurrent.Eventually
import org.scalatest._
import org.scalatest.mock.MockitoSugar

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

class GuestSpec extends FlatSpec
  with Matchers with Inspectors with BeforeAndAfter with BeforeAndAfterAll with Eventually with MockitoSugar {

  implicit override val patienceConfig = PatienceConfig(1 second)

  val mockHostSocket = new DatagramSocket()
  val receivedData = new ListBuffer[TimestampedData[KeyEvent]]
  val mockHost = Future {
    val packetBytes = Array.ofDim[Byte](16384)
    val packet = new DatagramPacket(packetBytes, packetBytes.length)
    while (true) {
      mockHostSocket.receive(packet)
      val data: TimestampedData[KeyEvent] = TimestampedData.from(packet)
      receivedData += data
    }
  }

  before {
    receivedData.clear()
  }

  override def afterAll(): Unit = {
    mockHostSocket.close()
  }

  "guest" should "successfully send a correctly timestamped data packet to the host" in withStubbedGuest {
    (guest, updater, timestamper) =>
      // given
      val keyEvent = keyDown(KeyCode.Q)

      // when
      guest.sendKeyToHost(keyEvent)

      // then
      eventually {
        forExactly(1, receivedData) { x =>
          x.getTimestamp shouldBe 1L
          x.getData.toString shouldBe keyEvent.toString
        }
      }
  }

  it should "successfully receive a correctly timestamped data packet from the host" in withStubbedGuest {
    (guest, updater, timestamper) =>

      // given
      val screen = Array.ofDim[Byte](6912)
      val borderLines = Array.ofDim[Int](192)
      val flashActive = false
      val hostData = new TimestampedData[SpectrumState](1L, new SpectrumState(screen, borderLines, flashActive))
      val packet = hostData.toPacket
      packet.setSocketAddress(guest.getLocalAddress)

      // when
      mockHostSocket.send(packet)

      // then
      eventually {
        forExactly(1, updater.receivedFromHost) { state =>
          state.getScreen shouldBe screen
          state.getBorderLines shouldBe borderLines
          state.isFlashActive shouldBe flashActive
        }
      }
  }

  it should "ignore a timestamped data packet that is older than the last received one" in withStubbedGuest {
    (guest, updater, timestamper) =>

      // given
      val lastReceivedPacket = new TimestampedData[SpectrumState](100L, mock[SpectrumState])
      guest.receiveHostData(lastReceivedPacket)
      updater.receivedFromHost should have size 1

      // when
      val nextPacket = new TimestampedData[SpectrumState](99L, mock[SpectrumState])
      guest.receiveHostData(nextPacket)

      // then
      updater.receivedFromHost should have size 1
  }

  it should "accept a timestamped data packet that is newer than the last received one" in withStubbedGuest {
    (guest, updater, timestamper) =>

      // given
      val lastReceivedPacket = new TimestampedData[SpectrumState](100L, mock[SpectrumState])
      guest.receiveHostData(lastReceivedPacket)
      updater.receivedFromHost should have size 1

      // when
      val nextPacket = new TimestampedData[SpectrumState](101L, mock[SpectrumState])
      guest.receiveHostData(nextPacket)

      // then
      updater.receivedFromHost should have size 2
  }

  it should "correctly record the latency of a packet" in withStubbedGuest {
    (guest, updater, timestamper) =>

      // given
      val packet = new TimestampedData[SpectrumState](100L, mock[SpectrumState])
      timestamper.set(101L)
      guest.receiveHostData(packet)

      // when
      val latency = guest.getAverageLatency

      // then
      latency shouldBe 1.0
  }

  it should "correctly record the average latency of multiple packets" in withStubbedGuest {
    (guest, updater, timestamper) =>

      // given
      timestamper.set(101L)
      guest.receiveHostData(new TimestampedData[SpectrumState](100L, mock[SpectrumState]))

      timestamper.set(103L)
      guest.receiveHostData(new TimestampedData[SpectrumState](101L, mock[SpectrumState]))

      // when
      val latency = guest.getAverageLatency

      // then
      latency shouldBe 1.5
  }

  it should "correctly record when a packet arrives out of sequence" in withStubbedGuest {
    (guest, updater, timestamper) =>

      // given
      guest.receiveHostData(new TimestampedData[SpectrumState](101L, mock[SpectrumState]))
      guest.receiveHostData(new TimestampedData[SpectrumState](100L, mock[SpectrumState]))

      // when
      val outOfOrderCount = guest.getOutOfOrderPacketCount

      // then
      outOfOrderCount shouldBe 1L
  }

  it should "correctly record when multiple packets arrive out of sequence" in withStubbedGuest {
    (guest, updater, timestamper) =>

      // given
      guest.receiveHostData(new TimestampedData[SpectrumState](109L, mock[SpectrumState]))
      guest.receiveHostData(new TimestampedData[SpectrumState](110L, mock[SpectrumState]))
      guest.receiveHostData(new TimestampedData[SpectrumState](100L, mock[SpectrumState]))
      guest.receiveHostData(new TimestampedData[SpectrumState](101L, mock[SpectrumState]))

      // when
      val outOfOrderCount = guest.getOutOfOrderPacketCount

      // then
      outOfOrderCount shouldBe 2L
  }

  val doNothing: SpectrumState => Unit = (x: SpectrumState) => ()

  implicit def toConsumer[T](fn: T => Unit): Consumer[T] = new Consumer[T] {
    override def accept(t: T): Unit = fn(t)
  }

  def supplierOf[T](fn: => T): Supplier[T] = new Supplier[T] {
    override def get: T = fn
  }

  def keyDown(keyCode: KeyCode): KeyEvent =
    new KeyEvent(KeyEvent.KEY_PRESSED, keyCode.getName, keyCode.getName, keyCode, false, false, false, false)

  def withStubbedGuest(testCode: (Guest, StubbedUpdater, AtomicLong) => Any): Unit = {
    val updater = new StubbedUpdater
    val timestamper = new AtomicLong(1L)
    val guest = new Guest(supplierOf(timestamper.get), mockHostSocket.getLocalSocketAddress, updater.storeReceived _)

    try {
      testCode(guest, updater, timestamper)
    } finally {
      guest.disconnectFromHost()
    }
  }

  class StubbedUpdater {
    val receivedFromHost = new ListBuffer[SpectrumState]
    def storeReceived(state: SpectrumState): Unit = receivedFromHost += state
  }
}
