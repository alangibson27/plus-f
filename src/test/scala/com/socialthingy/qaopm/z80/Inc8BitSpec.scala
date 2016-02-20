package com.socialthingy.qaopm.z80

import org.scalatest.prop.TableDrivenPropertyChecks

class Inc8BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val incOperations = Table(
    ("opcode", "register"),
    (0x3c, "a"), (0x04, "b"), (0x0c, "c"), (0x14, "d"), (0x1c, "e"), (0x24, "h"), (0x2c, "l")
  )

  forAll(incOperations) { (opcode, register) =>
    s"inc $register" should "correctly calculate a negative result" in new Machine {
      // given
      registerContainsValue(register, binary("10000001"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue(register).asInstanceOf[Byte] shouldBe -126

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
    }
  }

  "inc <reg>" should "correctly calculate a zero result" in new Machine {
    // given
    registerContainsValue("a", binary("11111111"))

    nextInstructionIs(0x3c)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  "inc <reg>" should "correctly calculate a result with overflow" in new Machine {
    // given
    registerContainsValue("b", binary("01111111"))

    nextInstructionIs(0x04)

    // when
    processor.execute()

    // then
    registerValue("b") shouldBe binary("10000000")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
  }

  "inc (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("hl", 0xbabe)
    memory(0xbabe) = binary("00000001")

    nextInstructionIs(0x34)

    // when
    processor.execute()

    // then
    memory(0xbabe) shouldBe binary("00000010")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
  }

  val indexedIncOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x34), "ix"),
    ((0xfd, 0x34), "iy")
  )

  forAll(indexedIncOperations) { (opcode, register) =>
    s"inc ($register + d)" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue(register, 0xbeef)
      val offset = randomByte
      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("00111000")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      memory(0xbeef + offset.asInstanceOf[Byte]) shouldBe binary("00111001")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
    }
  }
}
