package com.socialthingy.qaopm.z80

import com.socialthingy.qaopm.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class Add8BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val addOperations = Table(
    ("opcode", "register"),
    (0x80, "b"), (0x81, "c"), (0x82, "d"), (0x83, "e"), (0x84, "h"), (0x85, "l")
  )

  forAll(addOperations) { (opcode, register) =>
    s"add a, $register" should "correctly calculate positive result with no carries or overflow" in new Machine {
      // given
      registerContainsValue("a", binary("00000100"))
      registerContainsValue(register, binary("00000001"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("00000101")
      registerValue(register) shouldBe binary("00000001")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  "add a, a" should "correctly calculate positive result with no carries or overflow" in new Machine {
    // given
    registerContainsValue("a", binary("00000001"))

    nextInstructionIs(0x87)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00000010")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "add operations" should "correctly calculate a positive result with half carry" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    registerContainsValue("b", binary("00001000"))

    nextInstructionIs(0x80)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("00010000")
    registerValue("b") shouldBe binary("00001000")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "add a, a when a = 0" should "give a zero result" in new Machine {
    // given
    registerContainsValue("a", 0)

    nextInstructionIs(0x87)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }
  
  "add operations" should "correctly calculate a negative result with overflow" in new Machine {
    // given
    registerContainsValue("a", binary("01111000"))
    registerContainsValue("c", binary("01101001"))

    nextInstructionIs(0x81)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("11100001")
    registerValue("c") shouldBe binary("01101001")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }
  
  "add operations" should "correctly calculate a result with full carry" in new Machine {
    // given
    registerContainsValue("a", binary("11111000"))
    registerContainsValue("d", binary("01101001"))

    nextInstructionIs(0x82)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01100001")
    registerValue("d") shouldBe binary("01101001")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe true
  }
  
  "add a, n" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))

    nextInstructionIs(0xc6, binary("01000001"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01001001")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }
  
  "add a, (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    registerContainsValue("hl", 0xbeef)

    memory(0xbeef) = binary("01000001")

    nextInstructionIs(0x86)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01001001")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  val indexedAddOperations = Table(
    ("opcode", "indexRegister"),
    ((0xdd, 0x86), "ix"),
    ((0xfd, 0x86), "iy")
  )

  forAll(indexedAddOperations) { (opcode, indexRegister) =>
    s"add a, ($indexRegister + d)" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("a", binary("00001000"))
      registerContainsValue(indexRegister, 0xbeef)

      val offset = randomByte
      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("01000001")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("01001001")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  val adcOperations = Table(
    ("opcode", "register"),
    (0x88, "b"), (0x89, "c"), (0x8a, "d"), (0x8b, "e"), (0x8c, "h"), (0x8d, "l")
  )

  val truthValues = Table("truth", true, false)

  forAll(adcOperations) { (opcode, register) =>
    forAll(truthValues) { carry: Boolean =>
      s"adc a, $register with carry set to $carry" should "calculate the correct result" in new Machine {
        // given
        registerContainsValue("a", binary("00000100"))
        registerContainsValue(register, binary("00000001"))
        flag("c") is carry

        nextInstructionIs(opcode)

        // when
        processor.execute()

        // then
        registerValue("a") shouldBe binary("00000101")+ (if (carry) 1 else 0)
        registerValue(register) shouldBe binary("00000001")

        flag("s").value shouldBe false
        flag("z").value shouldBe false
        flag("h").value shouldBe false
        flag("p").value shouldBe false
        flag("n").value shouldBe false
        flag("c").value shouldBe false
      }
    }
  }

  "adc operations with carry set" should "correctly calculate a result with overflow" in new Machine {
    // given
    registerContainsValue("a", 0xfe)
    registerContainsValue("b", 0x01)
    flag("c") is true

    nextInstructionIs(0x88)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x00
    registerValue("b") shouldBe 0x01

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe true
  }

  "adc a, a with carry set" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", 0x00)
    flag("c") is true

    nextInstructionIs(0x8f)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x01

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "adc a, n with carry set" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    flag("c") is true

    nextInstructionIs(0xce, binary("01000001"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01001010")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "adc a, n with carry reset" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    flag("c") is false

    nextInstructionIs(0xce, binary("01000001"))

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01001001")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "adc a, (hl) with carry set" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("a", binary("00001000"))
    flag("c") is true

    registerContainsValue("hl", 0xa000)
    memory(0xa000) = binary("01000001")

    nextInstructionIs(0x8e)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe binary("01001010")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  val indexedAdcOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x8e), "ix"),
    ((0xfd, 0x8e), "iy")
  )

  forAll(indexedAdcOperations) { (opcode, register) =>
    s"adc a, ($register + d) with carry set" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("a", binary("00001000"))
      flag("c") is true
      registerContainsValue(register, 0xbeef)

      val offset = randomByte

      memory(0xbeef + offset.asInstanceOf[Byte]) = binary("01000001")

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe binary("01001010")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

}