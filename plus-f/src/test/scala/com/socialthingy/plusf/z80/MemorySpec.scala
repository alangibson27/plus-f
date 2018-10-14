package com.socialthingy.plusf.z80

import com.socialthingy.plusf.spectrum.Model
import org.scalatest.{Matchers, WordSpec}

class MemorySpec extends WordSpec with Matchers {

  "+2 memory" when {
    "configured" should {
      val memory = new Memory
      memory.configure(Model.PLUS_2)

      "have the editor ROM in slot 0" in {
        memory.get(0x0000) shouldBe 0xf3
      }
    }

    "switching ROM from page 0 to 1 and back again" should {
      "work properly" in new ConfiguredMemory {
        memory.setRomPage(1)
        memory.setRomPage(0)

        memory.get(0x0001) shouldBe 0x01
      }
    }

    "slot 0 is written to" should {
      "not make change when ROM 0 is present" in new ConfiguredMemory {
        memory.set(0x0001, 0xff)

        memory.get(0x0001) shouldBe 0x01
      }

      "not make change when ROM 1 is present" in new ConfiguredMemory {
        memory.setRomPage(1)

        memory.set(0x0001, 0xff)

        memory.get(0x0001) shouldBe 0xaf
      }
    }

    "slot 1 is written to" should {
      "replicate writes when page 5 is in slot 1 and slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(5)

        memory.set(0x4001, 0xff)

        memory.get(0x4001) shouldBe 0xff
        memory.get(0xc001) shouldBe 0xff
      }

      "not replicate writes when page 5 is in slot 1 another page is in slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(7)

        memory.set(0x4001, 0xff)

        memory.get(0x4001) shouldBe 0xff
        memory.get(0xc001) shouldBe 0x00
      }

      "record the screen as being changed if page 5 is the displayable screen" +
        "and the low part of slot 1 is written to" in new ConfiguredMemory {
        memory.setScreenPage(5)
        memory.markScreenDrawn()

        memory.set(0x4001, 0xff)

        memory.get(0x4001) shouldBe 0xff
        memory.screenChanged shouldBe true
      }

      "not record the screen as being changed if page 7 is the displayable screen" +
        "and the low part of slot 1 is written to" in new ConfiguredMemory {
        memory.setScreenPage(7)
        memory.markScreenDrawn()

        memory.set(0x4001, 0xff)

        memory.get(0x4001) shouldBe 0xff
        memory.screenChanged shouldBe false
      }

      "put the current state of page 5 into slot 3 when page 5 is subsequently swapped into slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(0)

        memory.set(0x4001, 0xbe)
        memory.setHighPageInMemory(5)

        memory.get(0xc001) shouldBe 0xbe
      }
    }

    "slot 2 is written to" should {
      "replicate writes when page 2 is in slot 2 and slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(2)

        memory.set(0x8001, 0xff)

        memory.get(0x8001) shouldBe 0xff
        memory.get(0xc001) shouldBe 0xff
      }

      "not replicate writes when page 2 is in slot 2 and another page is in slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(0)

        memory.set(0x8001, 0xff)

        memory.get(0x8001) shouldBe 0xff
        memory.get(0xc001) shouldBe 0x00
      }

      "put the current state of page 2 into slot 3 when page 2 is subsequently swapped into slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(0)

        memory.set(0x8001, 0xbe)
        memory.setHighPageInMemory(2)

        memory.get(0xc001) shouldBe 0xbe
      }
    }

    "slot 3 is written to" should {
      "replicate writes when page 5 is in slot 1 and slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(5)

        memory.set(0xc001, 0xff)

        memory.get(0x4001) shouldBe 0xff
        memory.get(0xc001) shouldBe 0xff
      }

      "not replicate writes when page 5 is in slot 1 another page is in slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(7)

        memory.set(0xc001, 0xff)

        memory.get(0xc001) shouldBe 0xff
        memory.get(0x4001) shouldBe 0x00
      }

      "replicate writes when page 2 is in slot 2 and slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(2)

        memory.set(0xc001, 0xff)

        memory.get(0x8001) shouldBe 0xff
        memory.get(0xc001) shouldBe 0xff
      }

      "not replicate writes when page 2 is in slot 2 and another page is in slot 3" in new ConfiguredMemory {
        memory.setHighPageInMemory(4)

        memory.set(0xc001, 0xff)

        memory.get(0x8001) shouldBe 0x00
        memory.get(0xc001) shouldBe 0xff
      }

      "record the screen as being changed if page 5 is in slot 3 and page 5 is the visible screen" in new ConfiguredMemory {
        memory.setScreenPage(5)
        memory.markScreenDrawn()
        memory.setHighPageInMemory(5)

        memory.set(0xc001, 0xff)

        memory.get(0xc001) shouldBe 0xff
        memory.get(0x4001) shouldBe 0xff
        memory.screenChanged shouldBe true
      }

      "record the screen as being changed if page 7 is in slot 3 and page 7 is the visible screen" in new ConfiguredMemory {
        memory.setScreenPage(7)
        memory.markScreenDrawn()
        memory.setHighPageInMemory(7)

        memory.set(0xc001, 0xff)

        memory.get(0xc001) shouldBe 0xff
        memory.get(0x4001) shouldBe 0x00
        memory.screenChanged shouldBe true
      }

      "not record the screen as being changed if page 5 is in slot 3 and page 7 is the visible screen" in new ConfiguredMemory {
        memory.setScreenPage(7)
        memory.markScreenDrawn()
        memory.setHighPageInMemory(5)

        memory.set(0xc001, 0xff)

        memory.get(0xc001) shouldBe 0xff
        memory.screenChanged shouldBe false
      }

      "not record the screen as being changed if page 7 is in slot 3 and page 5 is the visible screen" in new ConfiguredMemory {
        memory.setScreenPage(5)
        memory.markScreenDrawn()
        memory.setHighPageInMemory(7)

        memory.set(0xc001, 0xff)

        memory.get(0xc001) shouldBe 0xff
        memory.screenChanged shouldBe false
      }
    }

    "a page is written to, swapped out and swapped back in" should {
      "preserve the written changes" in new ConfiguredMemory {
        memory.setHighPageInMemory(4)

        memory.set(0xc001, 0xff)
        memory.setHighPageInMemory(7)
        memory.set(0xc001, 0xfe)
        memory.setHighPageInMemory(4)

        memory.get(0xc001) shouldBe 0xff
      }
    }
  }

  trait ConfiguredMemory {
    val memory = new Memory
    memory.configure(Model.PLUS_2)
  }
}
