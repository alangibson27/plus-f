package com.socialthingy.p2p

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

private val timestamper = AtomicLong(0)

data class RawData(val content: Any, private val batchPosition: Int = 0) {
    val timestamp = if (batchPosition == 0) {
        timestamper.getAndIncrement()
    } else {
        timestamper.get()
    }
    val wrap = WrappedData(timestamp, System.currentTimeMillis(), content)
}

interface Deserialiser {
    fun deserialise(bytes: ByteBuffer): Any
}

interface Serialiser {
    fun serialise(obj: Any, byteStringBuilder: ByteBuffer)
}

class WrappedData(val timestamp: Long, val systemTime: Long, val content: Any) {
    constructor(bytes: ByteBuffer, deserialiser: Deserialiser) : this(bytes.long, bytes.long, deserialiser.deserialise(bytes))

    fun pack(serialiser: Serialiser): ByteBuffer {
        val buf = ByteBuffer.allocate(32768)
        buf.putLong(timestamp)
        buf.putLong(systemTime)
        serialiser.serialise(content, buf)
        buf.flip()
        return buf
    }
}