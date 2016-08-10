package com.socialthingy.plusf.spectrum.remote

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import javafx.util.{Pair => JPair}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

class SpectrumStateSpec extends FlatSpec with Matchers {

  "SpectrumState" should "serialise and deserialise correctly" in {
    val screen = Array.fill[Int](0x10000)(Random.nextInt(255))
    val borderLines = Array.fill[Int](EmulatorState.BORDER_LINE_COUNT)(Random.nextInt(255))
    val input = new EmulatorState(screen, borderLines, true)

    val bytesOut = new ByteArrayOutputStream
    EmulatorState.serialise(new JPair(input, bytesOut))
    val serialisedForm = bytesOut.toByteArray

    val bytesIn = new ByteArrayInputStream(serialisedForm)
    val output = EmulatorState.deserialise(bytesIn)

    output.getMemory.slice(0x4000, 0x5b00) shouldBe input.getMemory.slice(0x4000, 0x5b00)
    output.getBorderLines shouldBe input.getBorderLines
    output.isFlashActive shouldBe input.isFlashActive
  }

}
