package com.socialthingy.plusf.spectrum.remote

import java.net.{DatagramPacket, DatagramSocket}
import java.util.concurrent.atomic.AtomicLong
import java.util.function.{Consumer, Supplier}

import org.scalatest.concurrent.Eventually
import org.scalatest._
import org.scalatest.mock.MockitoSugar

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

class NetworkPeerSpec extends FlatSpec
  with Matchers with Inspectors with BeforeAndAfter with BeforeAndAfterAll with Eventually with MockitoSugar {

  implicit override val patienceConfig = PatienceConfig(1 second)

  val mockHostSocket = new DatagramSocket()
  val receivedData = new ListBuffer[TimestampedData[TestData]]
  val mockHost = Future {
    val packetBytes = Array.ofDim[Byte](16384)
    val packet = new DatagramPacket(packetBytes, packetBytes.length)
    while (true) {
      mockHostSocket.receive(packet)
      val data: TimestampedData[TestData] = TimestampedData.from(packet)
      receivedData += data
    }
  }

  before {
    receivedData.clear()
  }

  override def afterAll(): Unit = {
    mockHostSocket.close()
  }

  "network peer" should "successfully send a correctly timestamped data packet to its partner" in withStubbedPeer {
    (peer, updater, timestamper) =>
      // when
      peer.sendDataToPartner(TestData("sent to partner"))

      // then
      eventually {
        forExactly(1, receivedData) { x =>
          x.getTimestamp shouldBe 1L
          x.getData shouldBe TestData("sent to partner")
        }
      }
  }

  it should "successfully receive a correctly timestamped data packet from the host" in withStubbedPeer {
    (guest, updater, timestamper) =>

      // given
      val hostData = new TimestampedData[TestData](1L, TestData("sent by partner"))
      val packet = hostData.toPacket
      packet.setSocketAddress(guest.getLocalAddress)

      // when
      mockHostSocket.send(packet)

      // then
      eventually {
        forExactly(1, updater.receivedFromPartner) { state =>
          state shouldBe TestData("sent by partner")
        }
      }
  }

  it should "ignore a timestamped data packet that is older than the last received one" in withStubbedPeer {
    (guest, updater, timestamper) =>

      // given
      val lastReceivedPacket = new TimestampedData(100L, TestData("latest packet"))
      guest.receivePartnerData(lastReceivedPacket)
      updater.receivedFromPartner should have size 1

      // when
      val nextPacket = new TimestampedData(99L, TestData("old packet"))
      guest.receivePartnerData(nextPacket)

      // then
      updater.receivedFromPartner should have size 1
  }

  it should "accept a timestamped data packet that is newer than the last received one" in withStubbedPeer {
    (guest, updater, timestamper) =>

      // given
      val lastReceivedPacket = new TimestampedData(100L, TestData("previous packet"))
      guest.receivePartnerData(lastReceivedPacket)
      updater.receivedFromPartner should have size 1

      // when
      val nextPacket = new TimestampedData(101L, TestData("most recent packet"))
      guest.receivePartnerData(nextPacket)

      // then
      updater.receivedFromPartner should have size 2
  }

  it should "correctly record when a packet arrives out of sequence" in withStubbedPeer {
    (guest, updater, timestamper) =>

      // given
      guest.receivePartnerData(new TimestampedData(101L, TestData("packet 2")))
      guest.receivePartnerData(new TimestampedData(100L, TestData("packet 1")))

      // when
      val outOfOrderCount = guest.getOutOfOrderPacketCount

      // then
      outOfOrderCount shouldBe 1L
  }

  it should "correctly record when multiple packets arrive out of sequence" in withStubbedPeer {
    (guest, updater, timestamper) =>

      // given
      guest.receivePartnerData(new TimestampedData(109L, TestData("packet 3")))
      guest.receivePartnerData(new TimestampedData(110L, TestData("packet 4")))
      guest.receivePartnerData(new TimestampedData(100L, TestData("packet 1")))
      guest.receivePartnerData(new TimestampedData(101L, TestData("packet 2")))

      // when
      val outOfOrderCount = guest.getOutOfOrderPacketCount

      // then
      outOfOrderCount shouldBe 2L
  }

  val doNothing: SpectrumState => Unit = (x: SpectrumState) => ()
  var nextPort = 7100

  def withStubbedPeer(testCode: (NetworkPeer[TestData], StubbedUpdater, AtomicLong) => Any): Unit = {
    val updater = new StubbedUpdater
    val consumer = new Consumer[TestData] {
      override def accept(t: TestData): Unit = updater.storeReceived(t)
    }
    val timestampValue = new AtomicLong(1L)
    val timestamper = new Supplier[java.lang.Long] {
      override def get(): java.lang.Long = timestampValue.get()
    }
    val port = {
      val p = nextPort
      nextPort += 1
      p
    }
    val peer = new NetworkPeer[TestData](consumer, timestamper, port, mockHostSocket.getLocalSocketAddress)

    try {
      testCode(peer, updater, timestampValue)
    } finally {
      peer.disconnect()
    }
  }

  class StubbedUpdater {
    val receivedFromPartner = new ListBuffer[TestData]
    val storeReceived: TestData => Unit = td => receivedFromPartner += td
  }

}

case class TestData(text: String)