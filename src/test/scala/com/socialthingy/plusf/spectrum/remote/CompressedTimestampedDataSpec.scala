package com.socialthingy.plusf.spectrum.remote

import java.io.{InputStream, OutputStream}
import java.util.function.{Consumer, Function => JFunction}

import org.scalatest.{FlatSpec, Matchers}
import org.apache.commons.lang3.tuple.{Pair => JPair}

import scala.util.Random

class CompressedTimestampedDataSpec extends FlatSpec with Matchers {

  val serialiser: Consumer[JPair[SpectrumState, OutputStream]] = new Consumer[JPair[SpectrumState, OutputStream]] {
    override def accept(t: JPair[SpectrumState, OutputStream]): Unit = SpectrumState.serialise(t)
  }

  val deserialiser: JFunction[InputStream, SpectrumState] = new JFunction[InputStream, SpectrumState] {
    override def apply(t: InputStream): SpectrumState = SpectrumState.deserialise(t)
  }

  "compressed timestamped data" should "serialise and deserialise correctly" in {
    val screen = Array.fill[Int](0x10000)(Random.nextInt(255))
    val borderLines = Array.fill[Int](SpectrumState.BORDER_LINE_COUNT)(Random.nextInt(255))
    val input = new CompressedTimestampedData[SpectrumState](1L, new SpectrumState(screen, borderLines, true))

    val packet = input.toPacket(serialiser)

    val bytesOut = packet.getData
    println(packet.getLength)

    val output = CompressedTimestampedData.from(packet, deserialiser)

    output.getData.getBorderLines shouldBe input.getData.getBorderLines
    output.getData.getMemory.slice(0x4000, 0x5b00) shouldBe input.getData.getMemory.slice(0x4000, 0x5b00)
  }

}
