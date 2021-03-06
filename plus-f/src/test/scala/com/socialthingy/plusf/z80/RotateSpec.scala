package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class RotateSpec extends ProcessorSpec with TableDrivenPropertyChecks {
  
  val rlcaPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry"),
    (binary("10101010"), true,  binary("01010101"), true),
    (binary("10101010"), false, binary("01010101"), true),
    (binary("01010101"), true,  binary("10101010"), false),
    (binary("01010101"), false, binary("10101010"), false)
  )  

  "rlca" should "perform the correct rotations" in new RotatingMachine {
    forAll(rlcaPermutations) { (initialValue, initialCarry, finalValue, finalCarry) =>
      checkRotation(0x07, initialValue, initialCarry, finalValue, finalCarry)
    }
  }

  trait RotatingMachine extends Machine {
    def checkRotation(opcode: Int, initialValue: Int, initialCarry: Boolean, finalValue: Int, finalCarry: Boolean): Unit = {
      // given
      registerContainsValue("a", initialValue)
      flag("c").is(initialCarry)

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe finalValue

      flag("h").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe finalCarry
      flag("f3").value shouldBe (finalValue & binary("00001000")) > 0
      flag("f5").value shouldBe (finalValue & binary("00101000")) > 0
    }

    def checkRegRotation(register: String, opcode: Int, initialValue: Int, initialCarry: Boolean, finalValue: Int, finalCarry: Boolean, s: Boolean, z: Boolean, p: Boolean): Unit = {
      // given
      registerContainsValue(register, initialValue)
      flag("c").is(initialCarry)

      nextInstructionIs(0xcb, opcode)

      // when
      processor.execute()

      // then
      registerValue(register) shouldBe finalValue

      flag("s").value shouldBe s
      flag("z").value shouldBe z
      flag("h").value shouldBe false
      flag("p").value shouldBe p
      flag("n").value shouldBe false
      flag("c").value shouldBe finalCarry
      flag("f3").value shouldBe (finalValue & binary("00001000")) > 0
      flag("f5").value shouldBe (finalValue & binary("00101000")) > 0
    }

    def checkRldRrd(opcode: Int, initialAccumulator: Int, initialMemory: Int, sign: Boolean, zero: Boolean,
                     parity: Boolean, finalAccumulator: Int, finalMemory: Int): Unit = {
      // given
      registerContainsValue("a", initialAccumulator)
      registerContainsValue("hl", 0x4000)
      memory.set(0x4000, initialMemory)

      nextInstructionIs(0xed, opcode)

      // when
      processor.execute()

      // then
      memory.get(0x4000) shouldBe finalMemory
      registerValue("a") shouldBe finalAccumulator

      flag("s").value shouldBe sign
      flag("z").value shouldBe zero
      flag("h").value shouldBe false
      flag("p").value shouldBe parity
      flag("n").value shouldBe false
      flag("f3").value shouldBe (finalAccumulator & binary("00001000")) > 0
      flag("f5").value shouldBe (finalAccumulator & binary("00101000")) > 0
    }
  }

  val rlaPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry"),
    (binary("10101010"), true,  binary("01010101"), true),
    (binary("10101010"), false, binary("01010100"), true),
    (binary("01010101"), true,  binary("10101011"), false),
    (binary("01010101"), false, binary("10101010"), false)
  )

  "rla" should "perform the correct rotations" in new RotatingMachine {
    forAll(rlaPermutations) { (initialValue, initialCarry, finalValue, finalCarry) =>
      checkRotation(0x17, initialValue, initialCarry, finalValue, finalCarry)
    }
  }

  val rrcaPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry"),
    (binary("10101010"), true,  binary("01010101"), false),
    (binary("10101010"), false, binary("01010101"), false),
    (binary("01010101"), true,  binary("10101010"), true),
    (binary("01010101"), false, binary("10101010"), true)
  )

  "rrca" should "perform the correct rotations" in new RotatingMachine {
    forAll(rrcaPermutations) { (initialValue, initialCarry, finalValue, finalCarry) =>
      checkRotation(0x0f, initialValue, initialCarry, finalValue, finalCarry)
    }
  }

  val rraPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry"),
    (binary("10101010"), true,  binary("11010101"), false),
    (binary("10101010"), false, binary("01010101"), false),
    (binary("01010101"), true,  binary("10101010"), true),
    (binary("01010101"), false, binary("00101010"), true)
  )

  "rra" should "perform the correct rotations" in new RotatingMachine {
    forAll(rraPermutations) { (initialValue, initialCarry, finalValue, finalCarry) =>
      checkRotation(0x1f, initialValue, initialCarry, finalValue, finalCarry)
    }
  }

  val rlcOperations = Table(
    ("opcode", "register"),
    (0x00, "b"), (0x01, "c"), (0x02, "d"), (0x03, "e"), (0x04, "h"), (0x05, "l"), (0x07, "a")
  )

  val rlcPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("01010101"), true, false, false, true),
    (binary("10101010"), false, binary("01010101"), true, false, false, true),
    (binary("01010101"), true,  binary("10101010"), false, true, false, true),
    (binary("01010101"), false, binary("10101010"), false, true, false, true),
    (binary("00000000"), false, binary("00000000"), false, false, true, true)
  )

  forAll(rlcOperations) { (opcode, register) =>
    s"rlc $register" should "perform the correct rotations" in new RotatingMachine {
      forAll(rlcPermutations) { (initialValue, initialCarry, finalValue, finalCarry, s, z, p) =>
        checkRegRotation(register, opcode, initialValue, initialCarry, finalValue, finalCarry, s, z, p)
      }
    }
  }

  "rlc (hl)" should "perform the correct rotations" in new Machine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c").is(false)
    memory.set(0x4000, binary("01010101"))

    nextInstructionIs(0xcb, 0x06)

    // when
    processor.execute()

    // then
    memory.get(0x4000) shouldBe binary("10101010")

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe false
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
    flag("f3").value shouldBe true
    flag("f5").value shouldBe true
  }
  
  val indexedRlcOperations = Table(
    ("opcode", "register"),
    (0xdd, "ix"),
    (0xfd, "iy")
  )  
  
  forAll(indexedRlcOperations) { (opcode, register) =>
    s"rlc ($register + d)" should "perform the correct rotations" in new Machine {
      // given
      val offset = randomByte
      val address = 0x4000 + offset.asInstanceOf[Byte]
      registerContainsValue(register, 0x4000)
      flag("c").is(false)
      memory.set(address, binary("01010101"))

      nextInstructionIs(opcode, 0xcb, offset, 0x06)

      // when
      processor.execute()

      // then
      memory.get(address) shouldBe binary("10101010")

      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe false
      flag("f3").value shouldBe true
      flag("f5").value shouldBe true
    }
  }

  val rlOperations = Table(
    ("opcode", "register"),
    (0x10, "b"), (0x11, "c"), (0x12, "d"), (0x13, "e"), (0x14, "h"), (0x15, "l"), (0x17, "a")
  )

  val rlPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("01010101"), true, false, false, true),
    (binary("10101010"), false, binary("01010100"), true, false, false, false),
    (binary("01010101"), true,  binary("10101011"), false, true, false, false),
    (binary("01010101"), false, binary("10101010"), false, true, false, true),
    (binary("00000000"), false, binary("00000000"), false, false, true, true)
  )

  forAll(rlOperations) { (opcode, register) =>
    s"rl $register" should "perform the correct rotations" in new RotatingMachine {
      forAll(rlPermutations) { (initialValue, initialCarry, finalValue, finalCarry, s, z, p) =>
        checkRegRotation(register, opcode, initialValue, initialCarry, finalValue, finalCarry, s, z, p)
      }
    }
  }

  "rl (hl)" should "perform the correct rotations" in new Machine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c").is(false)
    memory.set(0x4000, binary("10101010"))

    nextInstructionIs(0xcb, 0x16)

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

  val indexedRlOperations = Table(
    ("opcode", "register"),
    (0xdd, "ix"),
    (0xfd, "iy")
  )

  forAll(indexedRlOperations) { (opcode, register) =>
    s"rl ($register + d)" should "perform the correct rotations" in new Machine {
      // given
      val offset = randomByte
      val address = 0x4000 + offset.asInstanceOf[Byte]
      registerContainsValue(register, 0x4000)
      flag("c").is(false)
      memory.set(address, binary("10101010"))

      nextInstructionIs(opcode, 0xcb, offset, 0x16)

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

  val rrcOperations = Table(
    ("opcode", "register"),
    (0x08, "b"), (0x09, "c"), (0x0a, "d"), (0x0b, "e"), (0x0c, "h"), (0x0d, "l"), (0x0f, "a")
  )

  val rrcPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("01010101"), false, false, false, true),
    (binary("10101010"), false, binary("01010101"), false, false, false, true),
    (binary("01010101"), true,  binary("10101010"), true, true, false, true),
    (binary("01010101"), false, binary("10101010"), true, true, false, true),
    (binary("00000000"), false, binary("00000000"), false, false, true, true)
  )

  forAll(rrcOperations) { (opcode, register) =>
    s"rrc $register" should "perform the correct rotations" in new RotatingMachine {
      forAll(rrcPermutations) { (initialValue, initialCarry, finalValue, finalCarry, s, z, p) =>
        checkRegRotation(register, opcode, initialValue, initialCarry, finalValue, finalCarry, s, z, p)
      }
    }
  }

  "rrc (hl)" should "perform the correct rotations" in new Machine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c").is(false)
    memory.set(0x4000, binary("10101010"))

    nextInstructionIs(0xcb, 0x0e)

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

  val indexedRrcOperations = Table(
    ("opcode", "register"),
    (0xdd, "ix"),
    (0xfd, "iy")
  )

  forAll(indexedRrcOperations) { (opcode, register) =>
    s"rrc ($register + d)" should "perform the correct rotations" in new Machine {
      // given
      val offset = randomByte
      val address = 0x4000 + offset.asInstanceOf[Byte]
      registerContainsValue(register, 0x4000)
      flag("c").is(false)
      memory.set(address, binary("10101010"))

      nextInstructionIs(opcode, 0xcb, offset, 0x0e)

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

  val rrOperations = Table(
    ("opcode", "register"),
    (0x18, "b"), (0x19, "c"), (0x1a, "d"), (0x1b, "e"), (0x1c, "h"), (0x1d, "l"), (0x1f, "a")
  )

  val rrPermutations = Table(
    ("initialValue", "initialCarry", "finalValue", "finalCarry", "s", "z", "p"),
    (binary("10101010"), true,  binary("11010101"), false, true, false, false),
    (binary("10101010"), false, binary("01010101"), false, false, false, true),
    (binary("01010101"), true,  binary("10101010"), true, true, false, true),
    (binary("01010101"), false, binary("00101010"), true, false, false, false),
    (binary("00000000"), false, binary("00000000"), false, false, true, true)
  )

  forAll(rrOperations) { (opcode, register) =>
    s"rr $register" should "perform the correct rotations" in new RotatingMachine {
      forAll(rrPermutations) { (initialValue, initialCarry, finalValue, finalCarry, s, z, p) =>
        checkRegRotation(register, opcode, initialValue, initialCarry, finalValue, finalCarry, s, z, p)
      }
    }
  }

  "rr (hl)" should "perform the correct rotations" in new Machine {
    // given
    registerContainsValue("hl", 0x4000)
    flag("c").is(true)
    memory.set(0x4000, binary("10101010"))

    nextInstructionIs(0xcb, 0x1e)

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

  val indexedRrOperations = Table(
    ("opcode", "register"),
    (0xdd, "ix"),
    (0xfd, "iy")
  )

  forAll(indexedRrOperations) { (opcode, register) =>
    s"rr ($register + d)" should "perform the correct rotations" in new Machine {
      // given
      val offset = randomByte
      val address = 0x4000 + offset.asInstanceOf[Byte]
      registerContainsValue(register, 0x4000)
      flag("c").is(true)
      memory.set(address, binary("10101010"))

      nextInstructionIs(opcode, 0xcb, offset, 0x1e)

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

  val rldOperations = Table(
    ("initialAccumulator", "initialMemory", "sign", "zero", "parity", "finalAccumulator", "finalMemory"),
    (0xab, 0xcd, true, false, true, 0xac, 0xdb),
    (0xff, 0xef, true, false, false, 0xfe, 0xff),
    (0x00, 0x00, false, true, true, 0x00, 0x00)
  )

  s"rld" should "perform the correct rotations" in new RotatingMachine {
    forAll(rldOperations) { (initialAccumulator, initialMemory, sign, zero, parity, finalAccumulator, finalMemory) =>
      checkRldRrd(0x6f, initialAccumulator, initialMemory, sign, zero, parity, finalAccumulator, finalMemory)
    }
  }

  val rrdOperations = Table(
    ("initialAccumulator", "initialMemory", "sign", "zero", "parity", "finalAccumulator", "finalMemory"),
    (0xab, 0xcd, true, false, false, 0xad, 0xbc),
    (0xff, 0xfe, true, false, false, 0xfe, 0xff),
    (0x00, 0x00, false, true, true, 0x00, 0x00)
  )

  s"rrd" should "perform the correct rotations" in new RotatingMachine {
    forAll(rrdOperations) { (initialAccumulator, initialMemory, sign, zero, parity, finalAccumulator, finalMemory) =>
      checkRldRrd(0x67, initialAccumulator, initialMemory, sign, zero, parity, finalAccumulator, finalMemory)
    }
  }
}
