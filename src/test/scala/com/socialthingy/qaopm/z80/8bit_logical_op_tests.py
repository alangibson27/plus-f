import random

from z80.funcs import to_signed
from tests.processor.processor_tests import TestHelper


class Test8BitLogicalOps(TestHelper):
    def test_and_a_with_other_reg_giving_negative_result(self):
        values = [('b', 0xa0), ('c', 0xa1), ('d', 0xa2), ('e', 0xa3), ('h', 0xa4), ('l', 0xa5)]
        for other_reg, op_code in values:
            yield self.check_and_a_with_other_reg_giving_negative_result, other_reg, op_code

    def check_and_a_with_other_reg_giving_negative_result(self, other_reg, op_code):
        # given
        self.given_register_contains_value('a', 0b10000100)
        self.given_register_contains_value(other_reg, 0b10000101)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b10000100)
        self.assert_register(other_reg).equals(0b10000101)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_and_a_with_other_reg_giving_zero_result(self):
        # given
        self.given_register_contains_value('a', 0b10101010)
        self.given_register_contains_value('b', 0b01010101)

        self.given_next_instruction_is(0xa0)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000000)
        self.assert_register('b').equals(0b01010101)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_and_a_with_other_reg_giving_even_parity_result(self):
        # given
        self.given_register_contains_value('a', 0b00111000)
        self.given_register_contains_value('b', 0b00101000)

        self.given_next_instruction_is(0xa0)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00101000)
        self.assert_register('b').equals(0b00101000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_and_a_with_other_reg_giving_odd_parity_result(self):
        # given
        self.given_register_contains_value('a', 0b11111111)
        self.given_register_contains_value('b', 0b00111000)

        self.given_next_instruction_is(0xa0)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00111000)
        self.assert_register('b').equals(0b00111000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_and_a_with_itself(self):
        # given
        self.given_register_contains_value('a', 0b11111111)

        self.given_next_instruction_is(0xa7)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111111)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_and_a_immediate(self):
        # given
        self.given_register_contains_value('a', 0b11111111)

        self.given_next_instruction_is(0xe6, 0b00111000)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00111000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_and_hl_indirect(self):
        # given
        self.given_register_contains_value('a', 0b11111111)
        self.given_register_pair_contains_value('hl', 0x4000)

        self.memory[0x4000] = 0b00111000

        self.given_next_instruction_is(0xa6)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00111000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_and_a_indexed_indirect(self):
        values = [('ix', [0xdd, 0xa6]), ('iy', [0xfd, 0xa6])]
        for register, op_codes in values:
            yield self.check_and_a_indexed_indirect, register, op_codes

    def check_and_a_indexed_indirect(self, register, op_codes):
        # given
        self.given_register_contains_value('a', 0b11111111)
        self.processor.index_registers[register] = 0xbeef

        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)

        self.memory[0xbeef + signed_offset] = 0b00111000

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00111000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    # Logical OR

    def test_or_a_with_other_reg_giving_negative_result(self):
        values = [('b', 0xb0), ('c', 0xb1), ('d', 0xb2), ('e', 0xb3), ('h', 0xb4), ('l', 0xb5)]
        for other_reg, op_code in values:
            yield self.check_or_a_with_other_reg_giving_negative_result, other_reg, op_code

    def check_or_a_with_other_reg_giving_negative_result(self, other_reg, op_code):
        # given
        self.given_register_contains_value('a', 0b10000100)
        self.given_register_contains_value(other_reg, 0b10000101)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b10000101)
        self.assert_register(other_reg).equals(0b10000101)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_or_a_with_other_reg_giving_zero_result(self):
        # given
        self.given_register_contains_value('a', 0x00)
        self.given_register_contains_value('b', 0x00)

        self.given_next_instruction_is(0xb0)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0x00)
        self.assert_register('b').equals(0x00)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_or_a_with_other_reg_giving_even_parity_result(self):
        # given
        self.given_register_contains_value('a', 0b00111000)
        self.given_register_contains_value('b', 0b00101100)

        self.given_next_instruction_is(0xb0)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00111100)
        self.assert_register('b').equals(0b00101100)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_or_a_with_other_reg_giving_odd_parity_result(self):
        # given
        self.given_register_contains_value('a', 0b11111110)
        self.given_register_contains_value('b', 0b00111000)

        self.given_next_instruction_is(0xb0)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111110)
        self.assert_register('b').equals(0b00111000)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_or_a_with_itself(self):
        # given
        self.given_register_contains_value('a', 0b10101010)

        self.given_next_instruction_is(0xb7)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b10101010)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_or_a_immediate(self):
        # given
        self.given_register_contains_value('a', 0b11000111)

        self.given_next_instruction_is(0xf6, 0b00111000)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111111)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_or_hl_indirect(self):
        # given
        self.given_register_contains_value('a', 0b11000111)
        self.given_register_pair_contains_value('hl', 0x4000)

        self.memory[0x4000] = 0b00111000

        self.given_next_instruction_is(0xb6)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111111)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_or_a_indexed_indirect(self):
        values = [('ix', [0xdd, 0xb6]), ('iy', [0xfd, 0xb6])]
        for register, op_codes in values:
            yield self.check_or_a_indexed_indirect, register, op_codes

    def check_or_a_indexed_indirect(self, register, op_codes):
        # given
        self.given_register_contains_value('a', 0b11000111)
        self.processor.index_registers[register] = 0xbeef

        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)

        self.memory[0xbeef + signed_offset] = 0b00111000

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111111)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    # Logical XOR

    def test_xor_a_with_other_reg_giving_negative_result(self):
        values = [('b', 0xa8), ('c', 0xa9), ('d', 0xaa), ('e', 0xab), ('h', 0xac), ('l', 0xad)]
        for other_reg, op_code in values:
            yield self.check_xor_a_with_other_reg_giving_negative_result, other_reg, op_code

    def check_xor_a_with_other_reg_giving_negative_result(self, other_reg, op_code):
        # given
        self.given_register_contains_value('a', 0b10000100)
        self.given_register_contains_value(other_reg, 0b00000101)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b10000001)
        self.assert_register(other_reg).equals(0b00000101)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_xor_a_with_other_reg_giving_zero_result(self):
        # given
        self.given_register_contains_value('a', 0b10101010)
        self.given_register_contains_value('b', 0b10101010)

        self.given_next_instruction_is(0xa8)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000000)
        self.assert_register('b').equals(0b10101010)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_xor_a_with_other_reg_giving_even_parity_result(self):
        # given
        self.given_register_contains_value('a', 0b00111000)
        self.given_register_contains_value('b', 0b00101100)

        self.given_next_instruction_is(0xa8)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00010100)
        self.assert_register('b').equals(0b00101100)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_xor_a_with_other_reg_giving_odd_parity_result(self):
        # given
        self.given_register_contains_value('a', 0b11111111)
        self.given_register_contains_value('b', 0b00111000)

        self.given_next_instruction_is(0xa8)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11000111)
        self.assert_register('b').equals(0b00111000)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_xor_a_with_itself(self):
        # given
        self.given_register_contains_value('a', 0b10101010)

        self.given_next_instruction_is(0xaf)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_xor_a_immediate(self):
        # given
        self.given_register_contains_value('a', 0b11000111)

        self.given_next_instruction_is(0xee, 0b00111000)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111111)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_xor_hl_indirect(self):
        # given
        self.given_register_contains_value('a', 0b11000111)
        self.given_register_pair_contains_value('hl', 0x4000)

        self.memory[0x4000] = 0b00111000

        self.given_next_instruction_is(0xae)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111111)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_xor_a_indexed_indirect(self):
        values = [('ix', [0xdd, 0xae]), ('iy', [0xfd, 0xae])]
        for register, op_codes in values:
            yield self.check_xor_a_indexed_indirect, register, op_codes

    def check_xor_a_indexed_indirect(self, register, op_codes):
        # given
        self.given_register_contains_value('a', 0b11000111)
        self.processor.index_registers[register] = 0xbeef

        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)

        self.memory[0xbeef + signed_offset] = 0b00111000

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11111111)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()
