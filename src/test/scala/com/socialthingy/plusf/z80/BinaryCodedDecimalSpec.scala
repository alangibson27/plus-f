package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec

class BinaryCodedDecimalSpec extends ProcessorSpec {

  "daa" should "correct addition with half carry" in new ArithmeticMachine {
    // given
    add(0x08, 0x18)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x26

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("c").value shouldBe false
  }

  it should "correct addition with full carry" in new ArithmeticMachine {
    // given
    add(0x90, 0x90)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x80

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("c").value shouldBe true
  }

  it should "correct addition with full and half carry" in new ArithmeticMachine {
    // given
    add(0x99, 0x99)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x98

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("c").value shouldBe true
  }

  it should "correct addition with no carries" in new ArithmeticMachine {
    // given
    add(0x09, 0x02)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x11

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("c").value shouldBe false
  }

  it should "correct subtraction with half carry" in new ArithmeticMachine {
    // given
    sub(0x10, 0x08)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x02

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("c").value shouldBe false
  }

  it should "correct subtraction with full carry" in new ArithmeticMachine {
    // given
    sub(0x79, 0x99)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x80

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("c").value shouldBe true
    flag("p").value shouldBe false
  }

  it should "correct subtraction with half and full carry" in new ArithmeticMachine {
    // given
    sub(0x44, 0x88)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x56

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("c").value shouldBe true
    flag("p").value shouldBe true
  }

  it should "correct subtraction with no carries" in new ArithmeticMachine {
    // given
    sub(0x88, 0x44)

    nextInstructionIs(0x27)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x44

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("c").value shouldBe false
    flag("p").value shouldBe true
  }

  trait ArithmeticMachine extends Machine {
    def add(v1: Int, v2: Int): Unit = {
      nextInstructionIs(0x3e, v1)
      processor.execute()
      nextInstructionIs(0xc6, v2)
      processor.execute()
    }

    def sub(v1: Int, v2: Int): Unit = {
      nextInstructionIs(0x3e, v1)
      processor.execute()
      nextInstructionIs(0xd6, v2)
      processor.execute()
    }
  }
}
