package com.socialthingy.p2p

import java.net.DatagramPacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer

import net.jpountz.lz4.LZ4Factory

object PacketUtils {
    val lz4 = LZ4Factory.fastestJavaInstance()

    fun buildPacket(data: String, destination: InetSocketAddress): DatagramPacket {
        val bytes = data.toByteArray()
        return DatagramPacket(bytes, bytes.size, destination)
    }

    fun decompress(compressed: ByteBuffer): ByteBuffer {
        val decompressor = lz4.fastDecompressor()
        val decompressedLength = compressed.int
        val bytesOut = ByteBuffer.allocate(decompressedLength)

        decompressor.decompress(compressed, 4, bytesOut, 0, decompressedLength)
        return bytesOut
    }

    fun compress(data: ByteBuffer): ByteBuffer {
        val compressor = lz4.fastCompressor()
        val maxlen = compressor.maxCompressedLength(data.limit())
        val bytesOut = ByteBuffer.allocate(4 + maxlen)
        val length = data.limit()

        bytesOut.putInt(length)
        val size = compressor.compress(data, data.position(), length, bytesOut, 4, maxlen)
        bytesOut.limit(size + 4)
        bytesOut.position(0)
        return bytesOut
    }
}
