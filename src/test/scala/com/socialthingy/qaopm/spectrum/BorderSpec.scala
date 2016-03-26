package com.socialthingy.qaopm.spectrum

import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Inspectors, Matchers, FlatSpec}

class BorderSpec extends FlatSpec with Matchers with MockitoSugar with Inspectors {
  type SpectrumColourId = Int

  "border change at 0 t-states" should "set the colour for the entire border" in new TestComputer {
    // given
    atClockCycle(0)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    val borderLines = ula.getBorderLines.toList

    // then
    forAll(borderLines) { x => x shouldBe 0xff00aaaa}
  }

  "border change at 224 t-states" should "set the colour for all but the first line of the border" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(224)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    val borderLines = ula.getBorderLines.toList

    // then
    borderLines.head shouldBe 0xff000000
    forAll(borderLines.tail)  { x => x shouldBe 0xff00aaaa }
  }

  "border changes at 224 and 448 t-states" should "change the colour at lines 1 and 2" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(224)(ula.write(0xfe, 0x00, binary("00000101")))
    atClockCycle(448)(ula.write(0xfe, 0x00, binary("00000110")))

    // when
    val borderLines = ula.getBorderLines.toList

    // then
    borderLines.head shouldBe 0xff000000
    borderLines.tail.head shouldBe 0xff00aaaa
    forAll(borderLines.tail.tail){ x => x shouldBe 0xffaaaa00 }
  }

  "border change after 69888 t-states" should "have effect on the following border refresh" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(69889)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    val borderLines = ula.getBorderLines.toList
    forAll(borderLines) { x => x shouldBe 0xff000000 }

    // then
    val nextBorderLines = ula.getBorderLines.toList
    forAll(nextBorderLines) { x => x shouldBe 0xff00aaaa }
  }

  "border change at 69664 t-states" should "change only the last line of the border" in new TestComputer {
    // given
    previousBorderColourIs(BLACK)

    atClockCycle(69664)(ula.write(0xfe, 0x00, binary("00000101")))

    // when
    val borderLines = ula.getBorderLines.toList

    // then
    forAll(borderLines.dropRight(1)) { x => x shouldBe 0xff000000 }
    forAll(borderLines.takeRight(1)) { x => x shouldBe 0xff00aaaa }
  }

  trait TestComputer {
    val BLACK: SpectrumColourId = 0

    val computer = mock[Computer]
    val ula = new ULA(computer, ULA.TOP_BORDER_HEIGHT, ULA.BOTTOM_BORDER_HEIGHT)

    def atClockCycle(clockCycle: Int)(state: => Unit) = {
      Mockito.when(computer.getCurrentCycleTstates).thenReturn(clockCycle)
      state
    }

    def previousBorderColourIs(colour: SpectrumColourId) = atClockCycle(0)(ula.write(0xfe, 0x00, colour))

    def binary(bin: String): Int = Integer.parseInt(bin, 2)
  }
}
