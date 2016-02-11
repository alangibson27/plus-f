from processor_tests import TestHelper


class TestJumps(TestHelper):
    def test_jp(self):
        # given
        self.given_register_contains_value('a', 0x00)
        self.memory[0xbeef] = 0xcb
        self.memory[0xbef0] = 0xff

        self.given_next_instruction_is(0xc3, 0xef, 0xbe)

        # when
        self.processor.execute()
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b10000000)
        self.assert_pc_address().equals(0xbef1)

    def test_jp_nz(self):
        for z_flag in [True, False]:
            yield self.check_jp_nz, z_flag

    def check_jp_nz(self, z_flag):
        # given
        self.processor.set_condition('z', z_flag)
        self.given_next_instruction_is(0xc2, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0003 if z_flag else 0xbeef)

    def test_jp_z(self):
        for z_flag in [True, False]:
            yield self.check_jp_z, z_flag

    def check_jp_z(self, z_flag):
        # given
        self.processor.set_condition('z', z_flag)
        self.given_next_instruction_is(0xca, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if z_flag else 0x0003)

    def test_jp_nc(self):
        for c_flag in [True, False]:
            yield self.check_jp_nc, c_flag

    def check_jp_nc(self, c_flag):
        # given
        self.processor.set_condition('c', c_flag)
        self.given_next_instruction_is(0xd2, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0003 if c_flag else 0xbeef)

    def test_jp_c(self):
        for c_flag in [True, False]:
            yield self.check_jp_c, c_flag

    def check_jp_c(self, c_flag):
        # given
        self.processor.set_condition('c', c_flag)
        self.given_next_instruction_is(0xda, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if c_flag else 0x0003)

    def test_jp_po(self):
        for p_flag in [True, False]:
            yield self.check_jp_po, p_flag

    def check_jp_po(self, p_flag):
        # given
        self.processor.set_condition('p', p_flag)
        self.given_next_instruction_is(0xe2, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0003 if p_flag else 0xbeef)

    def test_jp_pe(self):
        for p_flag in [True, False]:
            yield self.check_jp_pe, p_flag

    def check_jp_pe(self, p_flag):
        # given
        self.processor.set_condition('p', p_flag)
        self.given_next_instruction_is(0xea, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if p_flag else 0x0003)

    def test_jp_p(self):
        for s_flag in [True, False]:
            yield self.check_jp_p, s_flag

    def check_jp_p(self, s_flag):
        # given
        self.processor.set_condition('s', s_flag)
        self.given_next_instruction_is(0xf2, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0003 if s_flag else 0xbeef)

    def test_jp_m(self):
        for s_flag in [True, False]:
            yield self.check_jp_m, s_flag

    def check_jp_m(self, s_flag):
        # given
        self.processor.set_condition('s', s_flag)
        self.given_next_instruction_is(0xfa, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if s_flag else 0x0003)

    def test_jr(self):
        values = [(0x0480, 0x03, 0x0485), (0x0480, 0xfb, 0x47d)]
        for (start, jump_size, destination) in values:
            yield self.check_jr, start, jump_size, destination

    def check_jr(self, start, jump_size, destination):
        # given
        self.given_program_counter_is(start)
        self.given_next_instruction_is(0x18, jump_size)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(destination)

    def test_jr_c(self):
        for c_flag in [True, False]:
            yield self.check_jr_c, c_flag

    def check_jr_c(self, c_flag):
        # given
        self.given_program_counter_is(0xbeea)
        self.processor.set_condition('c', c_flag)
        self.given_next_instruction_is(0x38, 0x03)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if c_flag else 0xbeec)

    def test_jr_nc(self):
        for c_flag in [True, False]:
            yield self.check_jr_nc, c_flag

    def check_jr_nc(self, c_flag):
        # given
        self.given_program_counter_is(0xbeea)
        self.processor.set_condition('c', c_flag)
        self.given_next_instruction_is(0x30, 0x03)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeec if c_flag else 0xbeef)

    def test_jr_z(self):
        for z_flag in [True, False]:
            yield self.check_jr_z, z_flag

    def check_jr_z(self, z_flag):
        # given
        self.given_program_counter_is(0xbeea)
        self.processor.set_condition('z', z_flag)
        self.given_next_instruction_is(0x28, 0x03)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if z_flag else 0xbeec)

    def test_jr_nz(self):
        for z_flag in [True, False]:
            yield self.check_jr_nz, z_flag

    def check_jr_nz(self, z_flag):
        # given
        self.given_program_counter_is(0xbeea)
        self.processor.set_condition('z', z_flag)
        self.given_next_instruction_is(0x20, 0x03)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeec if z_flag else 0xbeef)

    def test_jp_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0xbabe)
        self.given_next_instruction_is(0xe9)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbabe)

    def test_jp_indexed_indirect(self):
        values = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in values:
            yield self.check_jp_indexed_indirect, op_code, reg

    def check_jp_indexed_indirect(self, op_code, reg):
        # given
        self.given_register_pair_contains_value(reg, 0xdead)
        self.given_next_instruction_is(op_code, 0xe9)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xdead)

    def test_djnz_with_b_decrementing_to_nonzero(self):
        # given
        self.given_program_counter_is(0x1000)
        self.given_register_contains_value('b', 0xff)

        self.given_next_instruction_is(0x10, 0x10)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x1012)

    def test_djnz_with_b_decrementing_to_zero(self):
        # given
        self.given_program_counter_is(0x1000)
        self.given_register_contains_value('b', 0x01)

        self.given_next_instruction_is(0x10, 0x10)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x1002)

    def test_djnz_with_b_overflowing(self):
        # given
        self.given_program_counter_is(0x1000)
        self.given_register_contains_value('b', 0x00)

        self.given_next_instruction_is(0x10, 0x10)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x1012)
        self.assert_register('b').equals(0xff)