package com.socialthingy.plusf.snapshot

import java.io.IOException

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.Matchers

class SnapshotLoaderSpec extends ProcessorSpec with Matchers {

  "Snapshot loader" should "load a z80 v1 snapshot" in new Machine {
    // given
    val loader = new SnapshotLoader(getClass.getResourceAsStream("/screenfiller.z80"))

    // when
    val borderColour = loader.read(processor, memory)

    // then
    borderColour shouldBe 7

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

    (0x4000 until 0x4100) foreach (memory(_) shouldBe 0xff)
  }

  it should "load z80 v3 snapshot" in new Machine {
    // given
    val loader = new SnapshotLoader(getClass.getResourceAsStream("/screenfiller.z80-v3"))

    // when
    val borderColour = loader.read(processor, memory)

    // then
    borderColour shouldBe 7

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

  it should "reject a snapshot for a non-48k machine" in new Machine {
    intercept[IOException] {
      val loader = new SnapshotLoader(getClass.getResourceAsStream("/screenfiller.z80-128k"))
      loader.read(processor, memory)
    }
  }
}
