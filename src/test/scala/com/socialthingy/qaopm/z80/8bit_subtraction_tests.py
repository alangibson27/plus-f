import random
from tests.processor.processor_tests import TestHelper
from z80.funcs import to_signed

class Test8BitSubtraction(TestHelper):
    def test_sub_with_other_reg_giving_positive_result_with_no_carries_or_overflow(self):
        values = [('b', 0x90), ('c', 0x91), ('d', 0x92), ('e', 0x93), ('h', 0x94), ('l', 0x95)]
        for register, op_code in values:
            yield self.check_sub_with_other_reg_giving_positive_result_with_no_carries_or_overflow, register, op_code

    def check_sub_with_other_reg_giving_positive_result_with_no_carries_or_overflow(self, other_reg, op_code):
        # given
        self.given_register_contains_value('a', 0b00000100)
        self.given_register_contains_value(other_reg, 0b00000001)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000011)
        self.assert_register(other_reg).equals(0b00000001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sub_to_itself_giving_zero_result(self):
        # given
        self.given_register_contains_value('a', 0b00000001)

        self.given_next_instruction_is(0x97)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sub_with_other_reg_giving_positive_result_with_half_borrow(self):
        # given
        self.given_register_contains_value('a', 0b00010000)
        self.given_register_contains_value('b', 0b00001000)

        self.given_next_instruction_is(0x90)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00001000)
        self.assert_register('b').equals(0b00001000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sub_to_other_reg_giving_negative_result_full_borrow_and_overflow(self):
        # given
        self.given_register_contains_value('a', 0b01101001)
        self.given_register_contains_value('c', 0b01111001)

        self.given_next_instruction_is(0x91)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11110000)
        self.assert_register('c').equals(0b01111001)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_set()

    def test_sub_immediate(self):
        # given
        self.given_register_contains_value('a', 0b00001000)

        self.given_next_instruction_is(0xd6, 0b00000001)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000111)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sub_hl_indirect(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.given_register_pair_contains_value('hl', 0xbeef)

        self.memory[0xbeef] = 0b00000001

        self.given_next_instruction_is(0x96)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000111)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sub_indexed_indirect(self):
        values = [('ix', [0xdd, 0x96]), ('iy', [0xfd, 0x96])]
        for register, op_codes in values:
            yield self.check_sub_indexed_indirect, register, op_codes

    def check_sub_indexed_indirect(self, register, op_codes):
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
        self.assert_register('a').equals(0b00000111)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sbc_to_other_reg(self):
        values = [('b', 0x98), ('c', 0x99), ('d', 0x9a), ('e', 0x9b), ('h', 0x9c), ('l', 0x9d)]
        for reg, op_code in values:
            for carry_set in [True, False]:
                yield self.check_sbc_to_other_reg, reg, op_code, carry_set

    def check_sbc_to_other_reg(self, other_reg, op_code, carry_set):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.given_register_contains_value(other_reg, 0b00000001)
        self.processor.set_condition('c', carry_set)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000111 - (1 if carry_set else 0))
        self.assert_register(other_reg).equals(0b00000001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sbc_to_other_reg_with_overflow(self):
        # given
        self.given_register_contains_value('a', 0x01)
        self.given_register_contains_value('b', 0x01)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0x98)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0xff)
        self.assert_register('b').equals(0x01)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_set()

    def test_sbc_to_itself_with_carry(self):
        # given
        self.given_register_contains_value('a', 0x01)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0x9f)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0xff)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_set()

    def test_sbc_immediate_with_carry(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0xde, 0b00000001)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000110)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sbc_immediate_without_carry(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', False)

        self.given_next_instruction_is(0xde, 0b00000001)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000111)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sbc_hl_indirect_with_carry(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', True)

        self.given_register_pair_contains_value('hl', 0xa000)
        self.memory[0xa000] = 0b00000001

        self.given_next_instruction_is(0x9e)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000110)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()

    def test_sbc_indexed_indirect_with_carry(self):
        values = [('ix', [0xdd, 0x9e]), ('iy', [0xfd, 0x9e])]
        for register, op_codes in values:
            yield self.check_sbc_indexed_indirect, register, op_codes

    def check_sbc_indexed_indirect(self, register, op_codes):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', True)
        self.processor.index_registers[register] = 0xbeef

        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)

        self.memory[0xbeef + signed_offset] = 0b00000001

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000110)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
        self.assert_flag('c').is_reset()
