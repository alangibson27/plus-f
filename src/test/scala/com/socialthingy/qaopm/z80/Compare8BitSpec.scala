package com.socialthingy.qaopm.z80

import com.socialthingy.qaopm.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class Compare8BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val compareRegisterOperations = Table(
    ("opcode", "register"),
    (0xb8, "b"), (0xb9, "c"), (0xba, "d"), (0xbb, "e"), (0xbc, "h"), (0xbd, "l")
  )
  
  forAll(compareRegisterOperations) { (opcode, register) =>
    s"cp $register" should "correctly calculate a positive result with no carries or overflow" in new Machine {
      // given
      registerContainsValue("a", binary("00000100"))
      registerContainsValue(register, binary("00000001"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00000100")
      registerValue(register) shouldBe binary("00000001")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe true
      flag("c").value shouldBe false
    }
  }

  "comparing a to itself" should "give a zero result" in new Machine {
    // given
    registerContainsValue("a", binary("00000001"))

    nextInstructionIs(0xbf)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000001")

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  "cp <reg>" should "correctly calculate a positive result with half borrow" in new Machine {
    // given
    registerContainsValue("a", binary("00010000"))
    registerContainsValue("b", binary("00001000"))

    nextInstructionIs(0xb8)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00010000")
    registerValue("b") shouldBe binary("00001000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  "cp <reg>" should "correctly calculate a negative result with full borrow and overflow" in new Machine {
    // given
    registerContainsValue("a", binary("01101001"))
    registerContainsValue("c", binary("01111001"))

    nextInstructionIs(0xb9)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01101001")
    registerValue("c") shouldBe binary("01111001")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe true
    flag("c").value shouldBe true
  }

  "cp n" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))

    nextInstructionIs(0xfe, binary("00000001"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00001000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  "cp (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    registerContainsValue("hl", 0xbeef)

    memory(0xbeef) = binary("00000001")

    nextInstructionIs(0xbe)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00001000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  val indexedCompareOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0xbe), "ix"),
    ((0xfd, 0xbe), "iy")
  )

  forAll(indexedCompareOperations) { (opcode, register) =>
    s"cp ($register + d)" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("a", binary("00001000"))
      registerContainsValue(register, 0xbeef)

      val offset = randomByte
      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("00000001")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00001000")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe true
      flag("c").value shouldBe false
    }
  }
}
