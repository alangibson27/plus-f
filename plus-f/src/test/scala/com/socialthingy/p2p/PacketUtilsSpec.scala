package com.socialthingy.p2p

import java.net.{InetAddress, InetSocketAddress}
import java.nio.ByteBuffer

import org.scalatest.{FlatSpec, Matchers}

class PacketUtilsSpec extends FlatSpec with Matchers {
  "PacketUtils" should "compress and decompress a message" in {
    val messageIn = ByteBuffer.allocate(16384)
    getClass.getResourceAsStream("/48.rom").read(messageIn.array(), 0, 16384)

    val compressed = PacketUtils.INSTANCE.compress(messageIn)
    compressed.position() shouldBe 0
    compressed.remaining() should be < 16384

    val messageOut = PacketUtils.INSTANCE.decompress(compressed)
    messageOut.position() shouldBe 0
    messageOut.remaining() shouldBe 16384
    messageIn.array() should equal(messageOut.array())
  }

  it should "build the correct packet from a string and an address" in {
    val packet = PacketUtils.INSTANCE.buildPacket("TEST PACKET", new InetSocketAddress("127.0.0.1", 2000))
    packet.getData shouldBe "TEST PACKET".getBytes("UTF-8")
    packet.getAddress shouldBe InetAddress.getByName("127.0.0.1")
    packet.getPort shouldBe 2000
  }
}
