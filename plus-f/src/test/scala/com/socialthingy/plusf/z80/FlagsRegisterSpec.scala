package com.socialthingy.plusf.z80

import org.scalatest.{Matchers, FlatSpec}

import com.socialthingy.plusf.z80.FlagsRegister.Flag

class FlagsRegisterSpec extends FlatSpec with Matchers {

  val flagsReg = new FlagsRegister

  "flags register" should "set c flag correctly" in {
    flagsReg.set(Flag.C, true)
    flagsReg.get(Flag.C) shouldBe true
  }

  "flags register" should "reset c flag correctly" in {
    flagsReg.set(Flag.C, false)
    flagsReg.get(Flag.C) shouldBe false
  }

  "flags register" should "set n flag correctly" in {
    flagsReg.set(Flag.N, true)
    flagsReg.get(Flag.N) shouldBe true
  }

  "flags register" should "reset n flag correctly" in {
    flagsReg.set(Flag.N, false)
    flagsReg.get(Flag.N) shouldBe false
  }

  "flags register" should "set p flag correctly" in {
    flagsReg.set(Flag.P, true)
    flagsReg.get(Flag.P) shouldBe true
  }

  "flags register" should "reset p flag correctly" in {
    flagsReg.set(Flag.P, false)
    flagsReg.get(Flag.P) shouldBe false
  }

  "flags register" should "set h flag correctly" in {
    flagsReg.set(Flag.H, true)
    flagsReg.get(Flag.H) shouldBe true
  }

  "flags register" should "reset h flag correctly" in {
    flagsReg.set(Flag.H, false)
    flagsReg.get(Flag.H) shouldBe false
  }

  "flags register" should "set z flag correctly" in {
    flagsReg.set(Flag.Z, true)
    flagsReg.get(Flag.Z) shouldBe true
  }

  "flags register" should "reset z flag correctly" in {
    flagsReg.set(Flag.Z, false)
    flagsReg.get(Flag.Z) shouldBe false
  }

  "flags register" should "set s flag correctly" in {
    flagsReg.set(Flag.S, true)
    flagsReg.get(Flag.S) shouldBe true
  }

  "flags register" should "reset s flag correctly" in {
    flagsReg.set(Flag.S, false)
    flagsReg.get(Flag.S) shouldBe false
  }
}
