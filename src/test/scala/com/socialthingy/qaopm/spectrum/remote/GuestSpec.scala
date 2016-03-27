package com.socialthingy.qaopm.spectrum.remote

import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import javafx.scene.input.{KeyCode, KeyEvent}

import org.apache.commons.lang3.SerializationUtils
import org.scalatest.concurrent.Eventually
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Inspectors, Matchers, FlatSpec}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import scala.language.postfixOps

class GuestSpec extends FlatSpec with Matchers with MockitoSugar with Eventually with Inspectors with BeforeAndAfter {

  implicit val patience = PatienceConfig(30 seconds)

  before {
    hostStub.receivedKeys.clear()
  }

  "guest" should "transmit a keypress to the host" in {
    // when
    val keypress = keyDown(KeyCode.Q)
    val sent = guest.sendKeypress(keypress)

    // then
    sent shouldBe true

    eventually {
      hostStub.checkKeyReceived(keypress)
    }
  }

  it should "only send keypresses permitted by the host" in {
    // when
    val qPress = keyDown(KeyCode.Q)
    guest.sendKeypress(qPress)

    val aPress = keyDown(KeyCode.A)
    guest.sendKeypress(aPress)

    val oPress = keyDown(KeyCode.O)
    guest.sendKeypress(oPress)

    // then
    eventually {
      hostStub.receivedKeys should have size 2
      hostStub.checkKeyReceived(qPress)
      hostStub.checkKeyReceived(aPress)
    }
  }

  it should "change the set of allowed keys based on instructions from the host" in {
    // given
    val qPress = keyDown(KeyCode.Q)
    guest.sendKeypress(qPress)

    val aPress = keyDown(KeyCode.A)
    guest.sendKeypress(aPress)

    val oPress = keyDown(KeyCode.O)
    guest.sendKeypress(oPress)

    eventually {
      hostStub.receivedKeys should have size 2
      hostStub.checkKeyReceived(qPress)
    }

    // when
    hostStub.sendAllowedKeys(KeyCode.O)

    guest.sendKeypress(qPress)
    guest.sendKeypress(aPress)
    guest.sendKeypress(oPress)

    // then
    eventually {
      hostStub.receivedKeys should have size 3
      hostStub.checkKeyReceived(qPress)
      hostStub.checkKeyReceived(aPress)
      hostStub.checkKeyReceived(oPress)
    }
  }

  val guest = new Guest(32765, InetAddress.getLocalHost, 32766, Array[KeyCode](KeyCode.Q, KeyCode.A))
  val hostStub = new HostStub
  Future { hostStub.start() }

  def keyDown(keyCode: KeyCode,
              shiftDown: Boolean = false,
              ctrlDown: Boolean = false,
              altDown: Boolean = false,
              metaDown: Boolean = false): KeyEvent = new KeyEvent(
    KeyEvent.KEY_PRESSED,
    keyCode.impl_getCode().asInstanceOf[Char].toString,
    keyCode.getName,
    keyCode,
    shiftDown,
    ctrlDown,
    altDown,
    metaDown
  )

  class HostStub {
    val datagramSocket = new DatagramSocket(32766, InetAddress.getLocalHost)
    val receivedKeys = ListBuffer[KeyEvent]()

    def start(): Unit = {
      while(true) {
        val packet = new DatagramPacket(Array.ofDim[Byte](1024), 1024)
        datagramSocket.receive(packet)
        receivedKeys += SerializationUtils.deserialize(packet.getData).asInstanceOf[KeyEvent]
      }
    }

    def sendAllowedKeys(keyCodes: KeyCode*): Unit = {
      val packet = new DatagramPacket(Array.ofDim[Byte](1024), 1024, InetAddress.getLocalHost, 32765)
      val serializableKeyCodes = new java.util.ArrayList[KeyCode]
      keyCodes.foreach(serializableKeyCodes.add)
      packet.setData(SerializationUtils.serialize(serializableKeyCodes))
      datagramSocket.send(packet)
    }

    def checkKeyReceived(event: KeyEvent) = forExactly(1, receivedKeys) { rcvd =>
      rcvd.getEventType shouldBe event.getEventType
      rcvd.getCode shouldBe event.getCode
      rcvd.isAltDown shouldBe event.isAltDown
      rcvd.isControlDown shouldBe event.isControlDown
      rcvd.isMetaDown shouldBe event.isMetaDown
      rcvd.isShiftDown shouldBe event.isShiftDown
    }

    def checkKeyNotReceived(event: KeyEvent) = forExactly(1, receivedKeys) { rcvd =>
      rcvd.getEventType should not be event.getEventType
      rcvd.getCode should not be event.getCode
      rcvd.isAltDown should not be event.isAltDown
      rcvd.isControlDown should not be event.isControlDown
      rcvd.isMetaDown should not be event.isMetaDown
      rcvd.isShiftDown should not be event.isShiftDown
    }

  }
}
