package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class Subtract8BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {
  
  val subtractOperations = Table(
    ("opcode", "register"),
    (0x90, "b"), (0x91, "c"), (0x92, "d"), (0x93, "e"), (0x94, "h"), (0x95, "l")
  )
  
  forAll(subtractOperations) { (opcode, register) =>
    s"sub a, $register" should "correctly calculate a positive result with no carries or overflow" in new Machine {
      // given
      registerContainsValue("a", binary("00000100"))
      registerContainsValue(register, binary("00000001"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00000011")
      registerValue(register) shouldBe binary("00000001")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe true
      flag("c").value shouldBe false
    }
  }

  "sub a, a" should "return a zero result" in new Machine {
    // given
    registerContainsValue("a", binary("00000001"))

    nextInstructionIs(0x97)

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
  }

  "sub a, <reg>" should "correctly calculate a positive result with half borrow" in new Machine {
    // given
    registerContainsValue("a", binary("00010000"))
    registerContainsValue("b", binary("00001000"))

    nextInstructionIs(0x90)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00001000")
    registerValue("b") shouldBe binary("00001000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  "sub a, <reg>" should "correctly calculate a negative result with full borrow and overflow" in new Machine {
    // given
    registerContainsValue("a", binary("01101001"))
    registerContainsValue("c", binary("01111001"))

    nextInstructionIs(0x91)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11110000")
    registerValue("c") shouldBe binary("01111001")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
    flag("c").value shouldBe true
  }

  "sub a, n" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))

    nextInstructionIs(0xd6, binary("00000001"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000111")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  "sub a, (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    registerContainsValue("hl", 0xbeef)

    memory(0xbeef) = binary("00000001")

    nextInstructionIs(0x96)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000111")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  val indexedSubOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x96), "ix"),
    ((0xfd, 0x96), "iy")
  )

  forAll(indexedSubOperations) { (opcode, register) =>
    s"sub a, ($register + d)" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("a", binary("00001000"))
      registerContainsValue(register, 0xbeef)

      val offset = randomByte

      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("00000001")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00000111")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe true
      flag("c").value shouldBe false
    }
  }

  val sbcOperations = Table(
    ("opcode", "register"),
    (0x98, "b"), (0x99, "c"), (0x9a, "d"), (0x9b, "e"), (0x9c, "h"), (0x9d, "l")
  )

  val truthValues = Table("value", true, false)

  forAll(sbcOperations) { (opcode, register) =>
    forAll(truthValues) { carry =>
      s"sbc a, $register with carry $carry" should "calculate the correct result" in new Machine {
        // given
        registerContainsValue("a", binary("00001000"))
        registerContainsValue(register, binary("00000001"))
        flag("c") is carry

        nextInstructionIs(opcode)

        // when
        processor.execute()

        // then
        registerValue("a") shouldBe binary("00000111") - (if (carry) 1 else 0)
        registerValue(register) shouldBe binary("00000001")

        flag("s").value shouldBe false
        flag("z").value shouldBe false
        flag("h").value shouldBe false
        flag("p").value shouldBe false
        flag("n").value shouldBe true
        flag("c").value shouldBe false
      }
    }
  }

  "sbc a, <reg> with carry set" should "correctly calculate a result with overflow" in new Machine {
    // given
    registerContainsValue("a", 0x01)
    registerContainsValue("b", 0x01)
    flag("c") is true

    nextInstructionIs(0x98)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0xff
    registerValue("b") shouldBe 0x01

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe true
    flag("c").value shouldBe true
  }

  "sbc a, a with carry set" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", 0x01)
    flag("c") is true

    nextInstructionIs(0x9f)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0xff

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe true
    flag("c").value shouldBe true
  }

  "sbc a, n with carry set" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    flag("c") is true

    nextInstructionIs(0xde, binary("00000001"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000110")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  "sbc a, n with carry reset" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    flag("c") is false

    nextInstructionIs(0xde, binary("00000001"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000111")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  "sbc a, (hl) with carry set" should "calculate the correct value" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    flag("c") is true

    registerContainsValue("hl", 0xa000)
    memory(0xa000) = binary("00000001")

    nextInstructionIs(0x9e)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000110")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  val indexedSbcOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x9e), "ix"),
    ((0xfd, 0x9e), "iy")
  )

  forAll(indexedSbcOperations) { (opcode, register) =>
    s"sbc a, ($register + d) with carry set" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("a", binary("00001000"))
      flag("c") is true
      registerContainsValue(register, 0xbeef)

      val offset = randomByte

      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("00000001")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00000110")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe true
      flag("c").value shouldBe false
    }
  }

  val subIndexed8RegOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x94), "ixh"),
    ((0xdd, 0x95), "ixl"),
    ((0xfd, 0x94), "iyh"),
    ((0xfd, 0x95), "iyl")
  )

  forAll(subIndexed8RegOperations) { (opcode, register) =>
    s"sub a, $register" should "correctly calculate a positive result with no carries or overflow" in new Machine {
      // given
      registerContainsValue("a", binary("00000100"))
      registerContainsValue(register, binary("00000001"))

      nextInstructionIs(opcode._1, opcode._2)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00000011")
      registerValue(register) shouldBe binary("00000001")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe true
      flag("c").value shouldBe false
    }
  }

  val sbcIndexed8RegOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x9c), "ixh"),
    ((0xdd, 0x9d), "ixl"),
    ((0xfd, 0x9c), "iyh"),
    ((0xfd, 0x9d), "iyl")
  )

  forAll(sbcIndexed8RegOperations) { (opcode, register) =>
    forAll(truthValues) { carry: Boolean =>
      s"sbc a, $register with carry $carry" should "calculate the correct result" in new Machine {
        // given
        registerContainsValue("a", binary("00001000"))
        registerContainsValue(register, binary("00000001"))
        flag("c") is carry

        nextInstructionIs(opcode._1, opcode._2)

        // when
        processor.execute()

        // then
        registerValue("a") shouldBe binary("00000111") - (if (carry) 1 else 0)
        registerValue(register) shouldBe binary("00000001")

        flag("s").value shouldBe false
        flag("z").value shouldBe false
        flag("h").value shouldBe false
        flag("p").value shouldBe false
        flag("n").value shouldBe true
        flag("c").value shouldBe false
      }
    }
  }
}
