package com.socialthingy.qaopm.z80

class NopSpec extends ProcessorSpec {

  "nop" should "do nothing" in new Machine {
    // given
    nextInstructionIs(0x00)

    // when
    processor.execute()

    // then
    registerValue("pc") shouldBe 0x0001
  }

}
