package com.socialthingy.p2p

import java.net.{DatagramPacket, InetSocketAddress}
import java.nio.ByteBuffer

import net.jpountz.lz4.LZ4Factory

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
