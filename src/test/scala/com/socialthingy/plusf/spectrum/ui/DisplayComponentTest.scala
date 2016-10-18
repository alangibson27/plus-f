package com.socialthingy.plusf.spectrum.ui

import java.util

import com.socialthingy.plusf.spectrum.display.{PixelMapper, SpectrumColour}
import com.socialthingy.plusf.spectrum.io.ULA
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, GivenWhenThen, Inspectors, Matchers}

class DisplayComponentTest
  extends FlatSpec
  with GivenWhenThen with Matchers with TableDrivenPropertyChecks with Inspectors with MockitoSugar {
  val displayComponents = Table(
    "display component class",
    classOf[SafeSwingDoubleSizeDisplay],
    classOf[SwingDoubleSizeDisplay]
  )

  val black = SpectrumColour.dullColour(0)
  val white = SpectrumColour.dullColour(7)

  forAll(displayComponents) { displayComponent =>
    s"${displayComponent.getSimpleName}" should "map source pixels to the target display correctly" in {
      Given("a bitmap with black pixels at each corner and white pixels everywhere else")
      val source = Array.ofDim[Int](0xc000)
      util.Arrays.fill(source, white)
      source(0x0000) = black
      source(0x00ff) = black
      source(0xbf00) = black
      source(0xbfff) = black

      When("the display is rendered")
      val ctr = displayComponent.getConstructor(classOf[PixelMapper], classOf[Array[Int]], classOf[ULA])
      val display = ctr.newInstance(mock[PixelMapper], Array.ofDim[Int](0x10000), mock[ULA])

      display.scale(source)

      Then("the pixels at each corner should be black and all other pixels should be white")
      val blackPixels = List(
        0x0000, 0x0001, 0x0200, // top left
        0x01fe, 0x01ff, 0x03ff, // top right

        0x2fc00, 0x2fe00, 0x2fe01, // bottom left
        0x2fdff, 0x2fffe, 0x2ffff  // bottom right
      )
      forEvery(blackPixels) { pixel => display.targetPixels(pixel) shouldBe black }

      val whitePixels = (0 until 0x18000) filterNot blackPixels.contains
      forEvery(whitePixels) { pixel => display.targetPixels(pixel) shouldBe white }
    }
  }
}
