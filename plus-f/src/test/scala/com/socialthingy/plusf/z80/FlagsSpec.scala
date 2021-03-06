package com.socialthingy.plusf.z80

import com.socialthingy.plusf.ProcessorSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class FlagsSpec extends ProcessorSpec with TableDrivenPropertyChecks {

  val truthValues = Table("value", true, false)

  forAll(truthValues) { value =>
    "ccf" should s"complement the carry flag when it is initially $value" in new Machine {
      // given
      registerContainsValue("a", binary("00101000"))
      flag("c") is value
      nextInstructionIs(0x3f)

      // when
      processor.execute()

      // then
      flag("h").value shouldBe value
      flag("c").value shouldBe (!value)
      flag("n").value shouldBe false
      flag("f3").value shouldBe true
      flag("f5").value shouldBe true
    }
  }

  forAll(truthValues) { value =>
    "scf" should s"set the carry flag when it is initially $value" in new Machine {
      // given
      registerContainsValue("a", binary("00101000"))
      flag("c") is value
      nextInstructionIs(0x37)

      // when
      processor.execute()

      // then
      flag("h").value shouldBe false
      flag("n").value shouldBe false
      flag("c").value shouldBe true
      flag("f3").value shouldBe true
      flag("f5").value shouldBe true
    }
  }
}
