package com.socialthingy.plusf.snapshot

import java.awt.event.KeyEvent._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.socialthingy.plusf.sound.AYChip
import com.socialthingy.plusf.spectrum.Model
import com.socialthingy.plusf.spectrum.io._
import com.socialthingy.plusf.ui.JoystickKeys
import com.socialthingy.plusf.z80.{Clock, ContentionModel, IO, Processor}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SnapshotSaveSpec extends FlatSpec with Matchers with TableDrivenPropertyChecks {
  "Snapshot saver" should "write register values the snapshot correctly" in new Spectrum48k {
    // given
    processor.register("a").set(0x01)
    processor.register("b").set(0x02)
    processor.register("c").set(0x03)
    processor.register("d").set(0x04)
    processor.register("e").set(0x05)
    processor.register("f").set(0x06)
    processor.register("i").set(0x07)
    processor.register("r").set(0xff)
    processor.register("af'").set(0x0910)
    processor.register("bc'").set(0x1112)
    processor.register("de'").set(0x1314)
    processor.register("hl'").set(0x1516)
    processor.register("pc").set(0x1718)
    processor.register("ix").set(0x1920)
    processor.register("iy").set(0x2122)
    processor.register("h").set(0x23)
    processor.register("l").set(0x24)
    processor.register("sp").set(0x2526)

    // when
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

    // then
    output(0) shouldBe 0x01
    output(1) shouldBe 0x06
    output(2) shouldBe 0x03
    output(3) shouldBe 0x02
    output(4) shouldBe 0x24
    output(5) shouldBe 0x23
    output(6) shouldBe 0x00
    output(7) shouldBe 0x00
    output(8) shouldBe 0x26
    output(9) shouldBe 0x25
    output(10) shouldBe 0x07
    output(11) shouldBe 0xff
    output(13) shouldBe 0x05
    output(14) shouldBe 0x04
    output(15) shouldBe 0x12
    output(16) shouldBe 0x11
    output(17) shouldBe 0x14
    output(18) shouldBe 0x13
    output(19) shouldBe 0x16
    output(20) shouldBe 0x15
    output(21) shouldBe 0x09
    output(22) shouldBe 0x10
    output(23) shouldBe 0x22
    output(24) shouldBe 0x21
    output(25) shouldBe 0x20
    output(26) shouldBe 0x19
    output(32) shouldBe 0x18
    output(33) shouldBe 0x17
  }

  it should "encode indicator byte 12 correctly" in new Spectrum48k {
    val cases = Table(
      ("r", "border colour", "expected"),
      (0xff, 5, "00101011".asBinary),
      (0x00, 5, "00101010".asBinary)
    )

    forAll(cases) { (r, borderColour, expected) =>
      // given
      processor.register("r").set(r)
      when(ula.getBorderColour).thenReturn(borderColour)

      // when
      val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

      // then
      output(12) shouldBe expected
    }
  }

  it should "correctly write the states of the interrupt flip-flops" in new Spectrum48k {
    // given
    processor.setIff(0, true)
    processor.setIff(1, true)

    // when
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

    // then
    output(27) shouldBe 1
    output(28) shouldBe 1
  }

  it should "correctly write the interrupt mode" in new Spectrum48k {
    val modes = Table("mode", 0, 1, 2)

    forAll(modes) { mode =>
      // given
      processor.setInterruptMode(mode)

      // when
      val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

      // then
      output(29) shouldBe mode
    }
  }

  it should "always have a 55-byte extended header" in new Spectrum48k {
    // when
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

    // then
    output(30) shouldBe 55
    output(31) shouldBe 0
  }

  it should "encode computer models into the correct mode value" in new Spectrum48k {
    val cases = Table(
      ("model", "mode value"),
      (Model._48K, 0),
      (Model._128K, 4),
      (Model.PLUS_2, 12),
      (Model.PLUS_2A, 13)
    )

    forAll(cases) { (model, modeValue) =>
      // when
      val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

      // then
      output(34) shouldBe modeValue
    }
  }

  it should "record the last value written to 0x7ffd for 128k machines" in new Plus2A {
    // given
    processor.register("pc").set(0xc000)
    processor.register("bc").set(0x7ffd)
    processor.register("a").set(0x10)
    memory.set(0xc000, 0xed)
    memory.set(0xc001, 0x79)

    // when
    processor.execute()
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

    // then
    output(35) shouldBe 0x10
  }

  it should "record the last value written to 0xfffd for 128k machines" in new Plus2A {
    // given
    processor.register("pc").set(0xc000)
    processor.register("bc").set(0xfffd)
    processor.register("a").set(0x20)
    memory.set(0xc000, 0xed)
    memory.set(0xc001, 0x79)

    // when
    processor.execute()
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys, ayChip)

    // then
    output(38) shouldBe 0x20
  }

  it should "write the joystick keys in binary and ascii form" in new Spectrum48k {
    // when
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

    // then
    output(63) shouldBe 0x05
    output(64) shouldBe 0x02
    output(65) shouldBe 0x05
    output(66) shouldBe 0x01
    output(67) shouldBe 0x01
    output(68) shouldBe 0x01
    output(69) shouldBe 0x02
    output(70) shouldBe 0x01
    output(71) shouldBe 0x07
    output(72) shouldBe 0x04

    output(73) shouldBe VK_O
    output(74) shouldBe 0x00
    output(75) shouldBe VK_P
    output(76) shouldBe 0x00
    output(77) shouldBe VK_A
    output(78) shouldBe 0x00
    output(79) shouldBe VK_Q
    output(80) shouldBe 0x00
    output(81) shouldBe VK_M
    output(82) shouldBe 0x00
  }

  it should "record the last value written to 0x1ffd for 128k machines" in new Plus2A {
    // given
    processor.register("pc").set(0xc000)
    processor.register("bc").set(0x1ffd)
    processor.register("a").set(0x20)
    memory.set(0xc000, 0xed)
    memory.set(0xc001, 0x79)

    // when
    processor.execute()
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys, ayChip)

    // then
    output(86) shouldBe 0x20
  }

  it should "write all memory banks on a 128k machine" in new Plus2A {
    // given
    (0 to 7).foreach { i =>
      val bankContents = Array.fill(0x4000)(i + 1)
      memory.copyIntoBank(bankContents, i)
    }

    // when
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys, ayChip)

    // then
    val bytes = new ByteArrayInputStream(output.map(_.toByte), 87, output.length - 86)
    val pages = readPages(bytes, 8)
    pages.foreach {
      case (pageNumber, contents) =>
        contents.foreach { _ shouldBe pageNumber - 3 + 1}
    }
  }

  private def readPages(bytes: ByteArrayInputStream, count: Int): Map[Int, Array[Int]] = {
    val pages = mutable.Map[Int, Array[Int]]()
    (1 to count).foreach { i =>
      val length = bytes.read() + (bytes.read() << 8)
      val pageNumber = bytes.read()

      val decompressed = EDCompressor.INSTANCE.decompress(bytes, length)
      pages += (pageNumber -> decompressed)
    }
    pages.toMap
  }

  it should "write the full 48k memory" in new Spectrum48k {
    // given
    (0x4000 to 0x7fff).foreach { i =>
      memory.set(i, 0x08)
    }

    (0x8000 to 0xbfff).foreach { i =>
      memory.set(i, 0x04)
    }

    (0xc000 to 0xffff).foreach { i =>
      memory.set(i, 0x05)
    }

    // when
    val output = snapshotBytes(processor, ula, memory, model, joystickKeys)

    // then
    val bytes = new ByteArrayInputStream(output.map(_.toByte), 87, output.length - 86)
    val pages = readPages(bytes, 3)
    pages.keys should contain allOf (4, 5, 8)
    List(4, 5, 8) foreach { pageNumber =>
      pages(pageNumber).foreach { _ shouldBe pageNumber }
    }
  }

  implicit class StringOps(s: String) {
    def asBinary: Int = Integer.parseInt(s, 2)
  }

  def snapshotBytes(processor: Processor, ula: ULA, memory: SpectrumMemory, model: Model, joystickKeys: JoystickKeys, ayChip: AYChip = null): Array[Int] = {
      val snapshot = new SnapshotSaver(processor, ula, memory, model, ayChip, joystickKeys)
      val target = new ByteArrayOutputStream()
      snapshot.write(target)
      target.toByteArray.map(_.toInt & 0xff)
  }

  trait AbstractSpectrum {
    val clock = new Clock()
    val contentionModel = Mockito.mock(classOf[ContentionModel])
    val ula = Mockito.mock(classOf[ULA])
    val joystickKeys = new JoystickKeys(VK_Q, VK_A, VK_O, VK_P, VK_M)
    def model: Model
    def memory: SpectrumMemory
    def io: IO
    def processor: Processor
  }

  trait Spectrum48k extends AbstractSpectrum {
    val model = Model._48K
    val memory = new Memory48K()
    val io = new IOMultiplexer(memory)
    val processor = new Processor(memory, contentionModel, io, clock)
  }

  trait Plus2A extends AbstractSpectrum {
    val model = Model.PLUS_2A
    val memory = new MemoryPlus2A()
    val ayChip = new AYChip(new java.util.ArrayList())
    val io = new IOMultiplexer(memory, ayChip)
    val processor = new Processor(memory, contentionModel, io, clock)
  }
}
