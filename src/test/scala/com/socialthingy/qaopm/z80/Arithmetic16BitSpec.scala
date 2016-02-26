package com.socialthingy.qaopm.z80

import org.scalatest.prop.TableDrivenPropertyChecks

class Arithmetic16BitSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val addHlRegOperations = Table(
    ("opcode", "register"),
    (0x09, "bc"), (0x19, "de"), (0x39, "sp")    
  )
  
  forAll(addHlRegOperations) { (opcode, register) =>
    s"add hl, $register" should "calculate the correct result" in new Machine {
      // given
      registerContainsValue("hl", 0x1111)
      registerContainsValue(register, 0x1111)

      nextInstructionIs(opcode)

      // when
      processor.execute()

      // then
      registerValue("hl") shouldBe 0x2222

      flag("h").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  "add hl, hl" should "calculate a result with no carry or half carry" in new Machine {
    // given
    registerContainsValue("hl", 0x1111)

    nextInstructionIs(0x29)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x2222

    flag("h").value shouldBe false
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  "add hl, bc" should "calculate the correct result with full and half carry" in new Machine {
    // given
    registerContainsValue("hl", 0x1100)
    registerContainsValue("bc", 0xff00)

    nextInstructionIs(0x09)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x1000

    flag("h").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe true
  }

  val truthValues = Table("value", true, false)

  val adcHlRegOperations = Table(
    ("opcode", "register"),
    (0x4a, "bc"), (0x5a, "de"), (0x7a, "sp")
  )

  forAll(adcHlRegOperations) { (opcode, register) =>
    forAll(truthValues) { carry =>
      s"adc hl, $register with carry $carry" should "calculate the correct result" in new Machine {
        // given
        registerContainsValue("hl", 0x1100)
        registerContainsValue(register, 0x0011)
        flag("c") is carry

        nextInstructionIs(0xed, opcode)

        // when
        processor.execute()

        // then
        registerValue("hl")  shouldBe (0x1111 + (if (carry) 1 else 0))

        flag("s").value shouldBe false
        flag("z").value shouldBe false
        flag("h").value shouldBe false
        flag("p").value shouldBe false
        flag("n").value shouldBe false
        flag("c").value shouldBe false
      }
    }
  }

  "adc hl, bc with carry true" should "correctly calculate a negative result" in new Machine {
    // given
    registerContainsValue("hl", 0x7fff)
    registerContainsValue("bc", 0x0001)
    flag("c") is true

    nextInstructionIs(0xed, 0x4a)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x8001

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe false
  }

  it should "correctly calculate a zero result" in new Machine {
    // given
    registerContainsValue("hl", 0xfffe)
    registerContainsValue("bc", 0x0001)
    flag("c") is true

    nextInstructionIs(0xed, 0x4a)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe false
    flag("c").value shouldBe true
  }

  val sbcHlRegOperations = Table(
    ("opcode", "register"),
    (0x42, "bc"), (0x52, "de"), (0x72, "sp")
  )

  forAll(sbcHlRegOperations) { (opcode, register) =>
    forAll(truthValues) { carry =>
      s"sbc hl, $register with carry $carry" should "calculate the correct result" in new Machine {
        // given
        registerContainsValue("hl", 0x1234)
        registerContainsValue(register, 0x0134)
        flag("c") is carry

        nextInstructionIs(0xed, opcode)

        // when
        processor.execute()

        // then
        val expected = if (carry) 0x10ff else 0x1100
        registerValue("hl") shouldBe expected

        flag("s").value shouldBe false
        flag("z").value shouldBe false
        flag("h").value shouldBe false
        flag("p").value shouldBe false
        flag("n").value shouldBe true
        flag("c").value shouldBe false
      }
    }
  }

  "sbc hl, hl with carry true" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    flag("c") is true

    nextInstructionIs(0xed, 0x62)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0xffff

    flag("s").value shouldBe true
    flag("z").value shouldBe false
    flag("h").value shouldBe true
    flag("p").value shouldBe true
    flag("n").value shouldBe true
    flag("c").value shouldBe true
  }

  "sbc hl, hl with carry false" should "calculate the correct result" in new Machine {
    // given
    registerContainsValue("hl", 0x1000)
    flag("c") is false

    nextInstructionIs(0xed, 0x62)

    // when
    processor.execute()

    // then
    registerValue("hl") shouldBe 0x0000

    flag("s").value shouldBe false
    flag("z").value shouldBe true
    flag("h").value shouldBe false
    flag("p").value shouldBe false
    flag("n").value shouldBe true
    flag("c").value shouldBe false
  }

  val indexRegisters = Table(("prefix", "register"), (0xdd, "ix"), (0xfd, "iy"))
  val addIndexRegOtherReg = Table(
    ("opcode", "register"),
    (0x09, "bc"), (0x19, "de"), (0x39, "sp")
  )

  forAll(indexRegisters) { (prefix, indexRegister) =>
    forAll(addIndexRegOtherReg) { (opcode, register) =>
      s"add $indexRegister, $register" should "correctly calculate a result with no carries" in new Machine {
        // given
        registerContainsValue(indexRegister, 0x1000)
        registerContainsValue(register, 0x0111)

        nextInstructionIs(prefix, opcode)

        // when
        processor.execute()

        // then
        registerValue(indexRegister) shouldBe 0x1111

        flag("h").value shouldBe false
        flag("n").value shouldBe false
        flag("c").value shouldBe false
      }
    }
  }

  forAll(indexRegisters) { (prefix, indexRegister) =>
    s"add $indexRegister, bc" should "correctly calculate a result with half carry" in new Machine {
      // given
      registerContainsValue(indexRegister, 0x1f00)
      registerContainsValue("bc", 0x0100)

      nextInstructionIs(prefix, 0x09)

      // when
      processor.execute()

      // then
      registerValue(indexRegister) shouldBe 0x2000

      flag("h").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe false
    }
  }

  forAll(indexRegisters) { (prefix, indexRegister) =>
    s"add $indexRegister, bc" should "correctly calculate a result with full and half carry" in new Machine {
      // given
      registerContainsValue(indexRegister, 0xff00)
      registerContainsValue("bc", 0x0100)

      nextInstructionIs(prefix, 0x09)

      // when
      processor.execute()

      // then
      registerValue(indexRegister) shouldBe 0x0000

      flag("h").value shouldBe true
      flag("n").value shouldBe false
      flag("c").value shouldBe true
    }
  }

  val incOperations = Table(
    ("opcode", "register"),
    (0x03, "bc"), (0x13, "de"), (0x23, "hl"), (0x33, "sp")
  )

  val incStates = Table(
    ("initial", "final"),
    (0x0010, 0x0011),
    (0xffff, 0x0000)
  )

  forAll(incOperations) { (opcode, register) =>
    forAll(incStates) { (initialState, finalState) =>
      s"inc $register" should s"increment the register value from $initialState to $finalState" in new Machine {
        // given
        registerContainsValue(register, initialState)
        nextInstructionIs(opcode)

        // when
        processor.execute()

        // then
        registerValue(register) shouldBe finalState
      }
    }
  }

  forAll(indexRegisters) { (prefix, register) =>
    forAll(incStates) { (initialState, finalState) =>
      s"inc $register" should s"increment the register value from $initialState to $finalState" in new Machine {
        // given
        registerContainsValue(register, initialState)
        nextInstructionIs(prefix, 0x23)

        // when
        processor.execute()

        // then
        registerValue(register) shouldBe finalState
      }
    }
  }

  val decOperations = Table(
    ("opcode", "register"),
    (0x0b, "bc"), (0x1b, "de"), (0x2b, "hl"), (0x3b, "sp")
  )

  val decStates = Table(
    ("initial", "final"),
    (0x0010, 0x000f),
    (0x0000, 0xffff)
  )

  forAll(decOperations) { (opcode, register) =>
    forAll(decStates) { (initialState, finalState) =>
      s"dec $register" should s"decrement the register value from $initialState to $finalState" in new Machine {
        // given
        registerContainsValue(register, initialState)
        nextInstructionIs(opcode)

        // when
        processor.execute()

        // then
        registerValue(register) shouldBe finalState
      }
    }
  }

  forAll(indexRegisters) { (prefix, register) =>
    forAll(decStates) { (initialState, finalState) =>
      s"dec $register" should s"decrement the register value from $initialState to $finalState" in new Machine {
        // given
        registerContainsValue(register, initialState)
        nextInstructionIs(prefix, 0x2b)

        // when
        processor.execute()

        // then
        registerValue(register) shouldBe finalState
      }
    }
  }
}
