package com.socialthingy.plusf.snapshot

import org.scalatest.{FlatSpec, Matchers}
import java.awt.event.KeyEvent._

class KeyIdentifierSpec extends FlatSpec with Matchers {
  "Key identifier" should "recognise the enter key code" in {
    KeyIdentifier.identify(0x06, 0x01) shouldBe VK_ENTER
  }

  it should "recognise the 'g' key code" in {
    KeyIdentifier.identify(0x01, 0x10) shouldBe VK_G
  }

  it should "return -1 when an unrecognisable code is supplied" in {
    KeyIdentifier.identify(0x09, 0x10) shouldBe -1
    KeyIdentifier.identify(0x01, 0x80) shouldBe -1
  }
}
