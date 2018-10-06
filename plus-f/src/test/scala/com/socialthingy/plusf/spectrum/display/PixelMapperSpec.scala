package com.socialthingy.plusf.spectrum.display

import java.util

import org.scalatest.{FlatSpec, GivenWhenThen, Inspectors, Matchers}
import org.scalatest.prop.TableDrivenPropertyChecks

class PixelMapperSpec extends FlatSpec with GivenWhenThen with Matchers with TableDrivenPropertyChecks with Inspectors {

  "PixelMapper" should "correctly map the Spectrum display memory to a bitmap" in {
    val memory = Array.ofDim[Int](0x10000)
    Given("the display ink colour is black and the paper colour is white")
    util.Arrays.fill(memory, 0x5800, 0x5b00, 56)

    And("the pixels at the four corners of the screen are set")
    memory(0x4000) = 0x80
    memory(0x401f) = 0x01
    memory(0x57e0) = 0x80
    memory(0x57ff) = 0x01

    When("the memory is mapped to a bitmap")
    val bitmap = new PixelMapper().getPixels(memory, false)

    Then("the four corners of the bitmap are black pixels")
    bitmap should have size 258 * 194
    val black = SpectrumColour.dullColour(0)
    val white = SpectrumColour.dullColour(7)

    val blackPixels = List(258 + 1, 258 + 256, (258 * 192) + 1, (258 * 192) + 256)
    forEvery(blackPixels) { pixel => bitmap(pixel) shouldBe black }

    And("all other pixels are white pixels")
    (1 until 194) foreach { y =>
      val xmax = if (y == 1 || y == 193) 1 else 0
      val xmin = if (y == 1 || y == 193) 256 else 257

      (xmin until xmax) foreach { x =>
        bitmap(x + (y * 194)) shouldBe white
      }
    }
  }

}
