package com.socialthingy.plusf.p2p

import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicLong

import akka.util.{ByteString, ByteStringBuilder}

object RawData {
  val timestamper = new AtomicLong(0)
}

case class RawData(content: Any) {
  import RawData._
  lazy val wrap = new WrappedData(timestamper.getAndIncrement(), System.currentTimeMillis, content)
}

trait Deserialiser {
  def deserialise(bytes: ByteString): Any
}

trait Serialiser {
  def serialise(obj: Any, byteStringBuilder: ByteStringBuilder): Unit
}

object WrappedData {
  implicit val byteOrder = ByteOrder.BIG_ENDIAN
  def apply(bytes: ByteString, deserialiser: Deserialiser): WrappedData = {
    val iter = bytes.iterator
    new WrappedData(iter.getLong, iter.getLong, deserialiser.deserialise(iter.toByteString))
  }
}

class WrappedData(val timestamp: Long, val systemTime: Long, val content: Any) {
  import WrappedData._

  def pack(serialiser: Serialiser) = {
    val bsb = new ByteStringBuilder()
    bsb.putLong(timestamp)
    bsb.putLong(systemTime)
    serialiser.serialise(content, bsb)
    bsb.result()
  }
}