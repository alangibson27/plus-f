package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class ShiftSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  trait ShiftingMachine extends Machine {
    def checkRegShift(opcode: Int, register: String, initialAcc: Int, initialCarry: Boolean, finalAcc: Int, finalCarry: Boolean, s: Boolean, z: Boolean, p: Boolean): Unit = {
      // given
      registerContainsValue(register, initialAcc)
      flag("c") is initialCarry

      nextInstructionIs(0xcb, opcode)

      // when
      processor.execute()

      // then
      registerValue(register) shouldBe finalAcc

      flag("s").value shouldBe s
      flag("z").value shouldBe z
      flag("h").value shouldBe false
      flag("p").value shouldBe p
      flag("n").value shouldBe false
      flag("c").value shouldBe finalCarry
      flag("f3").value shouldBe (finalAcc & binary("00001000")) > 0
      flag("f5").value shouldBe (finalAcc & binary("00101000")) > 0
    }
  }

  val slaOperations = Table(
    ("opcode", "register"),
    (0x20, "b"), (0x21, "c"), (0x22, "d"), (0x23, "e"), (0x24, "h"), (0x25, "l"), (0x27, "a")
  )
  
  val slaPermutations = Table(
    ("initialAcc", "initialCarry", "finalAcc", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("01010100"), true, false, false, false),
    (binary("10101010"), false, binary("01010100"), true, false, false, false),
    (binary("01010101"), true,  binary("10101010"), false, true, false, true),
    (binary("01010101"), false, binary("10101010"), false, true, false, true),
    (binary("00000000"), false, binary("00000000"), false, false, true, true)
  )
  
  forAll(slaOperations) { (opcode, register) =>
    forAll(slaPermutations) { (initialAcc, initialCarry, finalAcc, finalCarry, s, z, p) =>
      s"sla $register" should s"correctly shift value $initialAcc with carry $initialCarry" in new ShiftingMachine {
        checkRegShift(opcode, register, initialAcc, initialCarry, finalAcc, finalCarry, s, z, p)
      }
    }
  }

  "sla (hl)" should "shift correctly" in new ShiftingMachine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c") is false
    memory.set(0x4000, binary("10101010"))

    nextInstructionIs(0xcb, 0x26)

    // when
    processor.execute()

    // then
    memory.get(0x4000) shouldBe binary("01010100")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe true
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }

  val indexRegisters = Table(
    ("prefix", "register"),
    (0xdd, "ix"), (0xfd, "iy")
  )

  forAll(indexRegisters) { (prefix, register) =>
    s"sla ($register)" should "shift correctly" in new ShiftingMachine {
      // given
      val offset = randomByte
      val address = 0x4000 + offset.asInstanceOf[Byte]
      registerContainsValue(register, 0x4000)
      flag("c") is false
      memory.set(address, binary("10101010"))

      nextInstructionIs(prefix, 0xcb, offset, 0x26)

      // when
      processor.execute()

      // then
      memory.get(address) shouldBe binary("01010100")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe true
      flag("f3").value shouldBe false
      flag("f5").value shouldBe false
    }
  }

  val sraOperations = Table(
    ("opcode", "register"),
    (0x28, "b"), (0x29, "c"), (0x2a, "d"), (0x2b, "e"), (0x2c, "h"), (0x2d, "l"), (0x2f, "a")
  )

  val sraPermutations = Table(
    ("initialAcc", "initialCarry", "finalAcc", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("11010101"), false, true, false, false),
    (binary("10101010"), false, binary("11010101"), false, true, false, false),
    (binary("01010101"), true,  binary("00101010"), true, false, false, false),
    (binary("01010101"), false, binary("00101010"), true, false, false, false),
    (binary("00000000"), false, binary("00000000"), false, false, true, true)
  )

  forAll(sraOperations) { (opcode, register) =>
    forAll(sraPermutations) { (initialAcc, initialCarry, finalAcc, finalCarry, s, z, p) =>
      s"sra $register" should s"correctly shift value $initialAcc with carry $initialCarry" in new ShiftingMachine {
        checkRegShift(opcode, register, initialAcc, initialCarry, finalAcc, finalCarry, s, z, p)
      }
    }
  }

  "sra (hl)" should "shift correctly" in new Machine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c") is false
    memory.set(0x4000, binary("10101010"))

    nextInstructionIs(0xcb, 0x2e)

    // when
    processor.execute()

    // then
    memory.get(0x4000) shouldBe binary("11010101")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }

  forAll(indexRegisters) { (prefix, register) =>
    s"sra ($register)" should "shift correctly" in new Machine {
      // given
      val offset = randomByte
      val address = 0x4000 + offset.asInstanceOf[Byte]
      registerContainsValue(register, 0x4000)
      flag("c") is false
      memory.set(address, binary("10101010"))

      nextInstructionIs(prefix, 0xcb, offset, 0x2e)

      // when
      processor.execute()

      // then
      memory.get(address) shouldBe binary("11010101")

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe false
      flag("f3").value shouldBe false
      flag("f5").value shouldBe false
    }
  }

  val srlOperations = Table(
    ("opcode", "register"),
    (0x38, "b"), (0x39, "c"), (0x3a, "d"), (0x3b, "e"), (0x3c, "h"), (0x3d, "l"), (0x3f, "a")
  )

  val srlPermutations = Table(
    ("initialAcc", "initialCarry", "finalAcc", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("01010101"), false, false, false, true),
    (binary("10101010"), false, binary("01010101"), false, false, false, true),
    (binary("01010101"), true,  binary("00101010"), true, false, false, false),
    (binary("01010101"), false, binary("00101010"), true, false, false, false),
    (binary("00000000"), false, binary("00000000"), false, false, true, true)
  )

  forAll(srlOperations) { (opcode, register) =>
    forAll(srlPermutations) { (initialAcc, initialCarry, finalAcc, finalCarry, s, z, p) =>
      s"srl $register" should s"correctly shift value $initialAcc with carry $initialCarry" in new ShiftingMachine {
        checkRegShift(opcode, register, initialAcc, initialCarry, finalAcc, finalCarry, s, z, p)
      }
    }
  }

  "srl (hl)" should "shift correctly" in new Machine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c") is false
    memory.set(0x4000, binary("10101010"))

    nextInstructionIs(0xcb, 0x3e)

    // when
    processor.execute()

    // then
    memory.get(0x4000) shouldBe binary("01010101")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }

  forAll(indexRegisters) { (prefix, register) =>
    s"srl ($register)" should "shift correctly" in new Machine {
      // given
      val offset = randomByte
      val address = 0x4000 + offset.asInstanceOf[Byte]
      registerContainsValue(register, 0x4000)
      flag("c") is false
      memory.set(address, binary("10101010"))

      nextInstructionIs(prefix, 0xcb, offset, 0x3e)

      // when
      processor.execute()

      // then
      memory.get(address) shouldBe binary("01010101")

      flag("s").value shouldBe false
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe false
      flag("f3").value shouldBe false
      flag("f5").value shouldBe false
    }
  }

  val sllOperations = Table(
    ("opcode", "register"),
    (0x30, "b"), (0x31, "c"), (0x32, "d"), (0x33, "e"), (0x34, "h"), (0x35, "l"), (0x37, "a")
  )

  val sllPermutations = Table(
    ("initialAcc", "initialCarry", "finalAcc", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("01010101"), true, false, false, true),
    (binary("10101010"), false, binary("01010101"), true, false, false, true),
    (binary("01010101"), true,  binary("10101011"), false, true, false, false),
    (binary("01010101"), false, binary("10101011"), false, true, false, false),
    (binary("00000000"), false, binary("00000001"), false, false, false, false)
  )

  forAll(sllOperations) { (opcode, register) =>
    forAll(sllPermutations) { (initialAcc, initialCarry, finalAcc, finalCarry, s, z, p) =>
      s"sll $register" should s"correctly shift value $initialAcc with carry $initialCarry" in new ShiftingMachine {
        checkRegShift(opcode, register, initialAcc, initialCarry, finalAcc, finalCarry, s, z, p)
      }
    }
  }

  "sll (hl)" should "shift correctly" in new Machine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c") is false
    memory.set(0x4000, binary("10101010"))

    nextInstructionIs(0xcb, 0x36)

    // when
    processor.execute()

    // then
    memory.get(0x4000) shouldBe binary("01010101")

    flag("s").value shouldBe false
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe true
    flag("f3").value shouldBe false
    flag("f5").value shouldBe false
  }
}