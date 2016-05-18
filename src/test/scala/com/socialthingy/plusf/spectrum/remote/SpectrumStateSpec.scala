package com.socialthingy.plusf.spectrum.remote

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.apache.commons.lang3.tuple.{Pair => JPair}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

class SpectrumStateSpec extends FlatSpec with Matchers {

  "SpectrumState" should "serialise and deserialise correctly" in {
    val screen = Array.fill[Int](0x10000)(Random.nextInt(255))
    val borderLines = Array.fill[Int](SpectrumState.BORDER_LINE_COUNT)(Random.nextInt(255))
    val input = new SpectrumState(screen, borderLines, true)

    val bytesOut = new ByteArrayOutputStream
    SpectrumState.serialise(JPair.of(input, bytesOut))
    val serialisedForm = bytesOut.toByteArray

    val bytesIn = new ByteArrayInputStream(serialisedForm)
    val output = SpectrumState.deserialise(bytesIn)

    output.getMemory.slice(0x4000, 0x5b00) shouldBe input.getMemory.slice(0x4000, 0x5b00)
    output.getBorderLines shouldBe input.getBorderLines
    output.isFlashActive shouldBe input.isFlashActive
  }

}
