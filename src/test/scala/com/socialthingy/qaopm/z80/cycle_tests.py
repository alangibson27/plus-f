from nose.tools import assert_equals
from processor_tests import TestHelper


class TestCycles(TestHelper):
    def test_execute_returns_correct_number_of_tstates_for_each_operation_type(self):
        values = [
            ([0x03], 6),                        # inc bc
            ([0x78], 4),                        # ld a, b
            ([0x3e, 0x01], 7),                  # ld a, 1
            ([0x7e], 7),                        # ld a, (hl)
            ([0xdd, 0x7e, 0x01], 19),           # ld a, (ix + 1)
            ([0x77], 7),                        # ld (hl), a
            ([0xdd, 0x77, 0x01], 19),           # ld (ix + 1), a
            ([0x36, 0x05], 10),                 # ld (hl), 5
            ([0xdd, 0x36, 0x01, 0x05], 19),     # ld (ix + 1), 5
            ([0x0a], 7),                        # ld a, (bc)
            ([0x3a, 0xef, 0xbe], 13),           # ld a, (0xbeef)
            ([0x02], 7),                        # ld (bc), a
            ([0x32, 0xef, 0xbe], 13),           # ld (0xbeef), a
            ([0xed, 0x57], 9),                  # ld a, i
            ([0xed, 0x5f], 9),                  # ld a, r
            ([0xed, 0x47], 9),                  # ld i, a
            ([0xed, 0x4f], 9),                  # ld r, a
            ([0x01, 0xef, 0xbe], 10),           # ld bc, 0xbeef
            ([0xdd, 0x21, 0xef, 0xbe], 14),     # ld ix, 0xbeef
            ([0x2a, 0xef, 0xbe], 16),           # ld hl, (0xbeef)
            ([0xed, 0x4b, 0xef, 0xbe], 20),     # ld bc, (0xbeef)
            ([0xdd, 0x2a, 0xef, 0xbe], 20),     # ld ix, (0xbeef)
            ([0x22, 0xef, 0xbe], 16),           # ld (0xbeef), hl
            ([0xed, 0x43, 0xef, 0xbe], 20),     # ld (0xbeef), bc
            ([0xdd, 0x22, 0xef, 0xbe], 20),     # ld (0xbeef), ix
            ([0xf9], 6),                        # ld sp, hl
            ([0xdd, 0xf9], 10),                 # ld sp, ix
            ([0xe5], 11),                       # push hl
            ([0xdd, 0xe5], 15),                 # push ix
            ([0xe1], 10),                       # pop hl
            ([0xdd, 0xe1], 14),                 # pop ix
            ([0xeb], 4),                        # ex de, hl
            ([0x08], 4),                        # ex af, af'
            ([0xd9], 4),                        # exx
            ([0xe3], 19),                       # ex (sp), hl
            ([0xdd, 0xe3], 23),                 # ex (sp), ix
            ([0xed, 0xa0], 16),                 # ldi
            ([0xed, 0xa8], 16),                 # ldd
            ([0xed, 0xa1], 16),                 # cpi
            ([0xed, 0xa9], 16),                 # cpd
            ([0x80], 4),                        # add a, b
            ([0xc6, 0x05], 7),                  # add a, 5
            ([0x86], 7),                        # add a, (hl)
            ([0xdd, 0x86, 0x01], 19),           # add a, (ix + 1)
            ([0x88], 4),                        # adc a, b
            ([0xce, 0x05], 7),                  # adc a, 5
            ([0x8e], 7),                        # adc a, (hl)
            ([0xdd, 0x8e, 0x01], 19),           # adc a, (ix + 1)
            ([0x97], 4),                        # sub a
            ([0xd6, 0x01], 7),                  # sub 1
            ([0x96], 7),                        # sub (hl)
            ([0xdd, 0x96, 0x05], 19),           # sub (ix + 5)
            ([0x98], 4),                        # sbc a, b
            ([0xde, 0x05], 7),                  # sbc a, 5
            ([0x9e], 7),                        # sbc a, (hl)
            ([0xfd, 0x9e, 0x06], 19),           # sbc a, (iy + 6)
            ([0xa0], 4),                        # and b
            ([0xe6, 0x10], 7),                  # and 0x10
            ([0xa6], 7),                        # and (hl)
            ([0xdd, 0xa6, 0x01], 19),           # and (ix + 1)
            ([0xb0], 4),                        # or b
            ([0xf6, 0x10], 7),                  # or 0x10
            ([0xb6], 7),                        # or (hl)
            ([0xdd, 0xb6, 0x05], 19),           # or (ix + 5)
            ([0xa8], 4),                        # xor b
            ([0xee, 0x10], 7),                  # xor 0x10
            ([0xae], 7),                        # xor (hl)
            ([0xdd, 0xae, 0x10], 19),           # xor (ix + 0x10)
            ([0xb8], 4),                        # cp b
            ([0xfe, 0x10], 7),                  # cp 0x10
            ([0xbe], 7),                        # cp (hl)
            ([0xdd, 0xbe, 0x05], 19),           # cp (ix + 5)
            ([0x0c], 4),                        # inc c
            ([0x34], 11),                       # inc (hl)
            ([0xdd, 0x34, 0x05], 23),           # inc (ix + 5)
            ([0x0d], 4),                        # dec c
            ([0x35], 11),                       # dec (hl)
            ([0xdd, 0x35, 0x05], 23),           # dec (ix + 5)
            ([0x27], 4),                        # daa
            ([0x2f], 4),                        # cpl
            ([0xed, 0x44], 8),                  # neg
            ([0x3f], 4),                        # ccf
            ([0x37], 4),                        # scf
            ([0x00], 4),                        # nop
            ([0x76], 4),                        # halt
            ([0xf3], 4),                        # di
            ([0xfb], 4),                        # ei
            ([0xed, 0x46], 8),                  # im 0
            ([0xed, 0x56], 8),                  # im 1
            ([0xed, 0x5e], 8),                  # im 2
            ([0x09], 11),                       # add hl, bc
            ([0xed, 0x4a], 15),                 # adc hl, bc
            ([0xed, 0x42], 15),                 # sbc hl, bc
            ([0xdd, 0x09], 15),                 # add ix, bc
            ([0x23], 6),                        # inc hl
            ([0xdd, 0x23], 10),                 # inc ix
            ([0x2b], 6),                        # dec hl
            ([0xdd, 0x2b], 10),                 # dec ix
            ([0x07], 4),                        # rlca
            ([0x17], 4),                        # rla
            ([0x0f], 4),                        # rrca
            ([0x1f], 4),                        # rra
            ([0xcb, 0x00], 8),                  # rlc b
            ([0xcb, 0x06], 15),                 # rlc (hl)
            ([0xdd, 0xcb, 0x01, 0x06], 23),     # rlc (ix + 1)
            ([0xcb, 0x10], 8),                  # rl b
            ([0xcb, 0x16], 15),                 # rl (hl)
            ([0xdd, 0xcb, 0x01, 0x16], 23),     # rl (ix + 1)
            ([0xcb, 0x08], 8),                  # rrc b
            ([0xcb, 0x0e], 15),                 # rrc (hl)
            ([0xdd, 0xcb, 0x01, 0x0e], 23),     # rrc (ix + 1)
            ([0xcb, 0x18], 8),                  # rr b
            ([0xcb, 0x1e], 15),                 # rr (hl)
            ([0xdd, 0xcb, 0x01, 0x1e], 23),     # rr (ix + 1)
            ([0xcb, 0x20], 8),                  # sla b
            ([0xcb, 0x26], 15),                 # sla (hl)
            ([0xdd, 0xcb, 0x02, 0x26], 23),     # sla (ix + 2)
            ([0xcb, 0x28], 8),                  # sra b
            ([0xcb, 0x2e], 15),                 # sra (hl)
            ([0xdd, 0xcb, 0x02, 0x2e], 23),     # sra (ix + 2)
            ([0xcb, 0x38], 8),                  # srl b
            ([0xcb, 0x3e], 15),                 # srl (hl)
            ([0xdd, 0xcb, 0x03, 0x3e], 23),     # srl (ix + 3)
            ([0xed, 0x6f], 18),                 # rld
            ([0xed, 0x67], 18),                 # rrd
            ([0xcb, 0x41], 8),                  # bit 0, c
            ([0xcb, 0x46], 12),                 # bit 0, (hl)
            ([0xdd, 0xcb, 0x01, 0x46], 20),     # bit 0, (ix + 1)
            ([0xcb, 0xc1], 8),                  # set 0, c
            ([0xcb, 0xc6], 15),                 # set 0, (hl)
            ([0xdd, 0xcb, 0x01, 0xc6], 23),     # set 0, (ix + 1)
            ([0xcb, 0x81], 8),                  # res 0, c
            ([0xcb, 0x86], 15),                 # res 0, (hl)
            ([0xdd, 0xcb, 0x01, 0x86], 23),     # res 0, (ix + 1)
            ([0xc3, 0xef, 0xbe], 10),           # jp 0xbeef
            ([0xc2, 0xef, 0xbe], 10),           # jp nz, 0xbeef
            ([0xca, 0xef, 0xbe], 10),           # jp z, 0xbeef
            ([0xd2, 0xef, 0xbe], 10),           # jp nc, 0xbeef
            ([0xda, 0xef, 0xbe], 10),           # jp c, 0xbeef
            ([0xe2, 0xef, 0xbe], 10),           # jp po, 0xbeef
            ([0xea, 0xef, 0xbe], 10),           # jp pe, 0xbeef
            ([0xf2, 0xef, 0xbe], 10),           # jp p, 0xbeef
            ([0xfa, 0xef, 0xbe], 10),           # jp m, 0xbeef
            ([0x18, 0x01], 12),                 # jr 1
            ([0xe9], 4),                        # jp (hl)
            ([0xdd, 0xe9], 8),                  # jp (ix)
            ([0xcd, 0xef, 0xbe], 17),           # call 0xbeef
            ([0xc9], 10),                       # ret
            ([0xed, 0x4d], 14),                 # reti
            ([0xed, 0x45], 14),                 # retn
            ([0xc7], 11),                       # rst 0x00
            ([0xdb, 0xff], 11),                 # in a, (0xff)
            ([0xed, 0x40], 12),                 # in b, (c)
            ([0xed, 0xa2], 16),                 # ini
            ([0xed, 0xaa], 16),                 # ind
            ([0xd3, 0xff], 11),                 # out (0xff), a
            ([0xed, 0x41], 12),                 # out (c), b
            ([0xed, 0x71], 12),                 # out (c), 0
        ]

        for op_codes, cycles in values:
            yield self.check_execute_returns_correct_number_of_tstates_for_each_operation_type, op_codes, cycles

    def check_execute_returns_correct_number_of_tstates_for_each_operation_type(self, op_codes, cycles):
        # given
        self.given_next_instruction_is(op_codes)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, cycles)

    def test_execute_returns_correct_number_of_tstates_for_block_operations_when_bc_decrements_to_zero(self):
        # given
        self.given_next_instruction_is(0xed, 0xb0)
        self.given_register_pair_contains_value('hl', 0xbeef)
        self.given_register_pair_contains_value('de', 0xbabe)
        self.given_register_pair_contains_value('bc', 0x0001)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 16)

    def test_execute_returns_correct_number_of_tstates_for_block_operations_when_bc_decrements_to_nonzero(self):
        # given
        self.given_next_instruction_is(0xed, 0xb8)
        self.given_register_pair_contains_value('hl', 0xbeef)
        self.given_register_pair_contains_value('de', 0xbabe)
        self.given_register_pair_contains_value('bc', 0x0010)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 21)

    def test_execute_returns_correct_number_of_tstates_for_jr_c_when_carry_is_set(self):
        # given
        self.given_flag('c').set_to(True)
        self.given_next_instruction_is(0x38, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 12)

    def test_execute_returns_correct_number_of_tstates_for_jr_c_when_carry_is_reset(self):
        # given
        self.given_flag('c').set_to(False)
        self.given_next_instruction_is(0x38, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 7)

    def test_execute_returns_correct_number_of_tstates_for_jr_nc_when_carry_is_set(self):
        # given
        self.given_flag('c').set_to(True)
        self.given_next_instruction_is(0x30, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 7)

    def test_execute_returns_correct_number_of_tstates_for_jr_nc_when_carry_is_reset(self):
        # given
        self.given_flag('c').set_to(False)
        self.given_next_instruction_is(0x30, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 12)

    def test_execute_returns_correct_number_of_tstates_for_jr_z_when_z_is_set(self):
        # given
        self.given_flag('z').set_to(True)
        self.given_next_instruction_is(0x28, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 12)

    def test_execute_returns_correct_number_of_tstates_for_jr_z_when_z_is_reset(self):
        # given
        self.given_flag('z').set_to(False)
        self.given_next_instruction_is(0x28, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 7)

    def test_execute_returns_correct_number_of_tstates_for_jr_nz_when_z_is_set(self):
        # given
        self.given_flag('z').set_to(True)
        self.given_next_instruction_is(0x20, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 7)

    def test_execute_returns_correct_number_of_tstates_for_jr_nz_when_z_is_reset(self):
        # given
        self.given_flag('z').set_to(False)
        self.given_next_instruction_is(0x20, 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 12)

    def test_execute_returns_correct_number_of_tstates_for_djnz_when_b_is_zero_after_decrement(self):
        # given
        self.given_next_instruction_is(0x10, 0xa0)
        self.given_register_contains_value('b', 1)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 8)

    def test_execute_returns_correct_number_of_tstates_for_djnz_when_b_is_nonzero_after_decrement(self):
        # given
        self.given_next_instruction_is(0x10, 0xa0)
        self.given_register_contains_value('b', 0x10)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 13)

    def test_execute_returns_correct_number_of_tstates_for_call_z_when_z_is_set(self):
        # given
        self.given_flag('z').set_to(True)
        self.given_next_instruction_is(0xcc, 0xef, 0xbe)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 5)

    def test_execute_returns_correct_number_of_tstates_for_call_z_when_z_is_reset(self):
        # given
        self.given_flag('z').set_to(False)
        self.given_next_instruction_is(0xcc, 0xef, 0xbe)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 3)

    def test_execute_returns_correct_number_of_tstates_for_ret_z_when_z_is_set(self):
        # given
        self.given_flag('z').set_to(True)
        self.given_next_instruction_is(0xc8)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 11)

    def test_execute_returns_correct_number_of_tstates_for_ret_z_when_z_is_reset(self):
        # given
        self.given_flag('z').set_to(False)
        self.given_next_instruction_is(0xc8)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 5)

    def test_execute_returns_correct_number_of_tstates_for_inir_when_b_decrements_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_next_instruction_is(0xed, 0xb2)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 16)

    def test_execute_returns_correct_number_of_tstates_for_inir_when_b_decrements_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_next_instruction_is(0xed, 0xb2)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 21)

    def test_execute_returns_correct_number_of_tstates_for_indr_when_b_decrements_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_next_instruction_is(0xed, 0xba)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 16)

    def test_execute_returns_correct_number_of_tstates_for_indr_when_b_decrements_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_next_instruction_is(0xed, 0xba)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 21)

    def test_execute_returns_correct_number_of_tstates_for_otir_when_b_decrements_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_next_instruction_is(0xed, 0xb3)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 16)

    def test_execute_returns_correct_number_of_tstates_for_otir_when_b_decrements_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_next_instruction_is(0xed, 0xb3)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 21)

    def test_execute_returns_correct_number_of_tstates_for_otdr_when_b_decrements_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_next_instruction_is(0xed, 0xbb)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 16)

    def test_execute_returns_correct_number_of_tstates_for_otdr_when_b_decrements_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_next_instruction_is(0xed, 0xbb)

        # when
        t_states = self.processor.execute()

        # then
        assert_equals(t_states, 21)
