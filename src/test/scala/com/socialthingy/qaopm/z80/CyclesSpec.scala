package com.socialthingy.qaopm.z80

import org.scalatest.prop.TableDrivenPropertyChecks

class CyclesSpec extends ProcessorSpec with TableDrivenPropertyChecks {
  
  val operations = Table(
    ("opcode", "cycles"),
    (List(0x03), 6),                        // inc bc
    (List(0x78), 4),                        // ld a, b
    (List(0x3e, 0x01), 7),                  // ld a, 1
    (List(0x7e), 7),                        // ld a, (hl)
    (List(0xdd, 0x7e, 0x01), 19),           // ld a, (ix + 1)
    (List(0x77), 7),                        // ld (hl), a
    (List(0xdd, 0x77, 0x01), 19),           // ld (ix + 1), a
    (List(0x36, 0x05), 10),                 // ld (hl), 5
    (List(0xdd, 0x36, 0x01, 0x05), 19),     // ld (ix + 1), 5
    (List(0x0a), 7),                        // ld a, (bc)
    (List(0x3a, 0xef, 0xbe), 13),           // ld a, (0xbeef)
    (List(0x02), 7),                        // ld (bc), a
    (List(0x32, 0xef, 0xbe), 13),           // ld (0xbeef), a
    (List(0xed, 0x57), 9),                  // ld a, i
    (List(0xed, 0x5f), 9),                  // ld a, r
    (List(0xed, 0x47), 9),                  // ld i, a
    (List(0xed, 0x4f), 9),                  // ld r, a
    (List(0x01, 0xef, 0xbe), 10),           // ld bc, 0xbeef
    (List(0xdd, 0x21, 0xef, 0xbe), 14),     // ld ix, 0xbeef
    (List(0x2a, 0xef, 0xbe), 16),           // ld hl, (0xbeef)
    (List(0xed, 0x4b, 0xef, 0xbe), 20),     // ld bc, (0xbeef)
    (List(0xdd, 0x2a, 0xef, 0xbe), 20),     // ld ix, (0xbeef)
    (List(0x22, 0xef, 0xbe), 16),           // ld (0xbeef), hl
    (List(0xed, 0x43, 0xef, 0xbe), 20),     // ld (0xbeef), bc
    (List(0xdd, 0x22, 0xef, 0xbe), 20),     // ld (0xbeef), ix
    (List(0xf9), 6),                        // ld sp, hl
    (List(0xdd, 0xf9), 10),                 // ld sp, ix
    (List(0xe5), 11),                       // push hl
    (List(0xdd, 0xe5), 15),                 // push ix
    (List(0xe1), 10),                       // pop hl
    (List(0xdd, 0xe1), 14),                 // pop ix
    (List(0xeb), 4),                        // ex de, hl
    (List(0x08), 4),                        // ex af, af"
    (List(0xd9), 4),                        // exx
    (List(0xe3), 19),                       // ex (sp), hl
    (List(0xdd, 0xe3), 23),                 // ex (sp), ix
    (List(0xed, 0xa0), 16),                 // ldi
    (List(0xed, 0xa8), 16),                 // ldd
    (List(0xed, 0xa1), 16),                 // cpi
    (List(0xed, 0xa9), 16),                 // cpd
    (List(0x80), 4),                        // add a, b
    (List(0xc6, 0x05), 7),                  // add a, 5
    (List(0x86), 7),                        // add a, (hl)
    (List(0xdd, 0x86, 0x01), 19),           // add a, (ix + 1)
    (List(0x88), 4),                        // adc a, b
    (List(0xce, 0x05), 7),                  // adc a, 5
    (List(0x8e), 7),                        // adc a, (hl)
    (List(0xdd, 0x8e, 0x01), 19),           // adc a, (ix + 1)
    (List(0x97), 4),                        // sub a
    (List(0xd6, 0x01), 7),                  // sub 1
    (List(0x96), 7),                        // sub (hl)
    (List(0xdd, 0x96, 0x05), 19),           // sub (ix + 5)
    (List(0x98), 4),                        // sbc a, b
    (List(0xde, 0x05), 7),                  // sbc a, 5
    (List(0x9e), 7),                        // sbc a, (hl)
    (List(0xfd, 0x9e, 0x06), 19),           // sbc a, (iy + 6)
    (List(0xa0), 4),                        // and b
    (List(0xe6, 0x10), 7),                  // and 0x10
    (List(0xa6), 7),                        // and (hl)
    (List(0xdd, 0xa6, 0x01), 19),           // and (ix + 1)
    (List(0xb0), 4),                        // or b
    (List(0xf6, 0x10), 7),                  // or 0x10
    (List(0xb6), 7),                        // or (hl)
    (List(0xdd, 0xb6, 0x05), 19),           // or (ix + 5)
    (List(0xa8), 4),                        // xor b
    (List(0xee, 0x10), 7),                  // xor 0x10
    (List(0xae), 7),                        // xor (hl)
    (List(0xdd, 0xae, 0x10), 19),           // xor (ix + 0x10)
    (List(0xb8), 4),                        // cp b
    (List(0xfe, 0x10), 7),                  // cp 0x10
    (List(0xbe), 7),                        // cp (hl)
    (List(0xdd, 0xbe, 0x05), 19),           // cp (ix + 5)
    (List(0x0c), 4),                        // inc c
    (List(0x34), 11),                       // inc (hl)
    (List(0xdd, 0x34, 0x05), 23),           // inc (ix + 5)
    (List(0x0d), 4),                        // dec c
    (List(0x35), 11),                       // dec (hl)
    (List(0xdd, 0x35, 0x05), 23),           // dec (ix + 5)
    (List(0x27), 4),                        // daa
    (List(0x2f), 4),                        // cpl
    (List(0xed, 0x44), 8),                  // neg
    (List(0x3f), 4),                        // ccf
    (List(0x37), 4),                        // scf
    (List(0x00), 4),                        // nop
    (List(0x76), 4),                        // halt
    (List(0xf3), 4),                        // di
    (List(0xfb), 4),                        // ei
    (List(0xed, 0x46), 8),                  // im 0
    (List(0xed, 0x56), 8),                  // im 1
    (List(0xed, 0x5e), 8),                  // im 2
    (List(0x09), 11),                       // add hl, bc
    (List(0xed, 0x4a), 15),                 // adc hl, bc
    (List(0xed, 0x42), 15),                 // sbc hl, bc
    (List(0xdd, 0x09), 15),                 // add ix, bc
    (List(0x23), 6),                        // inc hl
    (List(0xdd, 0x23), 10),                 // inc ix
    (List(0x2b), 6),                        // dec hl
    (List(0xdd, 0x2b), 10),                 // dec ix
    (List(0x07), 4),                        // rlca
    (List(0x17), 4),                        // rla
    (List(0x0f), 4),                        // rrca
    (List(0x1f), 4),                        // rra
    (List(0xcb, 0x00), 8),                  // rlc b
    (List(0xcb, 0x06), 15),                 // rlc (hl)
    (List(0xdd, 0xcb, 0x01, 0x06), 23),     // rlc (ix + 1)
    (List(0xcb, 0x10), 8),                  // rl b
    (List(0xcb, 0x16), 15),                 // rl (hl)
    (List(0xdd, 0xcb, 0x01, 0x16), 23),     // rl (ix + 1)
    (List(0xcb, 0x08), 8),                  // rrc b
    (List(0xcb, 0x0e), 15),                 // rrc (hl)
    (List(0xdd, 0xcb, 0x01, 0x0e), 23),     // rrc (ix + 1)
    (List(0xcb, 0x18), 8),                  // rr b
    (List(0xcb, 0x1e), 15),                 // rr (hl)
    (List(0xdd, 0xcb, 0x01, 0x1e), 23),     // rr (ix + 1)
    (List(0xcb, 0x20), 8),                  // sla b
    (List(0xcb, 0x26), 15),                 // sla (hl)
    (List(0xdd, 0xcb, 0x02, 0x26), 23),     // sla (ix + 2)
    (List(0xcb, 0x28), 8),                  // sra b
    (List(0xcb, 0x2e), 15),                 // sra (hl)
    (List(0xdd, 0xcb, 0x02, 0x2e), 23),     // sra (ix + 2)
    (List(0xcb, 0x38), 8),                  // srl b
    (List(0xcb, 0x3e), 15),                 // srl (hl)
    (List(0xdd, 0xcb, 0x03, 0x3e), 23),     // srl (ix + 3)
    (List(0xed, 0x6f), 18),                 // rld
    (List(0xed, 0x67), 18),                 // rrd
    (List(0xcb, 0x41), 8),                  // bit 0, c
    (List(0xcb, 0x46), 12),                 // bit 0, (hl)
    (List(0xdd, 0xcb, 0x01, 0x46), 20),     // bit 0, (ix + 1)
    (List(0xcb, 0xc1), 8),                  // set 0, c
    (List(0xcb, 0xc6), 15),                 // set 0, (hl)
    (List(0xdd, 0xcb, 0x01, 0xc6), 23),     // set 0, (ix + 1)
    (List(0xcb, 0x81), 8),                  // res 0, c
    (List(0xcb, 0x86), 15),                 // res 0, (hl)
    (List(0xdd, 0xcb, 0x01, 0x86), 23),     // res 0, (ix + 1)
    (List(0xc3, 0xef, 0xbe), 10),           // jp 0xbeef
    (List(0xc2, 0xef, 0xbe), 10),           // jp nz, 0xbeef
    (List(0xca, 0xef, 0xbe), 10),           // jp z, 0xbeef
    (List(0xd2, 0xef, 0xbe), 10),           // jp nc, 0xbeef
    (List(0xda, 0xef, 0xbe), 10),           // jp c, 0xbeef
    (List(0xe2, 0xef, 0xbe), 10),           // jp po, 0xbeef
    (List(0xea, 0xef, 0xbe), 10),           // jp pe, 0xbeef
    (List(0xf2, 0xef, 0xbe), 10),           // jp p, 0xbeef
    (List(0xfa, 0xef, 0xbe), 10),           // jp m, 0xbeef
    (List(0x18, 0x01), 12),                 // jr 1
    (List(0xe9), 4),                        // jp (hl)
    (List(0xdd, 0xe9), 8),                  // jp (ix)
    (List(0xcd, 0xef, 0xbe), 17),           // call 0xbeef
    (List(0xc9), 10),                       // ret
    (List(0xed, 0x4d), 14),                 // reti
    (List(0xed, 0x45), 14),                 // retn
    (List(0xc7), 11),                       // rst 0x00
    (List(0xdb, 0xff), 11),                 // in a, (0xff)
    (List(0xed, 0x40), 12),                 // in b, (c)
    (List(0xed, 0xa2), 16),                 // ini
    (List(0xed, 0xaa), 16),                 // ind
    (List(0xd3, 0xff), 11),                 // out (0xff), a
    (List(0xed, 0x41), 12),                 // out (c), b
    (List(0xed, 0x71), 12)                 // out (c), 0
  )

  forAll(operations) { (opcode, cycles) =>
    s"executing operation ${opcode.map(x => x.toHexString).mkString(" ")}" should s"take $cycles cycles" in new Machine {
      opcode foreach { nextInstructionIs(_) }

      processor.execute()

      processor.lastTime() shouldBe cycles
    }
  }
  
  "execute on a block operation when bc decrements to zero" should "return the correct number of cycles" in new Machine {
    // given
    nextInstructionIs(0xed, 0xb0)
    registerContainsValue("hl", 0xbeef)
    registerContainsValue("de", 0xbabe)
    registerContainsValue("bc", 0x0001)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 16
  }
  
  "execute on a block operation when bc decrements to non-zero" should "return the correct number of cycles" in new Machine {
    // given
    nextInstructionIs(0xed, 0xb8)
    registerContainsValue("hl", 0xbeef)
    registerContainsValue("de", 0xbabe)
    registerContainsValue("bc", 0x0010)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 21
  }
  
  "execute on jr c when carry is set" should "return the correct number of cycles" in new Machine {
    // given
    flag("c") is true
    nextInstructionIs(0x38, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 12
  }

  "execute on jr c when carry is reset" should "return the correct number of cycles" in new Machine {
    // given
    flag("c") is false
    nextInstructionIs(0x38, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 7
  }

  "execute on jr nc when carry is set" should "return the correct number of cycles" in new Machine {
    // given
    flag("c") is true
    nextInstructionIs(0x30, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 7
  }

  "execute on jr nc when carry is reset" should "return the correct number of cycles" in new Machine {
    // given
    flag("c") is false
    nextInstructionIs(0x30, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 12
  }

  "execute on jr z when z is set" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is true
    nextInstructionIs(0x28, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 12
  }

  "execute on jr z when z is reset" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is false
    nextInstructionIs(0x28, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 7
  }

  "execute on jr nz when z is set" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is true
    nextInstructionIs(0x20, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 7
  }

  "execute on jr nz when z is reset" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is false
    nextInstructionIs(0x20, 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 12
  }

  "execute on djnz when b decrements to zero" should "return the correct number of cycles" in new Machine {
    // given
    nextInstructionIs(0x10, 0xa0)
    registerContainsValue("b", 1)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 8
  }

  "execute on djnz when b decrements to non-zero" should "return the correct number of cycles" in new Machine {
    // given
    nextInstructionIs(0x10, 0xa0)
    registerContainsValue("b", 0x10)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 13
  }

  "execute on call z when z is set" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is true
    nextInstructionIs(0xcc, 0xef, 0xbe)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 5
  }

  "execute on call z when z is reset" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is false
    nextInstructionIs(0xcc, 0xef, 0xbe)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 3
  }

  "execute on ret z when z is set" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is true
    nextInstructionIs(0xc8)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 11
  }

  "execute on ret z when z is reset" should "return the correct number of cycles" in new Machine {
    // given
    flag("z") is false
    nextInstructionIs(0xc8)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 5
  }

  "execute on inir when b decrements to zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    nextInstructionIs(0xed, 0xb2)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 16
  }

  "execute on inir when b decrements to non-zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    nextInstructionIs(0xed, 0xb2)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 21
  }

  "execute on indr when b decrements to zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    nextInstructionIs(0xed, 0xba)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 16
  }

  "execute on indr when b decrements to non-zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    nextInstructionIs(0xed, 0xba)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 21
  }

  "execute on otir when b decrements to zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    nextInstructionIs(0xed, 0xb3)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 16
  }

  "execute on otir when b decrements to non-zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    nextInstructionIs(0xed, 0xb3)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 21
  }

  "execute on otdr when b decrements to zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x01)
    nextInstructionIs(0xed, 0xbb)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 16
  }

  "execute on otdr when b decrements to non-zero" should "return the correct number of cycles" in new Machine {
    // given
    registerContainsValue("b", 0x10)
    nextInstructionIs(0xed, 0xbb)

    // when
    processor.execute()

    // then
    processor.lastTime() shouldBe 21
  }
}
