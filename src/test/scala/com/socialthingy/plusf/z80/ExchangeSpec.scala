package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class ExchangeSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  "ex de, hl" should "exchange the values of de and hl" in new Machine {
    // given
    registerContainsValue("de", 0x1234)
    registerContainsValue("hl", 0xabcd)

    nextInstructionIs(0xeb)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x1234
    registerValue("de") shouldBe 0xabcd
  }

  "ex af, af'" should "exchange the values of af and af'" in new Machine {
    // given
    registerContainsValue("af", 0x1234)
    registerContainsValue("af'", 0xabcd)

    nextInstructionIs(0x08)

    // when
    processor.execute()

    // then
    registerValue("af") shouldBe 0xabcd
    registerValue("af'") shouldBe 0x1234
  }

  "exx" should "exchange the values of bc, de and hl with bc', de' and hl' respectively" in new Machine {
    // given
    registerContainsValue("bc", 0x1234)
    registerContainsValue("bc'", 0x4321)

    registerContainsValue("de", 0x5678)
    registerContainsValue("de'", 0x8765)

    registerContainsValue("hl", 0x9abc)
    registerContainsValue("hl'", 0xcba9)

    nextInstructionIs(0xd9)

    // when
    processor.execute()

    // then
    registerValue("bc") shouldBe 0x4321
    registerValue("bc'") shouldBe 0x1234

    registerValue("de") shouldBe 0x8765
    registerValue("de'") shouldBe 0x5678

    registerValue("hl") shouldBe 0xcba9
    registerValue("hl'") shouldBe 0x9abc
  }

  "ex (sp), hl" should "exchange the word at the stack pointer with the value of hl" in new Machine {
    // given
    registerContainsValue("sp", 0xbeef)
    registerContainsValue("hl", 0x1234)

    memory(0xbeef) = 0xba
    memory(0xbef0) = 0xbe

    nextInstructionIs(0xe3)

    // when
    processor.execute()

    // then
    memory(0xbeef) shouldBe 0x34
    memory(0xbef0) shouldBe 0x12
    registerValue("hl") shouldBe 0xbeba
  }

  val indexExchangeOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0xe3), "ix"),
    ((0xfd, 0xe3), "iy")
  )

  forAll(indexExchangeOperations) { (opcode, register) =>
    s"ex (sp), $register" should s"exchange the word at the stack pointer with the value in $register" in new Machine {
      // given
      registerContainsValue("sp", 0xbeef)
      memory(0xbeef) = 0x12
      memory(0xbef0) = 0x34

      registerContainsValue(register, 0xbeba)

      nextInstructionIs(opcode._1, opcode._2)

      // when
      processor.execute()

      // then
      memory(0xbeef) shouldBe 0xba
      memory(0xbef0) shouldBe 0xbe

      registerValue(register) shouldBe 0x3412
    }
  }
}

/*

 */