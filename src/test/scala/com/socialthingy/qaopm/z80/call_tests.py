from nose.tools import assert_true
from processor_tests import TestHelper


class TestCallReturn(TestHelper):
    def test_call(self):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)

        self.given_next_instruction_is(0xcd, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef)
        self.assert_stack_pointer().equals(0xfffd)
        self.assert_memory(0xfffd).contains(0x37)
        self.assert_memory(0xfffe).contains(0x12)

    def test_call_nz(self):
        for z_flag in [True, False]:
            yield self.check_call_nz, z_flag

    def check_call_nz(self, z_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('z', z_flag)

        self.given_next_instruction_is(0xc4, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x1237 if z_flag else 0xbeef)
        self.assert_stack_pointer().equals(0xffff if z_flag else 0xfffd)

    def test_call_z(self):
        for z_flag in [True, False]:
            yield self.check_call_z, z_flag

    def check_call_z(self, z_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('z', z_flag)

        self.given_next_instruction_is(0xcc, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if z_flag else 0x1237)
        self.assert_stack_pointer().equals(0xfffd if z_flag else 0xffff)

    def test_call_nc(self):
        for c_flag in [True, False]:
            yield self.check_call_nc, c_flag

    def check_call_nc(self, c_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('c', c_flag)

        self.given_next_instruction_is(0xd4, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x1237 if c_flag else 0xbeef)
        self.assert_stack_pointer().equals(0xffff if c_flag else 0xfffd)

    def test_call_c(self):
        for c_flag in [True, False]:
            yield self.check_call_c, c_flag

    def check_call_c(self, c_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('c', c_flag)

        self.given_next_instruction_is(0xdc, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if c_flag else 0x1237)
        self.assert_stack_pointer().equals(0xfffd if c_flag else 0xffff)

    def test_call_po(self):
        for p_flag in [True, False]:
            yield self.check_call_po, p_flag

    def check_call_po(self, p_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('p', p_flag)

        self.given_next_instruction_is(0xe4, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x1237 if p_flag else 0xbeef)
        self.assert_stack_pointer().equals(0xffff if p_flag else 0xfffd)

    def test_call_pe(self):
        for p_flag in [True, False]:
            yield self.check_call_pe, p_flag

    def check_call_pe(self, p_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('p', p_flag)

        self.given_next_instruction_is(0xec, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if p_flag else 0x1237)
        self.assert_stack_pointer().equals(0xfffd if p_flag else 0xffff)

    def test_call_p(self):
        for s_flag in [True, False]:
            yield self.check_call_p, s_flag

    def check_call_p(self, s_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('s', s_flag)

        self.given_next_instruction_is(0xf4, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x1237 if s_flag else 0xbeef)
        self.assert_stack_pointer().equals(0xffff if s_flag else 0xfffd)

    def test_call_m(self):
        for s_flag in [True, False]:
            yield self.check_call_m, s_flag

    def check_call_m(self, s_flag):
        # given
        self.given_stack_pointer_is(0xffff)
        self.given_program_counter_is(0x1234)
        self.processor.set_condition('s', s_flag)

        self.given_next_instruction_is(0xfc, 0xef, 0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef if s_flag else 0x1237)
        self.assert_stack_pointer().equals(0xfffd if s_flag else 0xffff)

    def test_ret(self):
        # given
        self.given_program_counter_is(0x1234)
        self.given_stack_pointer_is(0xfffd)

        self.memory[0xfffd] = 0xef
        self.memory[0xfffe] = 0xbe

        self.given_next_instruction_is(0xc9)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbeef)
        self.assert_stack_pointer().equals(0xffff)

    def test_ret_nz(self):
        for z_flag in [True, False]:
            yield self.check_ret_nz, z_flag

    def check_ret_nz(self, z_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('z', z_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xc0)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x5679 if z_flag else 0xbabe)
        self.assert_stack_pointer().equals(0xfffd if z_flag else 0xffff)

    def test_ret_z(self):
        for z_flag in [True, False]:
            yield self.check_ret_z, z_flag

    def check_ret_z(self, z_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('z', z_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xc8)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbabe if z_flag else 0x5679)
        self.assert_stack_pointer().equals(0xffff if z_flag else 0xfffd)

    def test_ret_nc(self):
        for c_flag in [True, False]:
            yield self.check_ret_nc, c_flag

    def check_ret_nc(self, c_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('c', c_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xd0)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x5679 if c_flag else 0xbabe)
        self.assert_stack_pointer().equals(0xfffd if c_flag else 0xffff)

    def test_ret_c(self):
        for c_flag in [True, False]:
            yield self.check_ret_c, c_flag

    def check_ret_c(self, c_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('c', c_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xd8)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbabe if c_flag else 0x5679)
        self.assert_stack_pointer().equals(0xffff if c_flag else 0xfffd)

    def test_ret_po(self):
        for p_flag in [True, False]:
            yield self.check_ret_po, p_flag

    def check_ret_po(self, p_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('p', p_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xe0)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x5679 if p_flag else 0xbabe)
        self.assert_stack_pointer().equals(0xfffd if p_flag else 0xffff)

    def test_ret_pe(self):
        for p_flag in [True, False]:
            yield self.check_ret_pe, p_flag

    def check_ret_pe(self, p_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('p', p_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xe8)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbabe if p_flag else 0x5679)
        self.assert_stack_pointer().equals(0xffff if p_flag else 0xfffd)

    def test_ret_p(self):
        for s_flag in [True, False]:
            yield self.check_ret_p, s_flag

    def check_ret_p(self, s_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('s', s_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xf0)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x5679 if s_flag else 0xbabe)
        self.assert_stack_pointer().equals(0xfffd if s_flag else 0xffff)

    def test_ret_m(self):
        for s_flag in [True, False]:
            yield self.check_ret_m, s_flag

    def check_ret_m(self, s_flag):
        # given
        self.given_program_counter_is(0x5678)
        self.given_stack_pointer_is(0xfffd)
        self.processor.set_condition('s', s_flag)

        self.memory[0xfffd] = 0xbe
        self.memory[0xfffe] = 0xba

        self.given_next_instruction_is(0xf8)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0xbabe if s_flag else 0x5679)
        self.assert_stack_pointer().equals(0xffff if s_flag else 0xfffd)

    def test_rst(self):
        values = [(0xc7, 0x00), (0xcf, 0x08),
                  (0xd7, 0x10), (0xdf, 0x18),
                  (0xe7, 0x20), (0xef, 0x28),
                  (0xf7, 0x30), (0xff, 0x38)]

        for op_code, jump_address in values:
            yield self.check_rst, op_code, jump_address

    def check_rst(self, op_code, jump_address):
        # given
        self.given_program_counter_is(0x9988)
        self.given_stack_pointer_is(0xffff)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xfffd).contains(0x89)
        self.assert_memory(0xfffe).contains(0x99)
        self.assert_stack_pointer().equals(0xfffd)
        self.assert_pc_address().equals(jump_address)