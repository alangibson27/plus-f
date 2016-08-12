package com.socialthingy.plusf.z80

import com.socialthingy.plusf.spectrum.Model
import org.scalatest.{Matchers, WordSpec}

class MemorySpec extends WordSpec with Matchers {

  "+2 memory" when {
    "configured" should {
      val addressableMemory = Memory.configure(Model.PLUS_2)

      "have the editor ROM in slot 0" in {
        addressableMemory(0x0000) shouldBe 0xf3
      }
    }

    "slot 1 is written to" should {
      "replicate writes when page 5 is in slot 1 and slot 3" in new ConfiguredMemory {
        Memory.setHighPage(addressableMemory, 5)

        Memory.set(addressableMemory, 0x4001, 0xff)

        addressableMemory(0x4001) shouldBe 0xff
        addressableMemory(0xc001) shouldBe 0xff
      }

      "not replicate writes when page 5 is in slot 1 another page is in slot 3" in new ConfiguredMemory {
        Memory.setHighPage(addressableMemory, 7)

        Memory.set(addressableMemory, 0x4001, 0xff)

        addressableMemory(0x4001) shouldBe 0xff
        addressableMemory(0xc001) shouldBe 0x00
      }

      "record the screen as being changed if page 5 is the displayable screen" +
        "and the low part of slot 1 is written to" in new ConfiguredMemory {
        Memory.setScreenPage(5)
        Memory.markScreenDrawn()

        Memory.set(addressableMemory, 0x4001, 0xff)

        addressableMemory(0x4001) shouldBe 0xff
        Memory.screenChanged shouldBe true
      }

      "not record the screen as being changed if page 7 is the displayable screen" +
        "and the low part of slot 1 is written to" in new ConfiguredMemory {
        Memory.setScreenPage(7)
        Memory.markScreenDrawn()

        Memory.set(addressableMemory, 0x4001, 0xff)

        addressableMemory(0x4001) shouldBe 0xff
        Memory.screenChanged shouldBe false
      }
    }

    "slot 3 is written to" should {
      "replicate writes when page 5 is in slot 1 and slot 3" in new ConfiguredMemory {
        Memory.setHighPage(addressableMemory, 5)

        Memory.set(addressableMemory, 0xc001, 0xff)

        addressableMemory(0x4001) shouldBe 0xff
        addressableMemory(0xc001) shouldBe 0xff
      }

      "not replicate writes when page 5 is in slot 1 another page is in slot 3" in new ConfiguredMemory {
        Memory.setHighPage(addressableMemory, 7)

        Memory.set(addressableMemory, 0xc001, 0xff)

        addressableMemory(0xc001) shouldBe 0xff
        addressableMemory(0x4001) shouldBe 0x00
      }

      "record the screen as being changed if page 5 is in slot 3 and page 5 is the visible screen" in new ConfiguredMemory {
        Memory.setScreenPage(5)
        Memory.markScreenDrawn()
        Memory.setHighPage(addressableMemory, 5)

        Memory.set(addressableMemory, 0xc001, 0xff)

        addressableMemory(0xc001) shouldBe 0xff
        addressableMemory(0x4001) shouldBe 0xff
        Memory.screenChanged shouldBe true
      }

      "record the screen as being changed if page 7 is in slot 3 and page 7 is the visible screen" in new ConfiguredMemory {
        Memory.setScreenPage(7)
        Memory.markScreenDrawn()
        Memory.setHighPage(addressableMemory, 7)

        Memory.set(addressableMemory, 0xc001, 0xff)

        addressableMemory(0xc001) shouldBe 0xff
        addressableMemory(0x4001) shouldBe 0x00
        Memory.screenChanged shouldBe true
      }

      "not record the screen as being changed if page 5 is in slot 3 and page 7 is the visible screen" in new ConfiguredMemory {
        Memory.setScreenPage(7)
        Memory.markScreenDrawn()
        Memory.setHighPage(addressableMemory, 5)

        Memory.set(addressableMemory, 0xc001, 0xff)

        addressableMemory(0xc001) shouldBe 0xff
        Memory.screenChanged shouldBe false
      }

      "not record the screen as being changed if page 7 is in slot 3 and page 5 is the visible screen" in new ConfiguredMemory {
        Memory.setScreenPage(5)
        Memory.markScreenDrawn()
        Memory.setHighPage(addressableMemory, 7)

        Memory.set(addressableMemory, 0xc001, 0xff)

        addressableMemory(0xc001) shouldBe 0xff
        Memory.screenChanged shouldBe false
      }
    }
  }

  trait ConfiguredMemory {
    val addressableMemory = Memory.configure(Model.PLUS_2)
  }
}
