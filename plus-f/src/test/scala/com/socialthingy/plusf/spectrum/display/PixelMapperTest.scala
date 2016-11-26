package com.socialthingy.plusf.spectrum.display

import java.util

import org.scalatest.{FlatSpec, GivenWhenThen, Inspectors, Matchers}
import org.scalatest.prop.TableDrivenPropertyChecks

class PixelMapperTest extends FlatSpec with GivenWhenThen with Matchers with TableDrivenPropertyChecks with Inspectors {

  val mappers = Table("mapper class", classOf[SafePixelMapper], classOf[UnsafePixelMapper])

  forAll(mappers) { mapper =>
    s"${mapper.getSimpleName}" should "correctly map the Spectrum display memory to a bitmap" in {
      val memory = Array.ofDim[Int](0x10000)
      Given("the display ink colour is black and the paper colour is white")
      util.Arrays.fill(memory, 0x5800, 0x5b00, 56)

      And("the pixels at the four corners of the screen are set")
      memory(0x4000) = 0x80
      memory(0x401f) = 0x01
      memory(0x57e0) = 0x80
      memory(0x57ff) = 0x01

      When("the memory is mapped to a bitmap")
      val bitmap = mapper.newInstance().getPixels(memory, false)

      Then("the four corners of the bitmap are black pixels")
      bitmap should have size 256 * 192
      val black = SpectrumColour.dullColour(0)
      val white = SpectrumColour.dullColour(7)

      val blackPixels = List(0x0000, 0x00ff, 0xbf00, 0xbfff)
      forEvery(blackPixels) { pixel => bitmap(pixel) shouldBe black }

      And("all other pixels are white pixels")
      val whitePixels = (0 until 0xc000) filterNot blackPixels.contains
      forEvery(whitePixels) { pixel => bitmap(pixel) shouldBe white }
    }
  }

}
