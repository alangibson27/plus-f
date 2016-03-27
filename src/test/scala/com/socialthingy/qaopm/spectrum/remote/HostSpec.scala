package com.socialthingy.qaopm.spectrum.remote

import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import javafx.scene.input.KeyCode

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

  implicit val patience = PatienceConfig(timeout = 30 seconds)

  "host" should "transmit a screen grab to the guest" in {
    // when
    val memory = newMemory
    val success = host.send(memory)

    // then
    success shouldBe true
    eventually {
      guestStub.receivedScreens should contain(memory.slice(0x4000, 0x5b00))
    }
  }

  it should "send a set of allowed keys to the guest" in {
    // given
    val allowedKeys = new java.util.ArrayList[KeyCode]
    allowedKeys.add(KeyCode.V)
    allowedKeys.add(KeyCode.B)

    // when
    val success = host.sendAllowedKeys(allowedKeys)

    // then
    success shouldBe true
    eventually {
      guestStub.receivedAllowedKeys should contain(allowedKeys.asScala.toList)
    }
  }

  val host = new Host(32767, InetAddress.getLocalHost, 32768)
  val guestStub = new GuestStub
  Future { guestStub.start() }

  def newMemory: Array[Int] = Array.fill(0x10000) { Random.nextInt() & 0xff }

  class GuestStub {
    val datagramSocket = new DatagramSocket(32768, InetAddress.getLocalHost)
    val receivedScreens = ListBuffer[Array[Int]]()
    val receivedAllowedKeys = ListBuffer[List[KeyCode]]()

    def start(): Unit = {
      while(true) {
        val packet = new DatagramPacket(Array.ofDim[Byte](6912), 6912)
        datagramSocket.receive(packet)

        if (packet.getLength == 6912) {
          val screen = packet.getData.map { x => x & 0xff }
          receivedScreens += screen
        } else {
          val allowedKeys: java.util.ArrayList[KeyCode] = SerializationUtils.deserialize(packet.getData)
          receivedAllowedKeys += allowedKeys.asScala.toList
        }
      }
    }
  }
}
