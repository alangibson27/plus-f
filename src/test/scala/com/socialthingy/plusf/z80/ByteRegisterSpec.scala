package com.socialthingy.plusf.z80

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ByteRegisterSpec extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  val reg = new ByteRegister

  "byte register" should "set and return supplied value" in {
    val rtn = reg.set(0x12)

    rtn shouldBe 0x12
    reg.get() shouldBe 0x12
  }

  it should "wrap values above 0xff" in {
    val rtn = reg.set(0x101)

    rtn shouldBe 0x01
    reg.get() shouldBe 0x01
  }

  it should "wrap values below 0x00" in {
    val rtn = reg.set(-0x01)

    rtn shouldBe 0xff
    reg.get() shouldBe 0xff
  }

  it should "return correct signed value" in {
    val unsignedValues = Table(
      ("unsigned", "signed"),
      (0xff, -1),
      (0x00, 0),
      (0x80, -128),
      (0x01, 1),
      (0x7f, 127)
    )

    forAll(unsignedValues) { (unsigned, signed) =>
      reg.set(unsigned)
      reg.signedGet() shouldBe signed
    }

  }

}

