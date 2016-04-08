package com.socialthingy.qaopm.spectrum.remote

import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import java.util.function.Consumer
import javafx.scene.input.{KeyEvent, KeyCode}

import org.apache.commons.lang3.SerializationUtils
import org.scalatest.concurrent.Eventually
import org.scalatest.{Inspectors, FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

import scala.collection.JavaConverters._

import scala.language.postfixOps

class HostSpec extends FlatSpec with Matchers with Eventually with Inspectors {

  implicit val patience = PatienceConfig(timeout = 1 second)

  "host" should "transmit allowed keys and a screen grab to the guest" in {
    // given
    val allowedKeys = new java.util.ArrayList[KeyCode]
    allowedKeys.add(KeyCode.V)
    allowedKeys.add(KeyCode.B)
    val memory = newMemory

    // when
    val success = host.sendToGuest(allowedKeys, memory, Array.ofDim[Int](192), false)

    // then
    success shouldBe true
    eventually {
      guestStub.receivedScreens should contain(memory.slice(0x4000, 0x5b00))
      guestStub.receivedAllowedKeys should contain(allowedKeys.asScala.toList)
    }
  }

  val host = new Host(
    new Consumer[KeyEvent] {
      override def accept(t: KeyEvent): Unit = ()
    }, 0
  )
  val guestStub = new GuestStub
  Future { guestStub.start() }

  def newMemory: Array[Int] = Array.fill(0x10000) { Random.nextInt() & 0xff }

  class GuestStub {
    val datagramSocket = new DatagramSocket(32768, InetAddress.getLocalHost)
    val receivedScreens = ListBuffer[Array[Int]]()
    val receivedAllowedKeys = ListBuffer[List[KeyCode]]()

    def start(): Unit = {
      while(true) {
        val packet = new DatagramPacket(Array.ofDim[Byte](16384), 16384)
        datagramSocket.receive(packet)

        val hostData = SerializationUtils.deserialize(packet.getData).asInstanceOf[HostData]

        if (hostData.getScreen != null) {
          val screen = hostData.getScreen.map { x => x & 0xff }
          receivedScreens += screen
        }

        if (hostData.getAllowedKeys != null) {
          receivedAllowedKeys += hostData.getAllowedKeys.asScala.toList
        }
      }
    }
  }
}
