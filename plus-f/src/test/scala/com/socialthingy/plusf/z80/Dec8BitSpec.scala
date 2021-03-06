package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class Dec8BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val decOperations = Table(
    ("opcode", "register"),
    (0x3d, "a"), (0x05, "b"), (0x0d, "c"), (0x15, "d"), (0x1d, "e"), (0x25, "h"), (0x2d, "l")
  )
  
  forAll(decOperations) { (opcode, register) =>
    s"dec $register" should "correctly calculate a negative result" in new Machine {
      // given
      registerContainsValue(register, binary("10000001"))

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue(register).asInstanceOf[Byte] shouldBe -128

      flag("s") is true
      flag("z") is false
      flag("h") is false
      flag("p") is false
      flag("n") is true
      flag("f3").value shouldBe false
      flag("f5").value shouldBe false
    }
  }

  "dec <reg>" should "correctly calculate a zero result" in new Machine {
    // given
    registerContainsValue("a", binary("00000001"))

    nextInstructionIs(0x3d)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0

    flag("s") is false
    flag("z") is true
    flag("h") is false
    flag("p") is false
    flag("n") is true
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }

  "dec <reg>" should "correctly calculate a result with underflow" in new Machine {
    // given
    registerContainsValue("b", binary("10000000"))

    nextInstructionIs(0x05)

    // when
    processor.execute()

    // then
    registerValue("b") shouldBe binary("01111111")

    flag("s") is false
    flag("z") is false
    flag("h") is true
    flag("p") is true
    flag("n") is true
    flag("f3").value shouldBe true
    flag("f5").value shouldBe true
  }

  "dec (hl)" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("hl", 0xbabe)
    memory.set(0xbabe, binary("00000010"))

    nextInstructionIs(0x35)

    // when
    processor.execute()

    // then
    memory.get(0xbabe) shouldBe binary("00000001")

    flag("s") is false
    flag("z") is false
    flag("h") is false
    flag("p") is false
    flag("n") is true
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }

  val indexedDecIndirectOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x35), "ix"),
    ((0xfd, 0x35), "iy")
  )

  forAll(indexedDecIndirectOperations) { (opcode, register) =>
    s"dec ($register + d)" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue(register, 0xbeef)
      val offset = randomByte
      memory.set(0xbeef + offset.asInstanceOf[Byte], binary("00111000"))

      nextInstructionIs(opcode._1, opcode._2, offset)

      // when
      processor.execute()

      // then
      memory.get(0xbeef + offset.asInstanceOf[Byte]) shouldBe binary("00110111")

      flag("s") is false
      flag("z") is false
      flag("h") is false
      flag("p") is false
      flag("n") is true
      flag("f3").value shouldBe false
      flag("f5").value shouldBe true
    }
  }

  val indexedDecImmediateOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0x25), "ixh"),
    ((0xfd, 0x25), "iyh"),
    ((0xdd, 0x2d), "ixl"),
    ((0xfd, 0x2d), "iyl")
  )

  forAll(indexedDecImmediateOperations) { (opcode, register) =>
    s"dec $register" should "correctly increment the register value" in new Machine {
      // given
      registerContainsValue(register, binary("10000001"))

      nextInstructionIs(opcode._1, opcode._2)

      // when
      processor.execute()

      // then
      registerValue(register).asInstanceOf[Byte] shouldBe -128

      flag("s") is true
      flag("z") is false
      flag("h") is false
      flag("p") is false
      flag("n") is true
      flag("f3").value shouldBe false
      flag("f5").value shouldBe false
    }
  }

}
