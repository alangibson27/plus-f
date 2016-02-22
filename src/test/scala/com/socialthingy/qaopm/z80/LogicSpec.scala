package com.socialthingy.qaopm.z80

import org.scalatest.prop.TableDrivenPropertyChecks

class LogicSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val andOperations = Table(
    ("opcode", "register"),
    (0xa0, "b"), (0xa1, "c"), (0xa2, "d"), (0xa3, "e"), (0xa4, "h"), (0xa5, "l")
  )
  
  forAll(andOperations) { (opcode, register) =>
    s"and $register" should "correctly calculate a negative result" in new Machine {
      // given
      registerContainsValue("a", binary("10000100"))
      registerContainsValue(register, binary("10000101"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("10000100")
      registerValue(register) shouldBe binary("10000101")

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe true
      flag("p").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  "and <reg>" should "calculate a zero result when a and <reg> have no shared bits" in new Machine {
    // given
    registerContainsValue("a", binary("10101010"))
    registerContainsValue("b", binary("01010101"))

    nextInstructionIs(0xa0)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000000")
    registerValue("b") shouldBe binary("01010101")

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "and <reg>" should "correctly calculate a result with even parity" in new Machine {
    // given
    registerContainsValue("a", binary("00111000"))
    registerContainsValue("b", binary("00101000"))

    nextInstructionIs(0xa0)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00101000")
    registerValue("b") shouldBe binary("00101000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "and <reg>" should "correctly calculate a result with odd parity" in new Machine {
    // given
    registerContainsValue("a", binary("11111111"))
    registerContainsValue("b", binary("00111000"))

    nextInstructionIs(0xa0)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00111000")
    registerValue("b") shouldBe binary("00111000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "and a with itself" should "not modify the value of the accumulator" in new Machine {
    // given
    registerContainsValue("a", binary("11111111"))

    nextInstructionIs(0xa7)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11111111")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "and n" should "calculate the correct value" in new Machine {
    // given
    registerContainsValue("a", binary("11111111"))

    nextInstructionIs(0xe6, binary("00111000"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00111000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "and (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("11111111"))
    registerContainsValue("hl", 0x4000)

    memory(0x4000) = binary("00111000")

    nextInstructionIs(0xa6)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00111000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  val indexedAndOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0xa6), "ix"),
    ((0xfd, 0xa6), "iy")
  )

  forAll(indexedAndOperations) { (opcode, register) =>
    s"and ($register + d)" should "calculate the correct value" in new Machine {
      // given
      registerContainsValue("a", binary("11111111"))
      registerContainsValue(register, 0xbeef)

      val offset = randomByte
      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("00111000")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00111000")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe true
      flag("p").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  val orOperations = Table(
    ("opcode", "register"),
    (0xb0, "b"), (0xb1, "c"), (0xb2, "d"), (0xb3, "e"), (0xb4, "h"), (0xb5, "l")
  )

  forAll(orOperations) { (opcode, register) =>
    s"or $register" should "correctly calculate a negative result" in new Machine {
      // given
      registerContainsValue("a", binary("10000100"))
      registerContainsValue(register, binary("10000101"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("10000101")
      registerValue(register) shouldBe binary("10000101")

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  "or <reg>" should "give a zero result when <reg> and a are both zero" in new Machine {
    // given
    registerContainsValue("a", 0x00)
    registerContainsValue("b", 0x00)

    nextInstructionIs(0xb0)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x00
    registerValue("b") shouldBe 0x00

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "or <reg>" should "correctly calculate a result with even parity" in new Machine {
    // given
    registerContainsValue("a", binary("00111000"))
    registerContainsValue("b", binary("00101100"))

    nextInstructionIs(0xb0)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00111100")
    registerValue("b") shouldBe binary("00101100")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "or <reg>" should "correctly calculate a result with odd parity" in new Machine {
    // given
    registerContainsValue("a", binary("11111110"))
    registerContainsValue("b", binary("00111000"))

    nextInstructionIs(0xb0)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11111110")
    registerValue("b") shouldBe binary("00111000")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "or a with itself" should "not modify the value of the accumulator" in new Machine {
    // given
    registerContainsValue("a", binary("10101010"))

    nextInstructionIs(0xb7)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("10101010")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "or n" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("11000111"))

    nextInstructionIs(0xf6, binary("00111000"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11111111")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "or (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("11000111"))
    registerContainsValue("hl", 0x4000)

    memory(0x4000) = binary("00111000")

    nextInstructionIs(0xb6)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11111111")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  val indexedOrOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0xb6), "ix"),
    ((0xfd, 0xb6), "iy")
  )

  forAll(indexedOrOperations) { (opcode, register) =>
    s"or ($register + d)" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("a", binary("11000111"))
      registerContainsValue(register, 0xbeef)

      val offset = randomByte
      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("00111000")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("11111111")

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  val xorOperations = Table(
    ("opcode", "register"),
    (0xa8, "b"), (0xa9, "c"), (0xaa, "d"), (0xab, "e"), (0xac, "h"), (0xad, "l")
  )

  forAll(xorOperations) { (opcode, register) =>
    s"xor $register" should "correctly calculate a negative result" in new Machine {
      // given
      registerContainsValue("a", binary("10000100"))
      registerContainsValue(register, binary("00000101"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("10000001")
      registerValue(register) shouldBe binary("00000101")

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  "xor <reg>" should "give a zero result when <reg> and a have the same value" in new Machine {
    // given
    registerContainsValue("a", binary("10101010"))
    registerContainsValue("b", binary("10101010"))

    nextInstructionIs(0xa8)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000000")
    registerValue("b") shouldBe binary("10101010")

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "xor <reg>" should "correctly calcualte a result with even parity" in new Machine {
    // given
    registerContainsValue("a", binary("00111000"))
    registerContainsValue("b", binary("00101100"))

    nextInstructionIs(0xa8)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00010100")
    registerValue("b") shouldBe binary("00101100")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "xor <reg>" should "correctly calculate a result with odd parity" in new Machine {
    // given
    registerContainsValue("a", binary("11111111"))
    registerContainsValue("b", binary("00111000"))

    nextInstructionIs(0xa8)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11000111")
    registerValue("b") shouldBe binary("00111000")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "xor a with itself" should "give a zero result" in new Machine {
    // given
    registerContainsValue("a", binary("10101010"))

    nextInstructionIs(0xaf)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000000")

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "xor n" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("11000111"))

    nextInstructionIs(0xee, binary("00111000"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11111111")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "xor (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("11000111"))
    registerContainsValue("hl", 0x4000)

    memory(0x4000) = binary("00111000")

    nextInstructionIs(0xae)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11111111")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  val indexedXorOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0xae), "ix"),
    ((0xfd, 0xae), "iy")
  )

  forAll(indexedXorOperations) { (opcode, register) =>
    s"xor ($register + d)" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("a", binary("11000111"))
      registerContainsValue(register, 0xbeef)

      val offset = randomByte
      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("00111000")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("11111111")

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }
}
