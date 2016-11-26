package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec

class AccumulatorOpsSpec extends ProcessorSpec {
  "cpl" should "complement the accumulator" in new Machine {
    // given
    registerContainsValue("a", binary("01010101"))
    nextInstructionIs(0x2f)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("10101010")

    flag("h").value shouldBe true
    flag("n").value shouldBe true
    flag("f3").value shouldBe true
    flag("f5").value shouldBe true
  }

  "neg" should "negate 0" in new Machine {
    // given
    registerContainsValue("a", binary("00000000"))
    nextInstructionIs(0xed, 0x44)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000000")

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }

  it should "negate a positive number" in new Machine {
    // given
    registerContainsValue("a", binary("00000001"))
    nextInstructionIs(0xed, 0x44)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11111111")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe true
    flag("f3").value shouldBe true
    flag("f5").value shouldBe true
  }

  it should "negate a negative number" in new Machine {
    // given
    registerContainsValue("a", binary("10000001"))
    nextInstructionIs(0xed, 0x44)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01111111")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe true
    flag("f3").value shouldBe true
    flag("f5").value shouldBe true
  }

  it should "negate 0x80" in new Machine {
    // given
    registerContainsValue("a", 0x80)
    nextInstructionIs(0xed, 0x44)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x80

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
    flag("c").value shouldBe true
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }
}
