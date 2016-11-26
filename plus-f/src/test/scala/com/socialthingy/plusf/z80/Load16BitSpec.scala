package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import com.socialthingy.plusf.util.Word
import org.scalatest.prop.TableDrivenPropertyChecks

class Load16BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val ldSpOperations = Table(
    ("opcode", "register"),
    (List(0xf9), "hl"),
    (List(0xdd, 0xf9), "ix"),
    (List(0xfd, 0xf9), "iy")
  )

  forAll(ldSpOperations) { (opcode, register) =>
    s"ld sp, $register" should "transfer the contents of the register to sp correctly" in new Machine {
      // given
      registerContainsValue(register, 0xbeef)
      opcode.foreach(nextInstructionIs(_))

      // when
      processor.execute()

      // then
      registerValue("sp") shouldBe 0xbeef
    }
  }

  val ldImmediateOperations = Table(
    ("opcode", "register"),
    (List(0x01), "bc"),
    (List(0x11), "de"),
    (List(0x21), "hl"),
    (List(0x31), "sp"),
    (List(0xdd, 0x21), "ix"),
    (List(0xfd, 0x21), "iy")
  )

  forAll(ldImmediateOperations) { (opcode, register) =>
    s"ld $register, nn" should "load the contents of the register correctly" in new Machine {
      // given
      val lowByte = randomByte
      val highByte = randomByte
      opcode.foreach(nextInstructionIs(_))
      nextInstructionIs(lowByte)
      nextInstructionIs(highByte)

      // when
      processor.execute()

      // then
      registerContainsValue(register, Word.from(lowByte, highByte))
    }
  }

  val pushOperations = Table(
    ("opcode", "register"),
    (List(0xf5), "af"),
    (List(0xc5), "bc"),
    (List(0xd5), "de"),
    (List(0xe5), "hl"),
    (List(0xdd, 0xe5), "ix"),
    (List(0xfd, 0xe5), "iy")
  )

  forAll(pushOperations) { (opcode, register) =>
    s"push $register" should "push the register value onto the stack correctly" in new Machine {
      // given
      val lowByte = randomByte
      val highByte = randomByte

      registerContainsValue(register, Word.from(lowByte, highByte))
      registerContainsValue("sp", 0xffff)

      opcode.foreach(nextInstructionIs(_))

      // when
      processor.execute()

      // then
      registerValue("sp") shouldBe 0xfffd
      memory(0xfffe) shouldBe highByte
      memory(0xfffd) shouldBe lowByte
    }
  }

  "push <reg>" should "push the register value onto the stack correctly when the stack pointer is at the bottom of memory" in new Machine {
    // given
    registerContainsValue("sp", 0x0000)
    registerContainsValue("hl", 0xabcd)

    nextInstructionIs(0xe5)

    // when
    processor.execute()

    // then
    memory(0xffff) shouldBe 0xab
    memory(0xfffe) shouldBe 0xcd
  }

  val popOperations = Table(
    ("opcode", "register"),
    (List(0xf1), "af"),
    (List(0xc1), "bc"),
    (List(0xd1), "de"),
    (List(0xe1), "hl"),
    (List(0xdd, 0xe1), "ix"),
    (List(0xfd, 0xe1), "iy")
  )

  forAll(popOperations) { (opcode, register) =>
    s"pop $register" should "pop the register value from the stack correctly" in new Machine {
      // given
      val lowByte = randomByte
      val highByte = randomByte

      memory(0xfff0) = lowByte
      memory(0xfff1) = highByte

      registerContainsValue("sp", 0xfff0)

      opcode.foreach(nextInstructionIs(_))

      // when
      processor.execute()

      // then
      registerValue("sp") shouldBe 0xfff2
      registerValue(register) shouldBe Word.from(lowByte, highByte)
    }
  }

  "pop <reg>" should "pop the register value from the stack correctly when the stack pointer is at the top of memory" in new Machine {
    // given
    memory(0xffff) = 0xab
    memory(0x0000) = 0xcd

    registerContainsValue("sp", 0xffff)

    registerContainsValue("pc", 0x1000)
    nextInstructionIs(0xe1)

    // when
    processor.execute()

    // then
    registerValue("h") shouldBe 0xcd
    registerValue("l") shouldBe 0xab
    registerValue("sp") shouldBe 0x0001
  }

  val ldMemoryFromRegOperations = Table(
    ("opcode", "register"),
    (List(0x22), "hl"),
    (List(0xed, 0x43), "bc"),
    (List(0xed, 0x53), "de"),
    (List(0xed, 0x63), "hl"),
    (List(0xed, 0x73), "sp"),
    (List(0xdd, 0x22), "ix"),
    (List(0xfd, 0x22), "iy")
  )

  forAll(ldMemoryFromRegOperations) { (opcode, register) =>
    s"ld (nn), $register ${opcode.size}" should "load the register value into memory" in new Machine {
      // given
      registerContainsValue(register, 0x1234)
      opcode.foreach(nextInstructionIs(_))
      nextInstructionIs(0xee)
      nextInstructionIs(0xbe)

      // when
      processor.execute()

      // then
      memory(0xbeee) shouldBe 0x34
      memory(0xbeef) shouldBe 0x12
    }
  }

  val ldRegFromMemoryOperations = Table(
    ("opcode", "register"),
    (List(0x2a), "hl"),
    (List(0xed, 0x4b), "bc"),
    (List(0xed, 0x5b), "de"),
    (List(0xed, 0x6b), "hl"),
    (List(0xed, 0x7b), "sp"),
    (List(0xdd, 0x2a), "ix"),
    (List(0xfd, 0x2a), "iy")
  )

  forAll(ldRegFromMemoryOperations) { (opcode, register) =>
    s"ld $register, (nn) ${opcode.size}" should "load the register value from memory" in new Machine {
      // given
      memory(0x1000) = 0x10
      memory(0x1001) = 0x20

      opcode.foreach(nextInstructionIs(_))
      nextInstructionIs(0x00)
      nextInstructionIs(0x10)

      // when
      processor.execute()

      // then
      registerValue(register) shouldBe 0x2010
    }
  }
}
