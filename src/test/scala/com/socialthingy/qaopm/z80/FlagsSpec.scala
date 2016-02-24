package com.socialthingy.qaopm.z80

import org.scalatest.prop.TableDrivenPropertyChecks

class FlagsSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val truthValues = Table("value", true, false)

  forAll(truthValues) { value =>
    "ccf" should s"complement the carry flag when it is initially $value" in new Machine {
      // given
      flag("c") is value
      nextInstructionIs(0x3f)

      // when
      processor.execute()

      // then
      flag("h").value shouldBe value
      flag("c").value shouldBe (!value)
      flag("n").value shouldBe false
    }
  }

  forAll(truthValues) { value =>
    "scf" should s"set the carry flag when it is initially $value" in new Machine {
      // given
      flag("c") is value
      nextInstructionIs(0x37)

      // when
      processor.execute()

      // then
      flag("h").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe true
    }
  }
}
