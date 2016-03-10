package com.socialthingy.qaopm.z80

import com.socialthingy.qaopm.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.util.Random

class Load8BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val regRegOperations = Table(
    ("opcode", "source", "dest"),
    (0x7f, "a", "a"), (0x78, "a", "b"), (0x79, "a", "c"), (0x7a, "a", "d"), (0x7b, "a", "e"), (0x7c, "a", "h"),
    (0x7d, "a", "l"),

    (0x47, "b", "a"), (0x40, "b", "b"), (0x41, "b", "c"), (0x42, "b", "d"), (0x43, "b", "e"), (0x44, "b", "h"),
    (0x45, "b", "l"),

    (0x4f, "c", "a"), (0x48, "c", "b"), (0x49, "c", "c"), (0x4a, "c", "d"), (0x4b, "c", "e"), (0x4c, "c", "h"),
    (0x4d, "c", "l"),

    (0x57, "d", "a"), (0x50, "d", "b"), (0x51, "d", "c"), (0x52, "d", "d"), (0x53, "d", "e"), (0x54, "d", "h"),
    (0x55, "d", "l"),

    (0x5f, "e", "a"), (0x58, "e", "b"), (0x59, "e", "c"), (0x5a, "e", "d"), (0x5b, "e", "e"), (0x5c, "e", "h"),
    (0x5d, "e", "l"),

    (0x67, "h", "a"), (0x60, "h", "b"), (0x61, "h", "c"), (0x62, "h", "d"), (0x63, "h", "e"), (0x64, "h", "h"),
    (0x65, "h", "l"),

    (0x6f, "l", "a"), (0x68, "l", "b"), (0x69, "l", "c"), (0x6a, "l", "d"), (0x6b, "l", "e"), (0x6c, "l", "h"),
    (0x6d, "l", "l")
  )

  forAll(regRegOperations) { (opcode, dest, source) =>
    s"ld $dest, $source immediate" should "transfer value from register to register correctly" in new Machine {
      // given
      registerContainsValue(source, 0xff)
      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe 0x0001
      registerValue(source) shouldBe 0xff
      registerValue(dest) shouldBe 0xff
    }
  }

  val regRegIndirectOperations = Table(
    ("opcode", "dest", "source"),
    (0x7e, "a", "hl"), (0x0a, "a", "bc"), (0x1a, "a", "de"),
    (0x46, "b", "hl"), (0x4e, "c", "hl"), (0x56, "d", "hl"), (0x5e, "e", "hl"), (0x66, "h", "hl"),
    (0x6e, "l", "hl")
  )

  forAll(regRegIndirectOperations) { (opcode, dest, source) =>
    s"ld $dest, ($source)" should "transfer value from referenced memory to register correctly" in new Machine {
      // given
      registerContainsValue(source, 0xa0a0)
      nextInstructionIs(opcode)
      memory(0xa0a0) = 0xaa

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe 0x0001
      registerValue(dest) shouldBe 0xaa
    }
  }

  val regIndirectRegOperations = Table(
    ("opcode", "dest", "source"),
    (0x77, "hl", "a"), (0x70, "hl", "b"), (0x71, "hl", "c"), (0x72, "hl", "d"), (0x73, "hl", "e"),
    (0x02, "bc", "a"),
    (0x12, "de", "a")
  )

  forAll(regIndirectRegOperations) { (opcode, dest, source) =>
    s"ld ($dest), $source" should "transfer value from register to referenced memory correctly" in new Machine {
      // given
      registerContainsValue(source, 0xbb)
      registerContainsValue(dest, 0xb0c0)
      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe 0x0001
      memory(0xb0c0) shouldBe 0xbb
    }
  }

  "ld (hl), l" should "transfer value from l register to referenced memory correctly" in new Machine {
    // given
    registerContainsValue("hl", 0xb0c0)
    nextInstructionIs(0x75)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0001
    memory(0xb0c0) shouldBe 0xc0
  }

  "ld (hl), h" should "transfer value from l register to referenced memory correctly" in new Machine {
    // given
    registerContainsValue("hl", 0xb0c0)
    nextInstructionIs(0x74)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0001
    memory(0xb0c0) shouldBe 0xb0
  }

  val regImmediateOperations = Table(
      ("opcode", "dest", "value"),
      (0x3e, "a", 0x10), (0x06, "b", 0x11), (0x0e, "c", 0x22), (0x16, "d", 0x33), (0x1e, "e", 0xaa),
      (0x26, "h", 0xab), (0x2e, "l", 0xef)
  )

  forAll(regImmediateOperations) { (opcode, dest, value) =>
    s"ld $dest, $value" should "transfer immediate value into register correctly" in new Machine {
      // given
      nextInstructionIs(opcode, value)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe 0x0002
      registerValue(dest) shouldBe value
    }
  }

  "ld (hl), 0xff" should "transfer value to referenced memory location correctly" in new Machine {
    // given
    nextInstructionIs(0x36, 0xff)
    registerContainsValue("hl", 0xa123)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0002
    memory(0xa123) shouldBe 0xff
  }

  val regIndexedAddrOperations = Table(
    ("opcode", "destination", "source"),
    ((0xdd, 0x7e), "a", "ix"), ((0xfd, 0x7e), "a", "iy"),
    ((0xdd, 0x46), "b", "ix"), ((0xfd, 0x46), "b", "iy"),
    ((0xdd, 0x4e), "c", "ix"), ((0xfd, 0x4e), "c", "iy"),
    ((0xdd, 0x56), "d", "ix"), ((0xfd, 0x56), "d", "iy"),
    ((0xdd, 0x5e), "e", "ix"), ((0xfd, 0x5e), "e", "iy"),
    ((0xdd, 0x66), "h", "ix"), ((0xfd, 0x66), "h", "iy"),
    ((0xdd, 0x6e), "l", "ix"), ((0xfd, 0x6e), "l", "iy")
  )

  forAll(regIndexedAddrOperations) { (opcode, dest, source) =>
    s"ld $dest, ($source + d)" should "transfer value from indexed referenced address to register correctly" in new Machine {
      // given
      val offset = Random.nextInt(256)
      nextInstructionIs(opcode._1, opcode._2, offset)
      registerContainsValue(source, 0x1000)

      val referencedAddress = 0x1000 + offset.asInstanceOf[Byte]
      val memoryValue = randomByte
      memory(referencedAddress) = memoryValue

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe 0x0003
      registerValue(dest) shouldBe memoryValue
    }
  }

  "ld a, (ix + d)" should "transfer value from indexed referenced address when the offset wraps around the bottom of memory" in new Machine {
    // given
    val offset = randomByte
    nextInstructionIs(0xdd, 0x7e, 0x80)
    registerContainsValue("ix", 0x000a)

    val referencedAddress = 0xff8a
    val memoryValue = 0x12
    memory(referencedAddress) = memoryValue

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0003
    registerValue("a") shouldBe memoryValue
  }

  "ld a, (ix + d)" should "transfer value from indexed referenced address when the offset wraps around the top of memory" in new Machine {
    // given
    val offset = randomByte
    nextInstructionIs(0xdd, 0x7e, 0x7f)
    registerContainsValue("ix", 0xffff)

    val referencedAddress = 0x007e
    val memoryValue = 0x12
    memory(referencedAddress) = memoryValue

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0003
    registerValue("a") shouldBe memoryValue
  }

  "ld a, (nn)" should "transfer a value from memory to the register correctly" in new Machine {
    // given
    val address = Random.nextInt(0x10000)
    val (addressHi, addressLo) = splitWord(address)

    nextInstructionIs(0x3a, addressLo, addressHi)

    val memoryValue = randomByte
    memory(address) = memoryValue

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0003
    registerValue("a") shouldBe memoryValue
  }

  val indexedRegFromRegOperations = Table(
    ("opcode", "dest", "source"),
    ((0xdd, 0x77), "ix", "a"),
    ((0xfd, 0x77), "iy", "a"),
    ((0xdd, 0x70), "ix", "b"),
    ((0xfd, 0x70), "iy", "b"),
    ((0xdd, 0x71), "ix", "c"),
    ((0xfd, 0x71), "iy", "c"),
    ((0xdd, 0x72), "ix", "d"),
    ((0xfd, 0x72), "iy", "d"),
    ((0xdd, 0x73), "ix", "e"),
    ((0xfd, 0x73), "iy", "e"),
    ((0xdd, 0x74), "ix", "h"),
    ((0xfd, 0x74), "iy", "h"),
    ((0xdd, 0x75), "ix", "l"),
    ((0xfd, 0x75), "iy", "l")
  )

  forAll(indexedRegFromRegOperations) { (opcode, dest, source) =>
    s"ld ($dest + d), $source" should "transfer value to indexed referenced address from register correctly" in new Machine {
      // given
      val offset = randomByte
      nextInstructionIs(opcode._1, opcode._2, offset)

      val registerValue = randomByte
      registerContainsValue(source, registerValue)
      registerContainsValue(dest, 0x1000)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe 0x0003

      val referencedAddress = 0x1000 + offset.asInstanceOf[Byte]
      memory(referencedAddress) shouldBe registerValue
    }
  }

  "ld (nn), a" should "transfer value to memory address from register correctly" in new Machine {
    // given
    val registerValue = randomByte
    registerContainsValue("a", registerValue)

    val address = Random.nextInt(0x10000)
    val (addressHi, addressLo) = splitWord(address)
    nextInstructionIs(0x32, addressLo, addressHi)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0003
    memory(address) shouldBe registerValue
  }

  val indexedRegImmediateOperations = Table(
    ("opcode", "dest"),
    ((0xdd, 0x36), "ix"),
    ((0xfd, 0x36), "iy")
  )

  forAll(indexedRegImmediateOperations) { (opcode, dest) =>
    s"ld ($dest + d), n" should "transfer immediate value into indexed referenced address correctly" in new Machine {
      // given
      val offset = randomByte
      val immediateValue = randomByte

      registerContainsValue(dest, 0x1000)
      nextInstructionIs(opcode._1, opcode._2, offset, immediateValue)

      // when
      processor.execute()

      // then
      memory(0xffff & (0x1000 + offset.asInstanceOf[Byte])) shouldBe immediateValue
    }
  }
}