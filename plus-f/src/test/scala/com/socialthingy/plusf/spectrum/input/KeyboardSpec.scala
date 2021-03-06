package com.socialthingy.plusf.spectrum.input

import com.socialthingy.plusf.sound.{AYChip, Beeper}
import com.socialthingy.plusf.spectrum.{Model, TapePlayer}
import com.socialthingy.plusf.spectrum.io.{Keyboard, SpectrumMemory, ULA}
import com.socialthingy.plusf.z80.Clock
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

class KeyboardSpec extends FlatSpec with GivenWhenThen with TableDrivenPropertyChecks with Matchers with MockitoSugar {

  val qwertHalfRow = "11111011".binary

  "keyboard" should "set all 5 low bits on port 254 when no keys are pressed" in new Spectrum {
    Given("no keys are pressed")

    When("port 254 is read")
    val result = ula.read(0xfe, 0) & "11111".binary

    Then("all 5 low bits are set")
    result & "11111".binary shouldBe "11111".binary
  }

  val halfRows = Table(
    ("halfRowKeys", "halfRowCode"),
    ("qwert", 0xfb),
    ("poiuy", 0xdf),
    ("asdfg", 0xfd),
    ("_lkjh", 0xbf),
    ("^zxcv", 0xfe),
    (" $MNB", 0x7f),
    ("12345", 0xf7),
    ("09876", 0xef)
  )

  forAll(halfRows) { (halfRowKeys, halfRowCode) =>
    (0 until 5).foreach { bit =>
      val key = halfRowKeys(bit)
      it should s"reset bit $bit on port 254 when the $key is pressed and its half-row is selected" in new Spectrum {
        Given(s"the $key key is pressed")
        keyboard.keyDown(key)

        When(s"port 254 is read with the half-row for $key selected")
        val result = ula.read(0xfe, halfRowCode) & "11111".binary

        Then(s"bit $bit is reset and all other bits are set")
        (0 until 5).foreach { testBit =>
          if (bit == testBit) {
            result & (1 << testBit) shouldBe 0
          } else {
            result & (1 << testBit) should not be 0
          }
        }
      }
    }
  }

  it should "reset bits 0 and 4 when Q and T are pressed simultaneously" in new Spectrum {
    Given("the Q key is pressed")
    keyboard.keyDown('q')

    And("the T key is pressed")
    keyboard.keyDown('t')

    When(s"port 254 is read with the half-row for Q and T selected")
    val result = ula.read(0xfe, 0xfb) & "11111".binary

    Then("bits 0 and 4 are reset")
    result shouldBe "01110".binary
  }

  it should "reset bits 1 and 3 when Z and N are pressed simulatenously and both their half-rows are selected" in new Spectrum {
    Given("the Z key is pressed")
    keyboard.keyDown('z')

    And("the N key is pressed")
    keyboard.keyDown('n')

    When(s"port 254 is read with the half-rows for Z and N both selected")
    val result = ula.read(0xfe, "01111110".binary) & "11111".binary

    Then("bits 1 and 3 are reset")
    result shouldBe "10101".binary
  }

  trait Spectrum {
    val keyboard = new Keyboard()
    val ula = new ULA(mock[SpectrumMemory], keyboard, new TapePlayer(), mock[Beeper], new Clock, Model._48K)
  }

  implicit class BinaryOps(i: String) {
    def binary = Integer.valueOf(i, 2)
  }

}
