package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class JumpSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  "jp nn" should "jump to the correct location" in new Machine {
    // given
    registerContainsValue("a", 0x00)
    memory.set(0xbeef, 0xcb)
    memory.set(0xbef0, 0xff)

    nextInstructionIs(0xc3, 0xef, 0xbe)

    // when
    processor.execute()
    processor.execute()

    // then
    registerValue("a") shouldBe binary("10000000")
    registerValue("pc") shouldBe 0xbef1
  }

  val jumpOnNegativeValues = Table(
    ("flagValue", "should", "text"),
    (true, false, "not"),
    (false, true, "")
  )

  val jumpOnPositiveValues = Table(
    ("flagValue", "should", "text"),
    (true, true, ""),
    (false, false, "not")
  )

  forAll(jumpOnNegativeValues) { (flagValue, shouldJump, text) =>
    "jp nz, nn" should s"$text jump if z flag is $flagValue" in new Machine {
      // given
      flag("z").is(flagValue)
      nextInstructionIs(0xc2, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  forAll(jumpOnPositiveValues) { (flagValue, shouldJump, text) =>
    "jp z, nn" should s"$text jump if z flag is $flagValue" in new Machine {
      // given
      flag("z").is(flagValue)
      nextInstructionIs(0xca, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  forAll(jumpOnNegativeValues) { (flagValue, shouldJump, text) =>
    "jp nc, nn" should s"$text jump if c flag is $flagValue" in new Machine {
      // given
      flag("c").is(flagValue)
      nextInstructionIs(0xd2, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  forAll(jumpOnPositiveValues) { (flagValue, shouldJump, text) =>
    "jp c, nn" should s"$text jump if c flag is $flagValue" in new Machine {
      // given
      flag("c").is(flagValue)
      nextInstructionIs(0xda, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  forAll(jumpOnNegativeValues) { (flagValue, shouldJump, text) =>
    "jp po, nn" should s"$text jump if p flag is $flagValue" in new Machine {
      // given
      flag("p").is(flagValue)
      nextInstructionIs(0xe2, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  forAll(jumpOnPositiveValues) { (flagValue, shouldJump, text) =>
    "jp pe, nn" should s"$text jump if p flag is $flagValue" in new Machine {
      // given
      flag("p").is(flagValue)
      nextInstructionIs(0xea, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  forAll(jumpOnNegativeValues) { (flagValue, shouldJump, text) =>
    "jp p, nn" should s"$text jump if s flag is $flagValue" in new Machine {
      // given
      flag("s").is(flagValue)
      nextInstructionIs(0xf2, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  forAll(jumpOnPositiveValues) { (flagValue, shouldJump, text) =>
    "jp m, nn" should s"$text jump if s flag is $flagValue" in new Machine {
      // given
      flag("s").is(flagValue)
      nextInstructionIs(0xfa, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0x0003)
    }
  }

  val jumps = Table(
    ("start", "jump size", "destination"),
    (0x0480, 0x03, 0x0485), (0x0480, 0xfb, 0x47d)
  )

  "jr n" should "jump to the correct location" in new Machine {
    forAll(jumps) { (start, jumpSize, destination) =>
      // given
      registerContainsValue("pc", start)
      nextInstructionIs(0x18, jumpSize)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe destination
    }
  }

  forAll(jumpOnPositiveValues) { (flagValue, shouldJump, text) =>
    "jr c, n" should s"$text jump if c flag is $flagValue" in new Machine {
      // given
      registerContainsValue("pc", 0xbeea)
      flag("c").is(flagValue)
      nextInstructionIs(0x38, 0x03)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0xbeec)
    }
  }

  forAll(jumpOnNegativeValues) { (flagValue, shouldJump, text) =>
    "jr nc, n" should s"$text jump if c flag is $flagValue" in new Machine {
      // given
      registerContainsValue("pc", 0xbeea)
      flag("c").is(flagValue)
      nextInstructionIs(0x30, 0x03)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0xbeec)
    }
  }

  forAll(jumpOnPositiveValues) { (flagValue, shouldJump, text) =>
    "jr z, n" should s"$text jump if z flag is $flagValue" in new Machine {
      // given
      registerContainsValue("pc", 0xbeea)
      flag("z").is(flagValue)
      nextInstructionIs(0x28, 0x03)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0xbeec)
    }
  }

  forAll(jumpOnNegativeValues) { (flagValue, shouldJump, text) =>
    "jr nz, n" should s"$text jump if z flag is $flagValue" in new Machine {
      // given
      registerContainsValue("pc", 0xbeea)
      flag("z").is(flagValue)
      nextInstructionIs(0x20, 0x03)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (shouldJump) 0xbeef else 0xbeec)
    }
  }

  "jp (hl)" should "jump to the correct location" in new Machine {
    // given
    registerContainsValue("hl", 0xbabe)
    nextInstructionIs(0xe9)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0xbabe
  }

  val indexedJumpOperations = Table(
    ("opcode", "register"),
    ((0xdd, 0xe9), "ix"),
    ((0xfd, 0xe9), "iy")
  )

  forAll(indexedJumpOperations) { (opcode, register) =>
    s"jp ($register)" should "jump to the correct address" in new Machine {
      // given
      registerContainsValue(register, 0xdead)
      nextInstructionIs(opcode._1, opcode._2)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe 0xdead
    }
  }

  "djnz n" should "jump when b decrements to non-zero" in new Machine {
    // given
    registerContainsValue("pc", 0x1000)
    registerContainsValue("b", 0xff)

    nextInstructionIs(0x10, 0x10)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x1012
  }

  "djnz n" should "not jump when b decrements to zero" in new Machine {
    // given
    registerContainsValue("pc", 0x1000)
    registerContainsValue("b", 0x01)

    nextInstructionIs(0x10, 0x10)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x1002
  }

  "djnz n" should "jump when b underflows" in new Machine {
    // given
    registerContainsValue("pc", 0x1000)
    registerContainsValue("b", 0x00)

    nextInstructionIs(0x10, 0x10)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x1012
    registerValue("b") shouldBe 0xff
  }
}
