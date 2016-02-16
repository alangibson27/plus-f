package com.socialthingy.qaopm.z80

import org.scalatest.{Matchers, FlatSpec}

class FlagsRegisterSpec extends FlatSpec with Matchers {

  val flagsReg = new FlagsRegister

  "flags register" should "set c flag correctly" in {
    flagsReg.setC(true)
    flagsReg.getC shouldBe true
  }

  "flags register" should "reset c flag correctly" in {
    flagsReg.setC(false)
    flagsReg.getC shouldBe false
  }

  "flags register" should "set n flag correctly" in {
    flagsReg.setN(true)
    flagsReg.getN shouldBe true
  }

  "flags register" should "reset n flag correctly" in {
    flagsReg.setN(false)
    flagsReg.getN shouldBe false
  }

  "flags register" should "set p flag correctly" in {
    flagsReg.setP(true)
    flagsReg.getP shouldBe true
  }

  "flags register" should "reset p flag correctly" in {
    flagsReg.setP(false)
    flagsReg.getP shouldBe false
  }

  "flags register" should "set h flag correctly" in {
    flagsReg.setH(true)
    flagsReg.getH shouldBe true
  }

  "flags register" should "reset h flag correctly" in {
    flagsReg.setH(false)
    flagsReg.getH shouldBe false
  }

  "flags register" should "set z flag correctly" in {
    flagsReg.setZ(true)
    flagsReg.getZ shouldBe true
  }

  "flags register" should "reset z flag correctly" in {
    flagsReg.setZ(false)
    flagsReg.getZ shouldBe false
  }

  "flags register" should "set s flag correctly" in {
    flagsReg.setS(true)
    flagsReg.getS shouldBe true
  }

  "flags register" should "reset s flag correctly" in {
    flagsReg.setS(false)
    flagsReg.getS shouldBe false
  }
}
