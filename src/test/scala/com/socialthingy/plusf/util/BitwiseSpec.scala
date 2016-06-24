package com.socialthingy.plusf.util

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class BitwiseSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  "parity" should "return true when the number of high bits in a byte is even" in {
    val evenParityBytes = Table("byte", binary("00000000"), binary("11111111"), binary("10010000"))
    forAll(evenParityBytes) { b =>
      Bitwise.hasParity(b) shouldBe true
    }
  }

  it should "return false when the number of high bits in a byte is odd" in {
    val evenParityBytes = Table("byte", binary("00010000"), binary("11111101"), binary("10110000"))
    forAll(evenParityBytes) { b =>
      Bitwise.hasParity(b) shouldBe false
    }
  }

}
