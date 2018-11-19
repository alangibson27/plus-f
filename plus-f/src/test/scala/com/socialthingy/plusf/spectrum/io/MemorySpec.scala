package com.socialthingy.plusf.spectrum.io

import com.socialthingy.plusf.spectrum.Model
import com.socialthingy.plusf.z80.Clock
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

class MemorySpec extends WordSpec with Matchers with TableDrivenPropertyChecks {

  val highBank0 = 0
  val highBank2 = 2
  val highBank5 = 5
  val highBank7 = 7
  val screenBank5 = 0
  val screenBank7 = 8
  val romBank0 = 0
  val romBank1 = 16
  val pagingDisabled = 32
  val plus2SpecialPagingMode = 1

  val clock = new Clock
  val memoryTable = Table(
    ("memory type", "memory"),
    ("48k", new Memory48K()),
    ("128k", new Memory128K(Model._128K)),
    ("+2A", new MemoryPlus2A())
  )

  val pageableMemoryTable = Table(
    ("memory type", "memory"),
    ("128k", () => { clock.reset(); new Memory128K(Model._128K) }),
    ("+2A", () => { clock.reset(); new MemoryPlus2A() })
  )

  forAll(pageableMemoryTable) { (memoryType, memoryCreator) =>
    s"$memoryType memory" when {
      "created" should {
        "have the editor ROM in page 0" in {
          val memory = memoryCreator()

          memory.get(0x0000) shouldBe 0xf3
        }
      }

      "switching ROM to bank 1 and back to bank 0 again" should {
        "work properly" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, romBank1)
          memory.get(0x0001) should not be 0x01

          memory.write(0xfd, 0x7f, romBank0)
          memory.get(0x0001) shouldBe 0x01
        }
      }

      "page 0 is written to" should {
        "not make change when ROM 0 is present" in {
          val memory = memoryCreator()

          memory.set(0x0001, 0xff)

          memory.get(0x0001) shouldBe 0x01
        }

        "not make change when ROM 1 is present" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, romBank1)

          val original = memory.get(0x0001)
          memory.set(0x0001, 0xff)

          memory.get(0x0001) shouldBe original
        }
      }

      "page 1 is written to" should {
        "replicate writes when bank 5 is in page 1 and page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank5)

          memory.set(0x4001, 0xff)

          memory.get(0x4001) shouldBe 0xff
          memory.get(0xc001) shouldBe 0xff
        }

        "not replicate writes when bank 5 is in page 1 another page is in page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank7)

          memory.set(0x4001, 0xff)

          memory.get(0x4001) shouldBe 0xff
          memory.get(0xc001) shouldBe 0x00
        }

        "put the current state of bank 5 into page 3 when bank 5 is subsequently swapped into page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank0)

          memory.set(0x4001, 0xbe)
          memory.write(0xfd, 0x7f, highBank5)

          memory.get(0xc001) shouldBe 0xbe
        }
      }

      "page 2 is written to" should {
        "replicate writes when bank 2 is in page 2 and page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank2)

          memory.set(0x8001, 0xff)

          memory.get(0x8001) shouldBe 0xff
          memory.get(0xc001) shouldBe 0xff
        }

        "not replicate writes when bank 2 is in page 2 and another bank is in page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank0)

          memory.set(0x8001, 0xff)

          memory.get(0x8001) shouldBe 0xff
          memory.get(0xc001) shouldBe 0x00
        }

        "put the current state of bank 2 into page 3 when bank 2 is subsequently swapped into page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank0)

          memory.set(0x8001, 0xbe)
          memory.write(0xfd, 0x7f, highBank2)

          memory.get(0xc001) shouldBe 0xbe
        }
      }

      "page 3 is written to" should {
        "replicate writes when bank 5 is in page 1 and page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank5)

          memory.set(0xc001, 0xff)

          memory.get(0x4001) shouldBe 0xff
          memory.get(0xc001) shouldBe 0xff
        }

        "not replicate writes when bank 5 is in page 1 another bank is in page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank0)

          memory.set(0xc001, 0xff)

          memory.get(0xc001) shouldBe 0xff
          memory.get(0x4001) shouldBe 0x00
        }

        "replicate writes when bank 2 is in page 2 and page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank2)

          memory.set(0xc001, 0xff)

          memory.get(0x8001) shouldBe 0xff
          memory.get(0xc001) shouldBe 0xff
        }

        "not replicate writes when bank 2 is in page 2 and another bank is in page 3" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank5)

          memory.set(0xc001, 0xff)

          memory.get(0x8001) shouldBe 0x00
          memory.get(0xc001) shouldBe 0xff
        }
      }

      "a bank is written to, swapped out and swapped back in" should {
        "preserve the written changes" in {
          val memory = memoryCreator()

          memory.write(0xfd, 0x7f, highBank2)

          memory.set(0xc001, 0xff)
          memory.write(0xfd, 0x7f, highBank7)
          memory.set(0xc001, 0xfe)
          memory.write(0xfd, 0x7f, highBank2)

          memory.get(0xc001) shouldBe 0xff
        }
      }
    }
  }

  val specialPagingModeTable = Table(
    ("mode", "mode value", "page 0", "page 1", "page 2", "page 3"),
    ("0", 0, 0, 1, 2, 3),
    ("1", 2, 4, 5, 6, 7),
    ("2", 4, 4, 5, 6, 3),
    ("3", 6, 4, 7, 6, 3)
  )

  val romPagingModeTable = Table(
    ("rom bank", "low bit", "high bit", "marker value"),
    (0, 0, 0, 0xf3),
    (1, 1, 0, 0x53),
    (2, 0, 1, 0x00),
    (3, 1, 1, 0xf3)
  )

  "+2A memory" should {
    forAll(specialPagingModeTable) { (mode, modeValue, page0, page1, page2, page3) =>
      s"switch to special mode $mode correctly" in {
        val memory = new MemoryPlus2A(true)

        memory.write(0xfd, 0x1f, plus2SpecialPagingMode + modeValue)

        memory.get(0x0000) shouldBe page0
        memory.get(0x4000) shouldBe page1
        memory.get(0x8000) shouldBe page2
        memory.get(0xc000) shouldBe page3
      }
    }

    forAll(romPagingModeTable) { (romBank, lowBit, highBit, marker) =>
      s"select ROM $romBank correctly" in {
        val memory = new MemoryPlus2A()

        memory.write(0xfd, 0x7f, lowBit << 4)
        memory.write(0xfd, 0x1f, highBit << 2)

        memory.get(0x0000) shouldBe marker
      }
    }
  }
}
