package com.socialthingy.plusf.spectrum.io

import com.socialthingy.plusf.spectrum.TapePlayer
import com.socialthingy.plusf.spectrum.display.Display
import com.socialthingy.plusf.spectrum.display.Display.{BOTTOM_BORDER_HEIGHT, TOP_BORDER_HEIGHT}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class BorderSpec extends FlatSpec with Matchers with Inspectors with MockitoSugar {

  "border change at 0 t-states" should "set the colour for the entire border" in new TestComputer {
    // given
    atClockCycle(0)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    display.redrawBorder()
    val borderLines = display.getBorderLines.toList

    // then
    forAll(borderLines) { x => x shouldBe 0xff00cccc}
  }

  "border change at 224 t-states" should "set the colour for all but the first line of the border" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(224)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    display.redrawBorder()
    val borderLines = display.getBorderLines.toList

    // then
    borderLines.head shouldBe 0xff000000
    forAll(borderLines.tail)  { x => x shouldBe 0xff00cccc }
  }

  "border changes at 224 and 448 t-states" should "change the colour at lines 1 and 2" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(224)(ula.write(0xfe, 0x00, binary("00000101")))
    atClockCycle(448)(ula.write(0xfe, 0x00, binary("00000110")))

    // when
    display.redrawBorder()
    val borderLines = display.getBorderLines.toList

    // then
    borderLines.head shouldBe 0xff000000
    borderLines.tail.head shouldBe 0xff00cccc
    forAll(borderLines.tail.tail){ x => x shouldBe 0xffcccc00 }
  }

  "border change after 69888 t-states" should "have effect on the following border refresh" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(69889)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    display.redrawBorder()
    val borderLines = display.getBorderLines.toList
    forAll(borderLines) { x => x shouldBe 0xff000000 }

    // then
    display.redrawBorder()
    val nextBorderLines = display.getBorderLines.toList
    forAll(nextBorderLines) { x => x shouldBe 0xff00cccc }
  }

  "border change at 69664 t-states" should "change only the last line of the border" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(69664)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    display.redrawBorder()
    val borderLines = display.getBorderLines.toList

    // then
    forAll(borderLines.dropRight(1)) { x => x shouldBe 0xff000000 }
    forAll(borderLines.takeRight(1)) { x => x shouldBe 0xff00cccc }
  }

  type SpectrumColourId = Int
  trait TestComputer {
    val BLACK: SpectrumColourId = 0
    val display = new Display(TOP_BORDER_HEIGHT, BOTTOM_BORDER_HEIGHT)
    val ula = new ULA(display, new Keyboard(), new TapePlayer())

    def atClockCycle(clockCycle: Int)(state: => Unit) = {
      ula.newCycle()
      ula.advanceCycle(clockCycle)
      state
    }

    def previousBorderColourIs(colour: SpectrumColourId) = atClockCycle(0)(ula.write(0xfe, 0x00, colour))

    def binary(bin: String): Int = Integer.parseInt(bin, 2)
  }
}
