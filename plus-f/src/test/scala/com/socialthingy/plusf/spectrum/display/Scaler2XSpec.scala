package com.socialthingy.plusf.spectrum.display

import java.util

import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, GivenWhenThen, Inspectors, Matchers}

class Scaler2XSpec
  extends FlatSpec
  with GivenWhenThen with Matchers with TableDrivenPropertyChecks with Inspectors with MockitoSugar {

  val black = SpectrumColour.dullColour(0)
  val white = SpectrumColour.dullColour(7)

  "Scaler2X" should "map source pixels to the target display correctly" in {
    Given("a bitmap with black pixels at each corner and white pixels everywhere else")
    val source = Array.ofDim[Int](0xc384)
    util.Arrays.fill(source, white)
    source(258 + 1) = black
    source(258 + 256) = black
    source((258 * 192) + 1) = black
    source((258 * 192) + 256) = black

    When("the display is rendered")
    val dest = Array.ofDim[Int](0x30000)
    Scaler2X.scale(source, dest)

    Then("the pixels at each corner should be black and all other pixels should be white")
    val blackPixels = List(
      0x0000, 0x0001, 0x0200, 0x0201, // top left
      0x01fe, 0x01ff, 0x03ff, 0x03fe, // top right

      0x2fc00, 0x2fe00, 0x2fe01, // bottom left
      0x2fdff, 0x2fffe, 0x2ffff  // bottom right
    )
    forEvery(blackPixels) { pixel => dest(pixel) shouldBe black }

    val whitePixels = (0 until 0x18000) filterNot blackPixels.contains
    forEvery(whitePixels) { pixel => dest(pixel) shouldBe white }
  }
}
