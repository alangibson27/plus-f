package com.socialthingy.plusf.spectrum.display

import java.awt.Color

import org.scalatest.{FlatSpec, Matchers}

class DisplaySupportSpec extends FlatSpec with Matchers {

  val flashing = true
  val notFlashing = false

  "colour block with flash attribute set" should "show non-inverted colours when not flashing" in new TestDisplay {
    // given
    memory(0x4000) = binary("10000000")
    memory(0x5800) = binary("10000001")

    // when
    refresh(memory, notFlashing)

    // then
    pixels(0) shouldBe new Color(0x00, 0x00, 0xaa)
    pixels(1) shouldBe Color.BLACK
  }

  it should "show inverted colours when flashing" in new TestDisplay {
    // given
    memory(0x4000) = binary("10000000")
    memory(0x5800) = binary("10000001")

    // when
    refresh(memory, flashing)

    // then
    pixels(0) shouldBe Color.BLACK
    pixels(1) shouldBe new Color(0x00, 0x00, 0xaa)
  }

  "colour block with flashing attribute not set" should "not show inverted colours when flashing" in new TestDisplay {
    // given
    memory(0x4000) = binary("10000000")
    memory(0x5800) = binary("00000001")

    // when
    refresh(memory, notFlashing)

    // then
    pixels(0) shouldBe new Color(0x00, 0x00, 0xaa)
    pixels(1) shouldBe Color.BLACK
  }

  trait TestDisplay extends DisplaySupport[Unit] with DisplayPixelUpdate {
    val memory = Array.ofDim[Int](0x10000)
    val pixels = Array.ofDim[Color](256 * 192)

    override def refresh(memory: Array[Int], flashActive: Boolean): Unit = {
      draw(memory, flashActive, this)
    }

    override def update(x: Int, y: Int, colour: Color): Unit = {
      pixels((y * 192) + x) = colour
    }
  }

  def binary(value: String) = Integer.parseInt(value, 2)
}
