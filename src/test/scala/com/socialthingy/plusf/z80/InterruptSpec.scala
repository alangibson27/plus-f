package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import com.socialthingy.plusf.z80.operations.Nop
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

    def anIm1InterruptIsGenerated: AckReceiver = {
      val ackReceiver = new AckReceiver()
      val request = new InterruptRequest(ackReceiver)
      processor.interrupt(request)
      ackReceiver
    }

    def anIm2InterruptIsGenerated: AckReceiver = {
      val interruptingDevice = new AckReceiver()
      val request = new InterruptRequest(interruptingDevice)
      processor.interrupt(request)
      interruptingDevice
    }

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

  class AckReceiver extends InterruptingDevice {
    var acknowledged = false

    override def acknowledge(): Unit = {
      acknowledged = true
    }
  }

  class RoutineBuilder(memory: Array[Int], var address: Int) {
    def does(op_codes: Int*): Unit = {
      op_codes foreach { opcode =>
        memory(address) = opcode
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
    }
  }

  "ei" should "enable interrupts after the following instruction" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    maskableInterruptModeIs(1)
    val interruptor = anIm1InterruptIsGenerated

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
    interruptor.acknowledged shouldBe true
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

    val interruptingDevice = anIm1InterruptIsGenerated

    // when
    executeUntilNop()

    // then
    interruptingDevice.acknowledged shouldBe true
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

    val interruptingDevice = anIm1InterruptIsGenerated

    // when
    processor.execute()

    // then
    interruptingDevice.acknowledged shouldBe true
    registerValue("pc") shouldBe 0x0038
    registerValue("sp") shouldBe 0xfffd
    memory(0xfffd) shouldBe 0x04
    memory(0xfffe) shouldBe 0x00
    registerValue("b") shouldBe 0
  }
  
  "im1" should "not be invoked when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    registerContainsValue("sp", 0xffff)

    maskableInterruptModeIs(1)
    nextInstructionIs(0xcb, 0xc0)

    val interruptingDevice = anIm1InterruptIsGenerated

    // when
    processor.execute()

    // then
    interruptingDevice.acknowledged shouldBe false
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

    memory(0xb0e0) = 0xef
    memory(0xb0e1) = 0xbe

    registerContainsValue("i", 0xb0)
    registerContainsValue("r", 0xe1)

    val interruptingDevice = anIm2InterruptIsGenerated

    // when
    processor.execute()

    // then
    interruptingDevice.acknowledged shouldBe true
    registerValue("pc") shouldBe 0xbeef
    registerValue("sp") shouldBe 0xfffd
    memory(0xfffd) shouldBe 0x04
    memory(0xfffe) shouldBe 0x00
    registerValue("b") shouldBe 0x00
  }
  
  "im2" should "not be invoked when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    registerContainsValue("sp", 0xffff)

    maskableInterruptModeIs(2)
    nextInstructionIs(0xcb, 0xc0)

    memory(0xb0e0) = 0xef
    memory(0xb0e1) = 0xbe

    registerContainsValue("i", 0xb0)
    registerContainsValue("r", 0xe1)

    val interruptingDevice = anIm2InterruptIsGenerated

    // when
    processor.execute()

    // then
    interruptingDevice.acknowledged shouldBe false
    registerValue("pc") shouldBe 0x0005
    registerValue("sp") shouldBe 0xffff
    registerValue("b") shouldBe 0x01
  }
  
  "nmi" should "be invoked when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreDisabled
    registerContainsValue("sp", 0xffff)

    // nmi service routine
    memory(0x0066) = 0xcb
    memory(0x0067) = 0xc7

    // when
    processor.nmi()
    anIm1InterruptIsGenerated
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0068
    registerValue("a") shouldBe 0x01

    registerValue("sp") shouldBe 0xfffd
    memory(0xfffd) shouldBe 0x01
    memory(0xfffe) shouldBe 0x00
  }
  
  "nmi" should "be invoked when maskable interrupts are enabled" in new InterruptingMachine {
    // given
    maskableInterruptsAreEnabled
    registerContainsValue("sp", 0xffff)

    // nmi service routine
    memory(0x0066) = 0xcb
    memory(0x0067) = 0xc7

    // when
    processor.nmi()
    anIm1InterruptIsGenerated
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0068
    registerValue("a") shouldBe 0x01

    registerValue("sp") shouldBe 0xfffd
    memory(0xfffd) shouldBe 0x02
    memory(0xfffe) shouldBe 0x00
  }
  
  "halt" should "execute nops until a non-maskable interrupt" in new InterruptingMachine {
    // given
    nextInstructionIs(0x76)

    routineAt(0x0066).does(0x3c,        // inc a
                            0xed, 0x45)  // retn

    // when
    processor.execute()

    var lastOp = processor.execute()
    lastOp.isInstanceOf[Nop] shouldBe true

    processor.nmi()
    lastOp = processor.execute()

    // then
    lastOp.isInstanceOf[Nop] shouldBe false
    registerValue("a") shouldBe 0x01
  }
  
  "halt" should "execute nops until a maskable interrupt when maskable interrupts are enabled" in new InterruptingMachine {
    // given
    routineAt(0x0038).does(0x3c,         // inc a
                            0xed, 0x4d)   // retn
    maskableInterruptModeIs(1)
    maskableInterruptsAreEnabled

    nextInstructionIs(0x76)

    // when
    processor.execute()

    var lastOp = processor.execute()
    lastOp.isInstanceOf[Nop] shouldBe true

    anIm1InterruptIsGenerated
    processor.execute()
    lastOp = processor.execute()

    // then
    lastOp.isInstanceOf[Nop] shouldBe false
    registerValue("a") shouldBe 0x01
  }
  
  "halt" should "execute nops despite maskable interrupt when maskable interrupts are disabled" in new InterruptingMachine {
    // given
    routineAt(0x0038).does(0x3c,         // inc a
                            0xed, 0x4d)   // retn
    maskableInterruptModeIs(1)
    maskableInterruptsAreDisabled

    nextInstructionIs(0x76)

    // when
    processor.execute()

    var lastOp = processor.execute()
    lastOp.isInstanceOf[Nop] shouldBe true

    anIm1InterruptIsGenerated
    lastOp = processor.execute()

    // then
    lastOp.isInstanceOf[Nop] shouldBe true
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
    val interruptingDevice = anIm1InterruptIsGenerated
    processor.nmi()
    executeUntilNop()

    // then
    interruptingDevice.acknowledged shouldBe true
    registerValue("a") shouldBe 3
    registerValue("b") shouldBe 1
  }
}
