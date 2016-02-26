package com.socialthingy.qaopm.z80

class BlockOperationSpec extends ProcessorSpec {

  "ldi" should "operate correctly when bc decrements to a non-zero value" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0xbeef)

    memory(0x1000) = 0xba

    nextInstructionIs(0xed, 0xa0)

    // when
    processor.execute()

    // then
    memory(0x2000) shouldBe 0xba
    registerValue("hl") shouldBe 0x1001
    registerValue("de") shouldBe 0x2001
    registerValue("bc") shouldBe 0xbeee

    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
  }

  "ldi" should "operate correctly when bc decrements to zero" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x0001)

    memory(0x1000) = 0xba

    nextInstructionIs(0xed, 0xa0)

    // when
    processor.execute()

    // then
    memory(0x2000) shouldBe 0xba
    registerValue("hl") shouldBe 0x1001
    registerValue("de") shouldBe 0x2001
    registerValue("bc") shouldBe 0x0000

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "ldir" should "operate correctly when bc is greater than 1" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x000a)

    memory(0x1000) = 0xff

    nextInstructionIs(0xed, 0xb0)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000
    memory(0x2000) shouldBe 0xff
    registerValue("hl") shouldBe 0x1001
    registerValue("de") shouldBe 0x2001
    registerValue("bc") shouldBe 0x0009

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "ldir" should "operate correctly when bc is 1" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x0001)

    memory(0x1000) = 0xff

    nextInstructionIs(0xed, 0xb0)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0002
    memory(0x2000) shouldBe 0xff
    registerValue("hl") shouldBe 0x1001
    registerValue("de") shouldBe 0x2001
    registerValue("bc") shouldBe 0x0000

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "ldir" should "operate correctly when bc is 0" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x0000)

    memory(0x1000) = 0xff

    nextInstructionIs(0xed, 0xb0)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000
    memory(0x2000) shouldBe 0xff
    registerValue("hl") shouldBe 0x1001
    registerValue("de") shouldBe 0x2001
    registerValue("bc") shouldBe 0xffff

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "ldd" should "operate correctly when bc decrements to a non-zero value" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0xbeef)

    memory(0x1000) = 0xba

    nextInstructionIs(0xed, 0xa8)

    // when
    processor.execute()

    // then
    memory(0x2000) shouldBe 0xba
    registerValue("hl") shouldBe 0x0fff
    registerValue("de") shouldBe 0x1fff
    registerValue("bc") shouldBe 0xbeee

    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
  }

  "ldd" should "operate correctly when bc decrements to zero" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x0001)

    memory(0x1000) = 0xba

    nextInstructionIs(0xed, 0xa8)

    // when
    processor.execute()

    // then
    memory(0x2000) shouldBe 0xba
    registerValue("hl") shouldBe 0x0fff
    registerValue("de") shouldBe 0x1fff
    registerValue("bc") shouldBe 0x0000

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "lddr" should "operate correctly when bc is greater than 1" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x000a)

    memory(0x1000) = 0xff

    nextInstructionIs(0xed, 0xb8)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000
    memory(0x2000) shouldBe 0xff
    registerValue("hl") shouldBe 0x0fff
    registerValue("de") shouldBe 0x1fff
    registerValue("bc") shouldBe 0x0009

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "lddr" should "operate correctly when bc is equal to 1" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x0001)

    memory(0x1000) = 0xff

    nextInstructionIs(0xed, 0xb8)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0002
    memory(0x2000) shouldBe 0xff
    registerValue("hl") shouldBe 0x0fff
    registerValue("de") shouldBe 0x1fff
    registerValue("bc") shouldBe 0x0000

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "lddr" should "operate correctly when bc is 0" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    registerContainsValue("de", 0x2000)
    registerContainsValue("bc", 0x0000)

    memory(0x1000) = 0xff

    nextInstructionIs(0xed, 0xb8)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000
    memory(0x2000) shouldBe 0xff
    registerValue("hl") shouldBe 0x0fff
    registerValue("de") shouldBe 0x1fff
    registerValue("bc") shouldBe 0xffff

    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "cpi" should "operate correctly with memory equal to value of a and bc greater than 1" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0090)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xa1)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0x008f

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
  }

  "cpi" should "operate correctly with memory equal to a and bc equal to 1" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xa1)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpi" should "operate correctly with memory less than a and half borrow" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = binary("00001000")

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", binary("10000000"))

    nextInstructionIs(0xed, 0xa1)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpi" should "operate correctly with memory greater than a and half borrow" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = binary("00001000")

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", binary("10000000"))

    nextInstructionIs(0xed, 0xa1)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpi" should "operate correctly with memory greater than a and full borrow" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = binary("10000000")

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", binary("00000001"))

    nextInstructionIs(0xed, 0xa1)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpir" should "operate correctly with memory equal to a and bc greater than one" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0090)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xb1)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000

    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0x008f

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
  }

  "cpir" should "operate correctly with memory equal to a and bc equal to one" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xb1)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0002

    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpir" should "operate correctly with memory equal to a and bc equal to zero" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0000)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xb1)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000

    registerValue("hl") shouldBe 0x1001
    registerValue("bc") shouldBe 0xffff

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
  }

  "cpd" should "operate correctly with memory equal to a and bc greater than one" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0090)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xa9)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0x008f

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
  }

  "cpd" should "operate correctly with memory equal to a and bc equal to one" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xa9)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpd" should "operate correctly with memory less than a and half borrow" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = binary("00001000")

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", binary("10000000"))

    nextInstructionIs(0xed, 0xa9)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpd" should "operate correctly with memory greater than a and half borrow" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = binary("00001000")

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", binary("10000000"))

    nextInstructionIs(0xed, 0xa9)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpd" should "operate correctly with memory greater than a and full borrow" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = binary("10000000")

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", binary("00000001"))

    nextInstructionIs(0xed, 0xa9)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpdr" should "operate correctly with memory equal to a and bc greater than one" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0090)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xb9)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000

    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0x008f

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
  }

  "cpdr" should "operate correctly with memory equal to a and bc equal to one" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0001)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xb9)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0002

    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
  }

  "cpdr" should "operate correctly with memory equal to a and bc equal to zero" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    memory(0x1000) = 0xbe

    registerContainsValue("bc", 0x0000)
    registerContainsValue("a", 0xbe)

    nextInstructionIs(0xed, 0xb9)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000

    registerValue("hl") shouldBe 0x0fff
    registerValue("bc") shouldBe 0xffff

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
  }
}
