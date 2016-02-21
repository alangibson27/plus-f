package com.socialthingy.qaopm.z80

import org.scalatest.prop.TableDrivenPropertyChecks

class BitManipulationSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val bitOperations = Table(
    ("opcode", "register", "bitPosition"),
    (0x40, "b", 0), (0x41, "c", 0), (0x42, "d", 0), (0x43, "e", 0), (0x44, "h", 0), (0x45, "l", 0), (0x47, "a", 0),
    (0x48, "b", 1), (0x49, "c", 1), (0x4a, "d", 1), (0x4b, "e", 1), (0x4c, "h", 1), (0x4d, "l", 1), (0x4f, "a", 1),
    (0x50, "b", 2), (0x51, "c", 2), (0x52, "d", 2), (0x53, "e", 2), (0x54, "h", 2), (0x55, "l", 2), (0x57, "a", 2),
    (0x58, "b", 3), (0x59, "c", 3), (0x5a, "d", 3), (0x5b, "e", 3), (0x5c, "h", 3), (0x5d, "l", 3), (0x5f, "a", 3),
    (0x60, "b", 4), (0x61, "c", 4), (0x62, "d", 4), (0x63, "e", 4), (0x64, "h", 4), (0x65, "l", 4), (0x67, "a", 4),
    (0x68, "b", 5), (0x69, "c", 5), (0x6a, "d", 5), (0x6b, "e", 5), (0x6c, "h", 5), (0x6d, "l", 5), (0x6f, "a", 5),
    (0x70, "b", 6), (0x71, "c", 6), (0x72, "d", 6), (0x73, "e", 6), (0x74, "h", 6), (0x75, "l", 6), (0x77, "a", 6),
    (0x78, "b", 7), (0x79, "c", 7), (0x7a, "d", 7), (0x7b, "e", 7), (0x7c, "h", 7), (0x7d, "l", 7), (0x7f, "a", 7)
  )
  
  val truthValues = Table("value", true, false)
  
  forAll(bitOperations) { (opcode, register, bitPosition) =>
    forAll(truthValues) { value =>
      s"bit $bitPosition, $register when bit is $value" should "calculate the correct result" in new Machine {
        // given
        registerContainsValue(register, if (value) Math.pow(2, bitPosition).asInstanceOf[Int] else 0)
        nextInstructionIs(0xcb, opcode)

        // when
        processor.execute()

        // then
        flag("z").value shouldBe !value
        flag("h").value shouldBe true
        flag("n").value shouldBe false
      }
    }
  }
  
  val indirectBitOperations = Table(
    ("opcode", "bitPosition"),
    (0x46, 0), (0x4e, 1),
    (0x56, 2), (0x5e, 3),
    (0x66, 4), (0x6e, 5),
    (0x76, 6), (0x7e, 7)
  ) 
  
  forAll(indirectBitOperations) { (opcode, bitPosition) =>
    forAll(truthValues) { value =>
      s"bit $bitPosition, (hl) when bit is $value" should "calculate the correct result" in new Machine {
        // given
        registerContainsValue("hl", 0x1234)
        memory(0x1234) = if (value) Math.pow(2, bitPosition).asInstanceOf[Int] else 0
        nextInstructionIs(0xcb, opcode)

        // when
        processor.execute()

        // then
        flag("z").value shouldBe !value
        flag("h").value shouldBe true
        flag("n").value shouldBe false
      }
    }
  }
  
  val indexRegisters = Table(
    ("opcode", "register"),
    (0xdd, "ix"),
    (0xfd, "iy")
  )

  forAll(indexRegisters) { (registerOpcode, indexRegister) =>
    forAll(indirectBitOperations) { (opcode, bitPosition) =>
      forAll(truthValues) { value =>
        s"bit $bitPosition, ($indexRegister + d) when bit is $value" should "calculate the correct result" in new Machine {
          // given
          val offset = randomByte
          registerContainsValue(indexRegister, 0x1234)
          memory(0x1234 + offset.asInstanceOf[Byte]) = if (value) Math.pow(2, bitPosition).toInt else 0
          nextInstructionIs(registerOpcode, 0xcb, offset, opcode)

          // when
          processor.execute()

          // then
          flag("z").value shouldBe !value
          flag("h").value shouldBe true
          flag("n").value shouldBe false
        }
      }
    }
  }

  val resOperations = Table(
    ("opcode", "register", "bitPosition"),
    (0x80, "b", 0), (0x81, "c", 0), (0x82, "d", 0), (0x83, "e", 0), (0x84, "h", 0), (0x85, "l", 0), (0x87, "a", 0),
    (0x88, "b", 1), (0x89, "c", 1), (0x8a, "d", 1), (0x8b, "e", 1), (0x8c, "h", 1), (0x8d, "l", 1), (0x8f, "a", 1),
    (0x90, "b", 2), (0x91, "c", 2), (0x92, "d", 2), (0x93, "e", 2), (0x94, "h", 2), (0x95, "l", 2), (0x97, "a", 2),
    (0x98, "b", 3), (0x99, "c", 3), (0x9a, "d", 3), (0x9b, "e", 3), (0x9c, "h", 3), (0x9d, "l", 3), (0x9f, "a", 3),
    (0xa0, "b", 4), (0xa1, "c", 4), (0xa2, "d", 4), (0xa3, "e", 4), (0xa4, "h", 4), (0xa5, "l", 4), (0xa7, "a", 4),
    (0xa8, "b", 5), (0xa9, "c", 5), (0xaa, "d", 5), (0xab, "e", 5), (0xac, "h", 5), (0xad, "l", 5), (0xaf, "a", 5),
    (0xb0, "b", 6), (0xb1, "c", 6), (0xb2, "d", 6), (0xb3, "e", 6), (0xb4, "h", 6), (0xb5, "l", 6), (0xb7, "a", 6),
    (0xb8, "b", 7), (0xb9, "c", 7), (0xba, "d", 7), (0xbb, "e", 7), (0xbc, "h", 7), (0xbd, "l", 7), (0xbf, "a", 7)
  )

  forAll(resOperations) { (opcode, register, bitPosition) =>
    s"res $bitPosition, $register" should "reset the correct bit" in new Machine {
      // given
      registerContainsValue(register, 0xff)
      nextInstructionIs(0xcb, opcode)

      // when
      processor.execute()

      // then
      (registerValue(register) & (1 << bitPosition)) shouldBe 0
    }
  }

  val indirectResOperations = Table(
    ("opcode", "bitPosition"),
    (0x86, 0), (0x8e, 1),
    (0x96, 2), (0x9e, 3),
    (0xa6, 4), (0xae, 5),
    (0xb6, 6), (0xbe, 7)
  )

  forAll(indirectResOperations) { (opcode, bitPosition) =>
    s"res $bitPosition, (hl)" should "reset the correct bit" in new Machine {
      // given
      registerContainsValue("hl", 0x1234)
      memory(0x1234) = 0xff
      nextInstructionIs(0xcb, opcode)

      // when
      processor.execute()

      // then
      (memory(0x1234) & (1 << bitPosition)) shouldBe 0
    }
  }

  forAll(indexRegisters) { (registerOpcode, indexRegister) =>
    forAll(indirectResOperations) { (opcode, bitPosition) =>
      s"res $bitPosition, ($indexRegister + d)" should "reset the correct bit" in new Machine {
        // given
        val offset = randomByte
        val address = 0x1234 + offset.asInstanceOf[Byte]
        registerContainsValue(indexRegister, 0x1234)
        memory(address) = 0xff
        nextInstructionIs(registerOpcode, 0xcb, offset, opcode)

        // when
        processor.execute()

        // then
        (memory(address) & (1 << bitPosition)) shouldBe 0
      }
    }
  }

  val setOperations = Table(
    ("opcode", "register", "bitPosition"),
    (0xc0, "b", 0), (0xc1, "c", 0), (0xc2, "d", 0), (0xc3, "e", 0), (0xc4, "h", 0), (0xc5, "l", 0), (0xc7, "a", 0),
    (0xc8, "b", 1), (0xc9, "c", 1), (0xca, "d", 1), (0xcb, "e", 1), (0xcc, "h", 1), (0xcd, "l", 1), (0xcf, "a", 1),
    (0xd0, "b", 2), (0xd1, "c", 2), (0xd2, "d", 2), (0xd3, "e", 2), (0xd4, "h", 2), (0xd5, "l", 2), (0xd7, "a", 2),
    (0xd8, "b", 3), (0xd9, "c", 3), (0xda, "d", 3), (0xdb, "e", 3), (0xdc, "h", 3), (0xdd, "l", 3), (0xdf, "a", 3),
    (0xe0, "b", 4), (0xe1, "c", 4), (0xe2, "d", 4), (0xe3, "e", 4), (0xe4, "h", 4), (0xe5, "l", 4), (0xe7, "a", 4),
    (0xe8, "b", 5), (0xe9, "c", 5), (0xea, "d", 5), (0xeb, "e", 5), (0xec, "h", 5), (0xed, "l", 5), (0xef, "a", 5),
    (0xf0, "b", 6), (0xf1, "c", 6), (0xf2, "d", 6), (0xf3, "e", 6), (0xf4, "h", 6), (0xf5, "l", 6), (0xf7, "a", 6),
    (0xf8, "b", 7), (0xf9, "c", 7), (0xfa, "d", 7), (0xfb, "e", 7), (0xfc, "h", 7), (0xfd, "l", 7), (0xff, "a", 7)
  )

  forAll(setOperations) { (opcode, register, bitPosition) =>
    s"set $bitPosition, $register" should "set the correct bit" in new Machine {
      // given
      registerContainsValue(register, 0x00)
      nextInstructionIs(0xcb, opcode)

      // when
      processor.execute()

      // then
      (registerValue(register) >> bitPosition) shouldBe 1
    }
  }

  val indirectSetOperations = Table(
    ("opcode", "bitPosition"),
    (0xc6, 0), (0xce, 1),
    (0xd6, 2), (0xde, 3),
    (0xe6, 4), (0xee, 5),
    (0xf6, 6), (0xfe, 7)
  )

  forAll(indirectSetOperations) { (opcode, bitPosition) =>
    s"set $bitPosition, (hl)" should "set the correct bit" in new Machine {
      // given
      registerContainsValue("hl", 0x1234)
      memory(0x1234) = 0x00
      nextInstructionIs(0xcb, opcode)

      // when
      processor.execute()

      // then
      (memory(0x1234) >> bitPosition) shouldBe 1
    }
  }

  forAll(indexRegisters) { (registerOpcode, indexRegister) =>
    forAll(indirectSetOperations) { (opcode, bitPosition) =>
      s"set $bitPosition, ($indexRegister + d)" should "set the correct bit" in new Machine {
        // given
        val offset = randomByte
        val address = 0x1234 + offset.asInstanceOf[Byte]
        registerContainsValue(indexRegister, 0x1234)
        memory(address) = 0x00
        nextInstructionIs(registerOpcode, 0xcb, offset, opcode)

        // when
        processor.execute()

        // then
        (memory(address) >> bitPosition) shouldBe 1
      }
    }
  }
}
