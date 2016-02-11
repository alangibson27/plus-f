from nose.tools import *
from processor_tests import TestHelper


class Test16BitArithmetic(TestHelper):
    def test_add_hl_and_other_reg_with_no_carry_or_half_carry(self):
        values = [(0x09, 'bc'), (0x19, 'de'), (0x39, 'sp')]
        for op_code, reg_pair in values:
            yield self.check_add_hl_with_no_carry_or_half_carry, op_code, reg_pair

    def check_add_hl_with_no_carry_or_half_carry(self, op_code, reg_pair):
        # given
        self.given_register_pair_contains_value('hl', 0x1111)
        self.given_register_pair_contains_value(reg_pair, 0x1111)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x2222)

        self.assert_flag('h').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_hl_to_itself_with_no_carry_or_half_carry(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1111)

        self.given_next_instruction_is(0x29)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x2222)

        self.assert_flag('h').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()


    def test_add_hl_and_bc_with_full_and_half_carry(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1100)
        self.given_register_pair_contains_value('bc', 0xff00)

        self.given_next_instruction_is(0x09)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1000)

        self.assert_flag('h').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_adc_hl_and_other_reg_with_no_carry_or_half_carry(self):
        values = [(0x4a, 'bc'), (0x5a, 'de'), (0x7a, 'sp')]
        for op_code, reg in values:
            for carry in [True, False]:
                yield self.check_adc_hl_and_other_reg_with_no_carry_or_half_carry, op_code, reg, carry

    def check_adc_hl_and_other_reg_with_no_carry_or_half_carry(self, op_code, reg, carry):
        # given
        self.given_register_pair_contains_value('hl', 0x1100)
        self.given_register_pair_contains_value(reg, 0x0011)
        self.processor.set_condition('c', carry)

        self.given_next_instruction_is(0xed, op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1111 + (1 if carry else 0))

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_hl_with_bc_with_negative_result(self):
        # given
        self.given_register_pair_contains_value('hl', 0x7fff)
        self.given_register_pair_contains_value('bc', 0x0001)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0xed, 0x4a)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x8001)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_hl_with_bc_with_zero_result(self):
        # given
        self.given_register_pair_contains_value('hl', 0xfffe)
        self.given_register_pair_contains_value('bc', 0x0001)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0xed, 0x4a)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_sbc_hl_with_other_reg_with_positive_result(self):
        values = [(0x42, 'bc'), (0x52, 'de'), (0x72, 'sp')]
        for op_code, reg in values:
            for carry in [False, True]:
                yield self.check_sbc_hl_with_other_reg_with_positive_result, op_code, reg, carry

    def check_sbc_hl_with_other_reg_with_positive_result(self, op_code, reg, carry):
        # given
        self.given_register_pair_contains_value('hl', 0x1234)
        self.given_register_pair_contains_value(reg, 0x0134)
        self.processor.set_condition('c', carry)

        self.given_next_instruction_is(0xed, op_code)

        # when
        self.processor.execute()

        # then
        expected = 0x10ff if carry else 0x1100
        self.assert_register_pair('hl').equals(expected)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sbc_hl_with_itself_and_carry(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0xed, 0x62)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0xffff)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_set()

    def test_sbc_hl_with_itself_and_no_carry(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.processor.set_condition('c', False)

        self.given_next_instruction_is(0xed, 0x62)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_add_index_reg_and_other_reg_with_no_carry_or_half_carry(self):
        index_registers = [(0xdd, 'ix'), (0xfd, 'iy')]
        for index_op_code, index_reg in index_registers:
            values = [(0x09, 'bc'), (0x19, 'de'), (0x39, 'sp')]
            for op_code, reg in values:
                yield self.check_add_index_reg_and_other_reg_with_no_carry_or_half_carry, index_op_code, index_reg, op_code, reg

    def check_add_index_reg_and_other_reg_with_no_carry_or_half_carry(self, index_op_code, index_reg, op_code, reg):
        # given
        self.processor.index_registers[index_reg] = 0x1000
        self.given_register_pair_contains_value(reg, 0x0111)

        self.given_next_instruction_is(index_op_code, op_code)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.index_registers[index_reg], 0x1111)

        self.assert_flag('h').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_index_reg_and_other_reg_with_half_carry(self):
        index_registers = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in index_registers:
            yield self.check_add_index_reg_and_other_reg_with_half_carry, op_code, reg

    def check_add_index_reg_and_other_reg_with_half_carry(self, op_code, reg):
        # given
        self.processor.index_registers[reg] = 0x1f00
        self.given_register_pair_contains_value('bc', 0x0100)

        self.given_next_instruction_is(op_code, 0x09)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.index_registers[reg], 0x2000)

        self.assert_flag('h').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_index_reg_and_other_reg_with_full_and_half_carry(self):
        index_registers = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in index_registers:
            yield self.check_add_index_reg_and_other_reg_with_full_and_half_carry, op_code, reg

    def check_add_index_reg_and_other_reg_with_full_and_half_carry(self, op_code, reg):
        # given
        self.processor.index_registers[reg] = 0xff00
        self.given_register_pair_contains_value('bc', 0x0100)

        self.given_next_instruction_is(op_code, 0x09)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.index_registers[reg], 0x0000)

        self.assert_flag('h').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_inc_16bit(self):
        registers = [(0x03, 'bc'), (0x13, 'de'), (0x23, 'hl'), (0x33, 'sp')]
        for op_code, reg in registers:
            values = [(0x0010, 0x0011), (0xffff, 0x0000)]
            for initial_value, final_value in values:
                yield self.check_inc_16bit, op_code, reg, initial_value, final_value

    def check_inc_16bit(self, op_code, reg, initial_value, final_value):
        # given
        self.given_register_pair_contains_value(reg, initial_value)
        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair(reg).equals(final_value)

    def test_inc_index_reg(self):
        registers = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in registers:
            values = [(0x0010, 0x0011), (0xffff, 0x0000)]
            for initial_value, final_value in values:
                yield self.check_inc_index_reg, op_code, reg, initial_value, final_value

    def check_inc_index_reg(self, op_code, reg, initial_value, final_value):
        # given
        self.processor.index_registers[reg] = initial_value
        self.given_next_instruction_is(op_code, 0x23)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.index_registers[reg], final_value)

    def test_dec_16bit(self):
        registers = [(0x0b, 'bc'), (0x1b, 'de'), (0x2b, 'hl'), (0x3b, 'sp')]
        for op_code, reg in registers:
            values = [(0x0010, 0x000f), (0x0000, 0xffff)]
            for initial_value, final_value in values:
                yield self.check_dec_16bit, op_code, reg, initial_value, final_value

    def check_dec_16bit(self, op_code, reg, initial_value, final_value):
        # given
        self.given_register_pair_contains_value(reg, initial_value)
        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair(reg).equals(final_value)

    def test_dec_index_reg(self):
        registers = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in registers:
            values = [(0x0010, 0x000f), (0x0000, 0xffff)]
            for initial_value, final_value in values:
                yield self.check_dec_index_reg, op_code, reg, initial_value, final_value

    def check_dec_index_reg(self, op_code, reg, initial_value, final_value):
        # given
        self.processor.index_registers[reg] = initial_value
        self.given_next_instruction_is(op_code, 0x2b)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.index_registers[reg], final_value)
