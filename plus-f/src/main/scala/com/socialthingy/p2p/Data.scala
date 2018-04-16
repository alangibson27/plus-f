package com.socialthingy.p2p

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

object RawData {
  val timestamper = new AtomicLong(0)
}

case class RawData(content: Any, batchPosition: Int = 0) {
  import RawData._
  lazy val timestamp = if (batchPosition == 0) timestamper.getAndIncrement() else timestamper.get()
  lazy val wrap = new WrappedData(timestamp, System.currentTimeMillis, content)
}

trait Deserialiser {
  def deserialise(bytes: ByteBuffer): Any
}

trait Serialiser {
  def serialise(obj: Any, byteStringBuilder: ByteBuffer): Unit
}

object WrappedData {
  def apply(bytes: ByteBuffer, deserialiser: Deserialiser): WrappedData = {
    new WrappedData(bytes.getLong(), bytes.getLong(), deserialiser.deserialise(bytes))
  }
}

class WrappedData(val timestamp: Long, val systemTime: Long, val content: Any) {
  def pack(serialiser: Serialiser): ByteBuffer = {
    val buf = ByteBuffer.allocate(32768)
    buf.putLong(timestamp)
    buf.putLong(systemTime)
    serialiser.serialise(content, buf)
    buf.flip()
    buf
  }
}