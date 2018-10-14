package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import com.socialthingy.plusf.z80.operations.{Nop, OpHalt}
import org.scalatest.prop.TableDrivenPropertyChecks

class InterruptSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  trait InterruptingMachine extends Machine {
    private val interrupt_mode_op_codes = Map(
      0 -> List(0xed, 0x46),
      1 -> List(0xed, 0x56),
      2 -> List(0xed, 0x5e)
    )

    def maskableInterruptsAreEnabled: Unit = {
      nextInstructionIs(0xfb)
      processor.execute()
      nextInstructionIs(0x00)
      processor.execute()
    }
    
    def maskableInterruptsAreDisabled: Unit = {
      nextInstructionIs(0xf3)
      processor.execute()
    }

    def maskableInterruptModeIs(mode: Int): Unit = {
      interrupt_mode_op_codes(mode) foreach (nextInstructionIs(_))
      processor.execute()
    }

    def anInterruptIsGenerated: Unit = processor.requestInterrupt()

    def routineAt(address: Int) = new RoutineBuilder(memory, address)
    
    def executeUntilNop(): Unit = {
      var op = processor.execute()
      while (!op.isInstanceOf[Nop]) {
        op = processor.execute()
      }
    }

    def executeRange(fromAddr: Int, toAddr: Int): Unit = {
      processor.register("pc").set(fromAddr)
      while (processor.register("pc").get() <= toAddr) {
        processor.execute()
      }
    }
  }

  class RoutineBuilder(memory: Memory, var address: Int) {
    def does(op_codes: Int*): Unit = {
      op_codes foreach { opcode =>
        memory.set(address, opcode)
        address += 1
      }
    }
  }

  "r register" should "increment lowest 7 bits by 1 on every fetch" in new Machine {
    val permutations = Table(
      ("initialValue", "finalValue"),
      (binary("11111111"), binary("10000000")),
      (binary("10000001"), binary("10000010")),
      (binary("01111111"), binary("00000000"))
    )
    
    forAll(permutations) { (initialValue, finalValue) =>
      // given
      registerContainsValue("r", initialValue)
      nextInstructionIs(0x00)

      // when
      processor.execute()

      // then
      registerValue("r") shouldBe finalValue

    }
  }

  it should "increment once when an operation with no operands is executed" in new Machine {
    // given
    registerContainsValue("r", binary("00000000"))
    nextInstructionIs(0x00)

    // when
    processor.execute()

    // then
    registerValue("r") shouldBe binary("00000001")
  }

  it should "increment once when an operation with a single operand is executed" in new Machine {
    // given
    registerContainsValue("r", binary("00000000"))
    nextInstructionIs(0x3e, 0x01)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x01
    registerValue("r") shouldBe binary("00000001")
  }

  it should "increment twice when a double-byte operation is executed" in new Machine {
    // given
    registerContainsValue("r", binary("00000000"))
    nextInstructionIs(0xed, 0xb0)

    // when
    processor.execute()

    // then
    registerValue("r") shouldBe binary("00000010")
  }

  val indexRegisters = Table(
    ("register", "prefix"),
    ("ix", 0xdd),
    ("iy", 0xfd)
  )

  forAll(indexRegisters) { (register, prefix) =>
    it should s"increment twice when an $register operation is executed" in new Machine {
      // given
      registerContainsValue("r", binary("00000000"))
      nextInstructionIs(prefix, 0x21, 0xff, 0xff)

      // when
      processor.execute()

      // then
      registerValue(register) shouldBe 0xffff
      registerValue("r") shouldBe binary("00000010")
    }

    it should s"increment twice when a triple-byte $register operation is executed" in new Machine {
      // given
      registerContainsValue("r", binary("00000000"))
      nextInstructionIs(prefix, 0xcb, 0x01, 0x06)

      // when
      processor.execute()

      // then
      registerValue("r") shouldBe binary("00000010")
    }
  }

  "ld i, a" should "transfer the accumulator to the i register" in new Machine {
    // given
    registerContainsValue("a", 0xbe)
    nextInstructionIs(0xed, 0x47)

    // when
    processor.execute()

    // then
    registerValue("i") shouldBe 0xbe
  }

  "ld a, i" should "transfer the i register to the accumulator" in new Machine {
    // given
    registerContainsValue("i", 0xff)
    nextInstructionIs(0xed, 0x57)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0xff
    registerValue("pc") shouldBe 0x0002
    flag("h").value shouldBe false
    flag("n").value shouldBe false
    flag("f3").value shouldBe true
    flag("f5").value shouldBe true
  }

  it should "set the parity flag if interrupts are enabled" in new Machine {
    // given
    interruptsAreEnabled()
    registerContainsValue("i", 0xff)
    nextInstructionIs(0xed, 0x57)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0xff
    flag("p").value shouldBe true
  }

  it should "reset the parity flag if interrupts are disabled" in new Machine {
    // given
    interruptsAreDisabled()
    registerContainsValue("i", 0xff)
    nextInstructionIs(0xed, 0x57)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0xff
    flag("p").value shouldBe false
  }

  it should "set the sign flag if the i register is negative" in new Machine {
    // given
    registerContainsValue("i", 0xff)
    nextInstructionIs(0xed, 0x57)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0xff
    flag("s").value shouldBe true
    flag("z").value shouldBe false
  }

  it should "set the zero flag if the i register is zero" in new Machine {
    // given
    registerContainsValue("i", 0x00)
    nextInstructionIs(0xed, 0x57)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0x00
    flag("s").value shouldBe false
    flag("z").value shouldBe true
  }

  "ld r, a" should "transfer the accumulator to the r register" in new Machine {
    // given
    registerContainsValue("a", 0xbe)
    nextInstructionIs(0xed, 0x4f)

    // when
    processor.execute()

    // then
    registerValue("r") shouldBe 0xbe
  }

  val truthValues = Table("value", true, false)
  forAll(truthValues) { iff2 =>
    s"ld a, r" should s"preserve the iff2 value when the iff2 value is $iff2" in new Machine {
      // given
      processor.setIff(1, iff2)
      registerContainsValue("r", 0xbe)
      nextInstructionIs(0xed, 0x5f)

      // when
      processor.execute()

      // then
      registerValue("a") shouldBe 0xc0
      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe iff2
      flag("n").value shouldBe false
      flag("f3").value shouldBe false
      flag("f5").value shouldBe false
    }
  }

  "ei" should "enable interrupts after the following instruction" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    maskableInterruptModeIs(1)
    anInterruptIsGenerated

    nextInstructionIs(0x3c)
    nextInstructionIs(0xfb)
    nextInstructionIs(0x3c)
    nextInstructionIs(0x3c)

    // when
    processor.execute()
    processor.execute()
    processor.execute()
    processor.execute()

    // then
    registerValue("a") shouldBe 0x02
    registerValue("pc") shouldBe 0x0038
  }

  "reti" should "return to the program counter" in new InterruptingMachine {
    // given
    routineAt(0x0000).does(0xfb,         // ei
                            0xed, 0x56,   // im 1
                            0x3c,         // inc a
                            0x04)        // inc b

    routineAt(0x0038).does(0x3c,         // inc a
                            0xed, 0x4d)  // reti

    anInterruptIsGenerated

    // when
    executeUntilNop()

    // then
    registerValue("a") shouldBe 2
    registerValue("b") shouldBe 1
  }
  
  "reti" should "notify devices of interrupt completion" in new Machine {
    pending
  }
  
  "im0" should "be invoked when maskable interrupts are enabled" in new Machine {
    pending
  }
  
  "im0" should "not be invoked when maskable interrupts are disabled" in new Machine {
    pending
  }
  
  "im1" should "be invoked when maskable interrupts are enabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreEnabled
    registerContainsValue("sp", 0xffff)

    maskableInterruptModeIs(1)
    nextInstructionIs(0xcb, 0xc0)

    anInterruptIsGenerated

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0038
    registerValue("sp") shouldBe 0xfffd
    memory.get(0xfffd) shouldBe 0x04
    memory.get(0xfffe) shouldBe 0x00
    registerValue("b") shouldBe 0
  }
  
  "im1" should "not be invoked when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    registerContainsValue("sp", 0xffff)

    maskableInterruptModeIs(1)
    nextInstructionIs(0xcb, 0xc0)

    anInterruptIsGenerated

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0005
    registerValue("sp") shouldBe 0xffff
    registerValue("b") shouldBe 0x01
  }
  
  "im2" should "be invoked when maskable interrupts are enabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreEnabled
    registerContainsValue("sp", 0xffff)

    maskableInterruptModeIs(2)
    nextInstructionIs(0xcb, 0xc0)

    memory.set(0xb0ff, 0xef)
    memory.set(0xb100, 0xbe)

    registerContainsValue("i", 0xb0)

    anInterruptIsGenerated

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0xbeef
    registerValue("sp") shouldBe 0xfffd
    memory.get(0xfffd) shouldBe 0x04
    memory.get(0xfffe) shouldBe 0x00
    registerValue("b") shouldBe 0x00
  }
  
  "im2" should "not be invoked when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    registerContainsValue("sp", 0xffff)

    maskableInterruptModeIs(2)
    nextInstructionIs(0xcb, 0xc0)

    memory.set(0xb0e0, 0xef)
    memory.set(0xb0e1, 0xbe)

    registerContainsValue("i", 0xb0)
    registerContainsValue("r", 0xe1)

    anInterruptIsGenerated

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0005
    registerValue("sp") shouldBe 0xffff
    registerValue("b") shouldBe 0x01
  }
  
  "nmi" should "be invoked when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    registerContainsValue("sp", 0xffff)

    // nmi service routine
    memory.set(0x0066, 0xcb)
    memory.set(0x0067, 0xc7)

    // when
    processor.nmi()
    anInterruptIsGenerated
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0068
    registerValue("a") shouldBe 0x01

    registerValue("sp") shouldBe 0xfffd
    memory.get(0xfffd) shouldBe 0x01
    memory.get(0xfffe) shouldBe 0x00
  }
  
  "nmi" should "be invoked when maskable interrupts are enabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreEnabled
    registerContainsValue("sp", 0xffff)

    // nmi service routine
    memory.set(0x0066, 0xcb)
    memory.set(0x0067, 0xc7)

    // when
    processor.nmi()
    anInterruptIsGenerated
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0068
    registerValue("a") shouldBe 0x01

    registerValue("sp") shouldBe 0xfffd
    memory.get(0xfffd) shouldBe 0x02
    memory.get(0xfffe) shouldBe 0x00
  }
  
  "halt" should "execute nops until a non-maskable interrupt" in new InterruptingMachine {
    // given
    val addressOfHalt = registerValue("pc")
    nextInstructionIs(0x76)

    routineAt(0x0066).does(0x3c,        // inc a
                            0xed, 0x45)  // retn

    // when
    processor.execute()
    registerValue("pc") shouldBe addressOfHalt

    var lastOp = processor.execute()
    registerValue("pc") shouldBe addressOfHalt
    lastOp.isInstanceOf[OpHalt] shouldBe true

    processor.nmi()
    lastOp = processor.execute()

    // then
    lastOp.isInstanceOf[OpHalt] shouldBe false
    registerValue("a") shouldBe 0x01
    registerValue("pc") shouldBe 0x0067
  }
  
  "halt" should "execute nops until a maskable interrupt when maskable interrupts are enabled" in new InterruptingMachine {
    // given
    routineAt(0x0038).does(0x3c,         // inc a
                            0xed, 0x4d)   // retn
    maskableInterruptModeIs(1)
    maskableInterruptsAreEnabled

    val addressOfHalt = registerValue("pc")
    nextInstructionIs(0x76)

    // when
    processor.execute()
    registerValue("pc") shouldBe addressOfHalt

    var lastOp = processor.execute()
    registerValue("pc") shouldBe addressOfHalt
    lastOp.isInstanceOf[OpHalt] shouldBe true

    anInterruptIsGenerated
    processor.execute()
    lastOp = processor.execute()

    // then
    lastOp.isInstanceOf[OpHalt] shouldBe false
    registerValue("a") shouldBe 0x01
    registerValue("pc") shouldBe 0x0039
  }
  
  "halt" should "execute nops despite maskable interrupt when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    routineAt(0x0038).does(0x3c,         // inc a
                            0xed, 0x4d)   // retn
    maskableInterruptModeIs(1)
    maskableInterruptsAreDisabled

    val addressOfHalt = registerValue("pc")
    nextInstructionIs(0x76)

    // when
    processor.execute()
    registerValue("pc") shouldBe addressOfHalt

    var lastOp = processor.execute()
    registerValue("pc") shouldBe addressOfHalt
    lastOp.isInstanceOf[OpHalt] shouldBe true

    anInterruptIsGenerated
    lastOp = processor.execute()

    // then
    registerValue("pc") shouldBe addressOfHalt
    lastOp.isInstanceOf[OpHalt] shouldBe true
  }
  
  "retn" should "re-enable maskable interrupts" in new InterruptingMachine {
    // given
    routineAt(0x0000).does(0xfb,         // ei
                            0xed, 0x56,   // im 0
                            0x3c,         // inc a
                            0x04)         // inc b

    routineAt(0x0066).does(0x3c,         // inc a
                            0xed, 0x45)   // retn

    routineAt(0x0038).does(0x3c,         // inc a
                            0xed, 0x4d)   // reti

    // when
    executeRange(0x0000, 0x0003)
    anInterruptIsGenerated
    processor.nmi()
    executeUntilNop()

    // then
    registerValue("a") shouldBe 3
    registerValue("b") shouldBe 1
  }
}
