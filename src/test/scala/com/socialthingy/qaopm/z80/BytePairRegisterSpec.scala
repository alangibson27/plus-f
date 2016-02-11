package com.socialthingy.qaopm.z80

import org.scalatest.{FlatSpec, Matchers}

class BytePairRegisterSpec extends FlatSpec with Matchers {

  "byte pair register" should "set and return supplied value" in new Registers {
    val rtn = reg.set(0x1234)

    rtn shouldBe 0x1234
    reg.get() shouldBe 0x1234
  }

  it should "wrap values above 0xffff" in new Registers {
    val rtn = reg.set(0x10001)

    rtn shouldBe 0x0001
    reg.get() shouldBe 0x0001
  }

  it should "wrap values below 0x0000" in new Registers {
    val rtn = reg.set(-0x0001)

    rtn shouldBe 0xffff
    reg.get() shouldBe 0xffff
  }

  it should "reflect value of a change in high byte" in new Registers {
    reg.get() shouldBe 0x0000

    high.set(0xff)

    reg.get() shouldBe 0xff00
  }

  it should "reflect value of a change in low byte" in new Registers {
    reg.get() shouldBe 0x0000

    low.set(0xff)

    reg.get() shouldBe 0x00ff
  }

  it should "update the low and high bytes correctly when it is changed" in new Registers {
    reg.set(0xbeef)

    high.get() shouldBe 0xbe
    low.get() shouldBe 0xef
  }

  trait Registers {
    val low = new ByteRegister
    val high = new ByteRegister
    val reg = new BytePairRegister(high, low)
  }
}
