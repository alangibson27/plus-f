package com.socialthingy.plusf.snapshot

import java.io.IOException

import com.socialthingy.plusf.ProcessorSpec
import com.socialthingy.plusf.spectrum.Clock
import com.socialthingy.plusf.spectrum.io.ULA
import com.socialthingy.plusf.z80.{IO, Memory, Processor}
import org.mockito.Mockito.verify
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar

class SnapshotLoaderSpec extends ProcessorSpec with Matchers with MockitoSugar {

  trait Spectrum {
    val ula = mock[ULA]
    val clock = new Clock()
    val processor = new Processor(mock[Memory], mock[IO])

    def registerValue(name: String) = processor.register(name).get()
  }

  "Snapshot loader" should "load a z80 v1 snapshot" in new Spectrum {
    // given
    val snapshot = new Snapshot(getClass.getResourceAsStream("/screenfiller.z80"))

    // when
    val memory = snapshot.getMemory(clock)
    snapshot.setBorderColour(ula)
    snapshot.setProcessorState(processor)

    // then
    verify(ula).setBorderColour(7)

    registerValue("a") shouldBe 0xc9
    registerValue("f") shouldBe 0x82
    registerValue("c") shouldBe 0xd0
    registerValue("b") shouldBe 0x5c
    registerValue("l") shouldBe 0xd1
    registerValue("h") shouldBe 0x47

    registerValue("pc") shouldBe 0x5cdb
    registerValue("sp") shouldBe 0xea1d

    registerValue("i") shouldBe 0x3f
    registerValue("r") shouldBe 0x2a

    registerValue("e") shouldBe 0x2f
    registerValue("d") shouldBe 0x13

    registerValue("c'") shouldBe 0x21
    registerValue("b'") shouldBe 0x17
    registerValue("e'") shouldBe 0x9b
    registerValue("d'") shouldBe 0x36
    registerValue("l'") shouldBe 0x58
    registerValue("h'") shouldBe 0x27
    registerValue("a'") shouldBe 0x00
    registerValue("f'") shouldBe 0x44

    registerValue("iy") shouldBe 0x5c3a
    registerValue("ix") shouldBe 0x03d4

    processor.getIff(0) shouldBe false
    processor.getIff(1) shouldBe false

    processor.getInterruptMode shouldBe 1

    (0x4000 until 0x4100) foreach (memory.get(_) shouldBe 0xff)
  }

  it should "load z80 v3 snapshot" in new Spectrum {
    // given
    val snapshot = new Snapshot(getClass.getResourceAsStream("/screenfiller.z80-v3"))

    // when
    val memory = snapshot.getMemory(clock)
    snapshot.setBorderColour(ula)
    snapshot.setProcessorState(processor)

    // then
    verify(ula).setBorderColour(7)

    registerValue("a") shouldBe 0xdd
    registerValue("f") shouldBe 0x82
    registerValue("c") shouldBe 0xd0
    registerValue("b") shouldBe 0x5c
    registerValue("l") shouldBe 0x6e
    registerValue("h") shouldBe 0x4f

    registerValue("pc") shouldBe 0x5cdc
    registerValue("sp") shouldBe 0xea1d

    registerValue("i") shouldBe 0x3f
    registerValue("r") shouldBe 0x12

    registerValue("e") shouldBe 0x92
    registerValue("d") shouldBe 0x0b

    registerValue("c'") shouldBe 0x21
    registerValue("b'") shouldBe 0x17
    registerValue("e'") shouldBe 0x9b
    registerValue("d'") shouldBe 0x36
    registerValue("l'") shouldBe 0x58
    registerValue("h'") shouldBe 0x27
    registerValue("a'") shouldBe 0x00
    registerValue("f'") shouldBe 0x44

    registerValue("iy") shouldBe 0x5c3a
    registerValue("ix") shouldBe 0x03d4

    processor.getIff(0) shouldBe false
    processor.getIff(1) shouldBe false

  }

  it should "reject a snapshot for a non-48k machine" in new Spectrum {
    intercept[IOException] {
      new Snapshot(getClass.getResourceAsStream("/screenfiller.z80-128k"))
    }
  }
}
