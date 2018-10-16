package com.socialthingy.plusf.spectrum.display

import java.awt.Color

import org.scalatest.{FlatSpec, Matchers}

class DisplaySpec extends FlatSpec with Matchers {
/*
  val flashing = true
  val notFlashing = false

  "colour block with flash attribute set" should "show non-inverted colours when not flashing" in new TestDisplay {
    // given
    memory.set(0x4000, binary("10000000"))
    memory.set(0x5800, binary("10000001"))

    // when
    refresh(memory, notFlashing)

    // then
    pixels(0) shouldBe new Color(0x00, 0x00, 0xcc)
    pixels(1) shouldBe Color.BLACK
  }

  it should "show inverted colours when flashing" in new TestDisplay {
    // given
    memory.set(0x4000, binary("10000000"))
    memory.set(0x5800, binary("10000001"))

    // when
    refresh(memory, flashing)

    // then
    pixels(0) shouldBe Color.BLACK
    pixels(1) shouldBe new Color(0x00, 0x00, 0xcc)
  }

  "colour block with flashing attribute not set" should "not show inverted colours when flashing" in new TestDisplay {
    // given
    memory.set(0x4000, binary("10000000"))
    memory.set(0x5800, binary("00000001"))

    // when
    refresh(memory, notFlashing)

    // then
    pixels(0) shouldBe new Color(0x00, 0x00, 0xcc)
    pixels(1) shouldBe Color.BLACK
  }

  class TestDisplay {
    val display = new Display(16, 16) {
      override def setPixel(x: Int, y: Int, colour: Int): Unit = {
        pixels((y * 192) + x) = colour
      }
    }
    val borderLines = Array.ofDim[Int](1)
    val memory = Array.ofDim[Int](0x10000)
    val pixels = Array.ofDim[Int](256 * 192)

    def refresh(memory: Array[Int], flashActive: Boolean): Unit = {
      display.draw(memory, flashActive)
    }
  }

  def binary(value: String) = Integer.parseInt(value, 2)
*/
}
