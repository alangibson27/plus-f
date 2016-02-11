import random
from tests.processor.processor_tests import TestHelper
from z80.funcs import to_signed


class Test8BitComparison(TestHelper):
    def test_cp_with_other_reg_giving_positive_result_with_no_carries_or_overflow(self):
        values = [('b', 0xb8), ('c', 0xb9), ('d', 0xba), ('e', 0xbb), ('h', 0xbc), ('l', 0xbd)]
        for register, op_code in values:
            yield self.check_cp_with_other_reg_giving_positive_result_with_no_carries_or_overflow, register, op_code

    def check_cp_with_other_reg_giving_positive_result_with_no_carries_or_overflow(self, other_reg, op_code):
        # given
        self.given_register_contains_value('a', 0b00000100)
        self.given_register_contains_value(other_reg, 0b00000001)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000100)
        self.assert_register(other_reg).equals(0b00000001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_cp_to_itself_giving_zero_result(self):
        # given
        self.given_register_contains_value('a', 0b00000001)

        self.given_next_instruction_is(0xbf)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_cp_with_other_reg_giving_positive_result_with_half_borrow(self):
        # given
        self.given_register_contains_value('a', 0b00010000)
        self.given_register_contains_value('b', 0b00001000)

        self.given_next_instruction_is(0xb8)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00010000)
        self.assert_register('b').equals(0b00001000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_cp_to_other_reg_giving_negative_result_full_borrow_and_overflow(self):
        # given
        self.given_register_contains_value('a', 0b01101001)
        self.given_register_contains_value('c', 0b01111001)

        self.given_next_instruction_is(0xb9)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01101001)
        self.assert_register('c').equals(0b01111001)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_set()

    def test_cp_immediate(self):
        # given
        self.given_register_contains_value('a', 0b00001000)

        self.given_next_instruction_is(0xfe, 0b00000001)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00001000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_cp_hl_indirect(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.given_register_pair_contains_value('hl', 0xbeef)

        self.memory[0xbeef] = 0b00000001

        self.given_next_instruction_is(0xbe)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00001000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_cp_indexed_indirect(self):
        values = [('ix', [0xdd, 0xbe]), ('iy', [0xfd, 0xbe])]
        for register, op_codes in values:
            yield self.check_cp_indexed_indirect, register, op_codes

    def check_cp_indexed_indirect(self, register, op_codes):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.index_registers[register] = 0xbeef

        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)

        self.memory[0xbeef + signed_offset] = 0b00000001

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00001000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()
