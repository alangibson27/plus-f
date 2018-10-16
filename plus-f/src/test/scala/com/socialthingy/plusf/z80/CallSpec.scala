package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class CallSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  "call" should "jump and store the return address on the stack" in new Machine {
    // given
    registerContainsValue("sp", 0xffff)
    registerContainsValue("pc", 0x1234)

    nextInstructionIs(0xcd, 0xef, 0xbe)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0xbeef
    registerValue("sp") shouldBe 0xfffd
    memory.get(0xfffd) shouldBe 0x37
    memory.get(0xfffe) shouldBe 0x12
  }

  val truthValues = Table("value", true, false)
  forAll(truthValues) { z =>
    "call nz" should s"work correctly when the z flag is $z" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("z") is z

      nextInstructionIs(0xc4, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (z) 0x1237 else 0xbeef)
      registerValue("sp") shouldBe (if (z) 0xffff else 0xfffd)
    }
  }

  forAll(truthValues) { z =>
    "call z" should s"work correctly when the z flag is $z" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("z") is z

      nextInstructionIs(0xcc, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (z) 0xbeef else 0x1237)
      registerValue("sp") shouldBe (if (z) 0xfffd else 0xffff)
    }
  }

  forAll(truthValues) { c =>
    "call nc" should s"work correctly when the c flag is $c" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("c") is c

      nextInstructionIs(0xd4, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (c) 0x1237 else 0xbeef)
      registerValue("sp") shouldBe (if (c) 0xffff else 0xfffd)
    }
  }

  forAll(truthValues) { c =>
    "call c" should s"work correctly when the c flag is $c" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("c") is c

      nextInstructionIs(0xdc, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (c) 0xbeef else 0x1237)
      registerValue("sp") shouldBe (if (c) 0xfffd else 0xffff)
    }
  }

  forAll(truthValues) { p =>
    "call po" should s"work correctly when the p flag is $p" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("p") is p

      nextInstructionIs(0xe4, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (p) 0x1237 else 0xbeef)
      registerValue("sp") shouldBe (if (p) 0xffff else 0xfffd)
    }
  }

  forAll(truthValues) { p =>
    "call pe" should s"work correctly when the p flag is $p" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("p") is p

      nextInstructionIs(0xec, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (p) 0xbeef else 0x1237)
      registerValue("sp") shouldBe (if (p) 0xfffd else 0xffff)
    }
  }

  forAll(truthValues) { s =>
    "call p" should s"work correctly when the s flag is $s" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("s") is s

      nextInstructionIs(0xf4, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (s) 0x1237 else 0xbeef)
      registerValue("sp") shouldBe (if (s) 0xffff else 0xfffd)
    }
  }

  forAll(truthValues) { s =>
    "call m" should s"work correctly when the s flag is $s" in new Machine {
      // given
      registerContainsValue("sp", 0xffff)
      registerContainsValue("pc", 0x1234)
      flag("s") is s

      nextInstructionIs(0xfc, 0xef, 0xbe)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (s) 0xbeef else 0x1237)
      registerValue("sp") shouldBe (if (s) 0xfffd else 0xffff)
    }
  }

  "ret" should "restore the program counter and pop the return address from the stack" in new Machine {
    // given
    registerContainsValue("pc", 0x1234)
    registerContainsValue("sp", 0xfffd)

    memory.set(0xfffd, 0xef)
    memory.set(0xfffe, 0xbe)

    nextInstructionIs(0xc9)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0xbeef
    registerValue("sp") shouldBe 0xffff
  }

  forAll(truthValues) { z =>
    "ret nz" should s"work correctly when the z flag is $z" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("z") is z

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xc0)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (z) 0x5679 else 0xbabe)
      registerValue("sp") shouldBe (if (z) 0xfffd else 0xffff)
    }
  }

  forAll(truthValues) { z =>
    "ret z" should s"work correctly when the z flag is $z" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("z") is z

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xc8)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (z) 0xbabe else 0x5679)
      registerValue("sp") shouldBe (if (z) 0xffff else 0xfffd)
    }
  }

  forAll(truthValues) { c =>
    "ret nc" should s"work correctly when the c flag is $c" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("c") is c

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xd0)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (c) 0x5679 else 0xbabe)
      registerValue("sp") shouldBe (if (c) 0xfffd else 0xffff)
    }
  }

  forAll(truthValues) { c =>
    "ret c" should s"work correctly when the c flag is $c" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("c") is c

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xd8)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (c) 0xbabe else 0x5679)
      registerValue("sp") shouldBe (if (c) 0xffff else 0xfffd)
    }
  }

  forAll(truthValues) { p =>
    "ret po" should s"work correctly when the p flag is $p" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("p") is p

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xe0)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (p) 0x5679 else 0xbabe)
      registerValue("sp") shouldBe (if (p) 0xfffd else 0xffff)
    }
  }

  forAll(truthValues) { p =>
    "ret pe" should s"work correctly when the p flag is $p" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("p") is p

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xe8)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (p) 0xbabe else 0x5679)
      registerValue("sp") shouldBe (if (p) 0xffff else 0xfffd)
    }
  }

  forAll(truthValues) { s =>
    "ret p" should s"work correctly when the s flag is $s" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("s") is s

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xf0)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (s) 0x5679 else 0xbabe)
      registerValue("sp") shouldBe (if (s) 0xfffd else 0xffff)
    }
  }

  forAll(truthValues) { s =>
    "ret m" should s"work correctly when the s flag is $s" in new Machine {
      // given
      registerContainsValue("pc", 0x5678)
      registerContainsValue("sp", 0xfffd)
      flag("s") is s

      memory.set(0xfffd, 0xbe)
      memory.set(0xfffe, 0xba)

      nextInstructionIs(0xf8)

      // when
      processor.execute()

      // then
      registerValue("pc") shouldBe (if (s) 0xbabe else 0x5679)
      registerValue("sp") shouldBe (if (s) 0xffff else 0xfffd)
    }
  }

  val rstOperations = Table(
    ("opcode", "jumpAddress"),
    (0xc7, 0x00), (0xcf, 0x08),
    (0xd7, 0x10), (0xdf, 0x18),
    (0xe7, 0x20), (0xef, 0x28),
    (0xf7, 0x30), (0xff, 0x38)
  )

  forAll(rstOperations) { (opcode, callAddress) =>
    s"rst 0x${callAddress.toHexString}" should "call to the correct address" in new Machine {
      // given
      registerContainsValue("pc", 0x9988)
      registerContainsValue("sp", 0xffff)

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      memory.get(0xfffd) shouldBe 0x89
      memory.get(0xfffe) shouldBe 0x99
      registerValue("sp") shouldBe 0xfffd
      registerValue("pc") shouldBe callAddress
    }
  }
}
