package com.socialthingy.qaopm.z80

import com.socialthingy.qaopm.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class IOSpec extends ProcessorSpec with TableDrivenPropertyChecks {
  
  "in a, (n)" should "read a value from a port" in new Machine {
    // given
    readFromPort(0xff, 0xbe) returns 0xaa

    registerContainsValue("a", 0xbe)
    nextInstructionIs(0xdb, 0xff)

    // when
    processor.execute()

    // then
    registerValue("a") shouldBe 0xaa
  }  
  
  val inOperations = Table(
    ("opcode", "register"),
    (0x40, "b"), (0x48, "c"), (0x50, "d"), (0x58, "e"),
    (0x60, "h"), (0x68, "l"), (0x78, "a")
  )
  
  forAll(inOperations) { (opcode, register) =>
    s"in $register, (c)" should "read the correct value into the register" in new Machine {
      // given
      readFromPort(0xfe, 0xef) returns 0xaa

      registerContainsValue("b", 0xef)
      registerContainsValue("c", 0xfe)

      nextInstructionIs(0xed, opcode)

      // when
      processor.execute()

      // then
      registerValue(register) shouldBe 0xaa
      flag("s").value shouldBe true
      flag("z").value shouldBe false
      flag("h").value shouldBe false
      flag("p").value shouldBe true
      flag("n").value shouldBe false
    }
  }  
  
  "ini" should "read a value and correctly decrement b to zero" in new Machine {
    // given
    readFromPort(0xef, 0x01) returns 0xaa

    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xa2)

    // when
    processor.execute()

    // then
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }
  
  it should "read a value and correctly decrement to non-zero" in new Machine {
    // given
    readFromPort(0xef, 0x10) returns 0xaa

    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xa2)

    // when
    processor.execute()

    // then
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe false
    flag("n").value shouldBe true
  }
  
  "inir" should "read a value and not repeat when b decrements to zero" in new Machine {
    // given
    readFromPort(0xef, 0x01) returns 0xaa

    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xb2)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0002
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }
  
  it should "read a value and repeat when b decrements to non-zero" in new Machine {
    // given
    readFromPort(0xef, 0x10) returns 0xaa

    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xb2)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }
  
  "ind" should "read a value and correctly decrement b to zero" in new Machine {
    // given
    readFromPort(0xef, 0x01) returns 0xaa

    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xaa)

    // when
    processor.execute()

    // then
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }
  
  it should "read a value and correctly decrement b to non-zero" in new Machine {
    // given
    readFromPort(0xef, 0x10) returns 0xaa

    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xaa)

    // when
    processor.execute()

    // then
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe false
    flag("n").value shouldBe true
  }
  
  "indr" should "read a value and not repeat when b decrements to zero" in new Machine {
    // given
    readFromPort(0xef, 0x01) returns 0xaa

    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xba)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0002
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }
  
  it should "read a value and repeat when b decrements to non-zero" in new Machine {
    // given
    readFromPort(0xef, 0x10) returns 0xaa

    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)

    nextInstructionIs(0xed, 0xba)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0000
    memory(0xbeef) shouldBe 0xaa
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }
  
  "out (n), a" should "write a value" in new Machine {
    // given
    registerContainsValue("a", 0xbe)
    nextInstructionIs(0xd3, 0xff)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xff, 0xbe, 0xbe)
  }

  "out (c), 0" should "write zero to the port" in new Machine {
    // given
    registerContainsValue("b", 0xbe)
    registerContainsValue("c", 0xef)

    nextInstructionIs(0xed, 0x71)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0xbe, 0x00)
  }

  val outOperations = Table(
    ("opcode", "register"),
    (0x41, "b"), (0x49, "c"), (0x51, "d"), (0x59, "e"),
    (0x61, "h"), (0x69, "l"), (0x79, "a")
  )

  forAll(outOperations) { (opcode, register) =>
    s"out (c), $register" should "write a value to the correct port" in new Machine {
      // given
      registerContainsValue(register, 0xef)
      registerContainsValue("b", 0xef)
      registerContainsValue("c", 0xfe)

      nextInstructionIs(0xed, opcode)

      // when
      processor.execute()

      // then
      if (register.equals("c")) {
        verifyPortWrite(0xfe, 0xef, 0xfe)
      } else {
        verifyPortWrite(0xfe, 0xef, 0xef)
      }
    }
  }

  "outi" should "write a value and correctly decrement b to zero" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xa3)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x00, 0xaa)
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }

  it should "read a value and correctly decrement b to non-zero" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xa3)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x0f, 0xaa)
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe false
    flag("n").value shouldBe true
  }

  "otir" should "read a value and not repeat when b decrements to zero" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xb3)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x00, 0xaa)
    registerValue("pc") shouldBe 0x0002
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }

  it should "read a value and repeat when b decrements to non-zero" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xb3)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x0f, 0xaa)
    registerValue("pc") shouldBe 0x0000
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbef0
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }

  "outd" should "write a value and correctly decrement b to zero" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xab)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x00, 0xaa)
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }

  it should "read a value and correctly decrement b to non-zero" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xab)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x0f, 0xaa)
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe false
    flag("n").value shouldBe true
  }

  "otdr" should "read a value and not repeat when b decrements to zero" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xbb)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x00, 0xaa)
    registerValue("pc") shouldBe 0x0002
    registerValue("b") shouldBe 0x00
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }

  it should "read a value and repeat when b decrements to non-zero" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    registerContainsValue("c", 0xef)

    registerContainsValue("hl", 0xbeef)
    memory(0xbeef) = 0xaa

    nextInstructionIs(0xed, 0xbb)

    // when
    processor.execute()

    // then
    verifyPortWrite(0xef, 0x0f, 0xaa)
    registerValue("pc") shouldBe 0x0000
    registerValue("b") shouldBe 0x0f
    registerValue("hl") shouldBe 0xbeee
    flag("z").value shouldBe true
    flag("n").value shouldBe true
  }
}
