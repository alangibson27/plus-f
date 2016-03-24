package com.socialthingy.qaopm.spectrum

import org.scalatest.{Matchers, FlatSpec}

class BorderSpec extends FlatSpec with Matchers {

  "border" should "be set based on the lowest 3 bits of the last byte sent to the ULA on port 0xfe" in new TestComputer {
    // when
    ula.write(0xfe, 0x00, binary("00000101"))

    // then
    withClue("border colour is cyan") {
      val correct = ula.getBorderLines.toList forall { _ == 0xff00aaaa }
      correct shouldBe true
    }
  }

  trait TestComputer {
    val ula = new ULA

    def binary(bin: String): Int = Integer.parseInt(bin, 2)
  }
}
