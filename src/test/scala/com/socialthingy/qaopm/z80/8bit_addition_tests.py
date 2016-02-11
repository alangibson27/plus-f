import random
from tests.processor.processor_tests import TestHelper
from z80.funcs import to_signed

class Test8BitAddition(TestHelper):
    def test_add_a_with_other_reg_giving_positive_result_with_no_carries_or_overflow(self):
        values = [('b', 0x80), ('c', 0x81), ('d', 0x82), ('e', 0x83), ('h', 0x84), ('l', 0x85)]
        for register, op_code in values:
            yield self.check_add_a_with_other_reg_giving_positive_result_with_no_carries_or_overflow, register, op_code

    def check_add_a_with_other_reg_giving_positive_result_with_no_carries_or_overflow(self, other_reg, op_code):
        # given
        self.given_register_contains_value('a', 0b00000100)
        self.given_register_contains_value(other_reg, 0b00000001)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000101)
        self.assert_register(other_reg).equals(0b00000001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_a_to_itself_giving_positive_result_with_no_carries_or_overflow(self):
        # given
        self.given_register_contains_value('a', 0b00000001)

        self.given_next_instruction_is(0x87)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000010)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_a_with_other_reg_giving_positive_result_with_half_carry(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.given_register_contains_value('b', 0b00001000)

        self.given_next_instruction_is(0x80)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00010000)
        self.assert_register('b').equals(0b00001000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_a_to_itself_giving_zero_result(self):
        # given
        self.given_register_contains_value('a', 0)

        self.given_next_instruction_is(0x87)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_a_to_other_reg_giving_negative_result_and_overflow(self):
        # given
        self.given_register_contains_value('a', 0b01111000)
        self.given_register_contains_value('c', 0b01101001)

        self.given_next_instruction_is(0x81)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b11100001)
        self.assert_register('c').equals(0b01101001)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_a_to_other_reg_giving_full_carry(self):
        # given
        self.given_register_contains_value('a', 0b11111000)
        self.given_register_contains_value('d', 0b01101001)

        self.given_next_instruction_is(0x82)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01100001)
        self.assert_register('d').equals(0b01101001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_add_a_immediate(self):
        # given
        self.given_register_contains_value('a', 0b00001000)

        self.given_next_instruction_is(0xc6, 0b01000001)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01001001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_a_hl_indirect(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.given_register_pair_contains_value('hl', 0xbeef)

        self.memory[0xbeef] = 0b01000001

        self.given_next_instruction_is(0x86)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01001001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_add_a_indexed_indirect(self):
        values = [('ix', [0xdd, 0x86]), ('iy', [0xfd, 0x86])]
        for register, op_codes in values:
            yield self.check_add_a_indexed_indirect, register, op_codes

    def check_add_a_indexed_indirect(self, register, op_codes):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.index_registers[register] = 0xbeef

        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)

        self.memory[0xbeef + signed_offset] = 0b01000001

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01001001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_a_to_other_reg(self):
        values = [('b', 0x88), ('c', 0x89), ('d', 0x8a), ('e', 0x8b), ('h', 0x8c), ('l', 0x8d)]
        for reg, op_code in values:
            for carry_set in [True, False]:
                yield self.check_adc_a_to_other_reg, reg, op_code, carry_set

    def check_adc_a_to_other_reg(self, other_reg, op_code, carry_set):
        # given
        self.given_register_contains_value('a', 0b00000100)
        self.given_register_contains_value(other_reg, 0b00000001)
        self.processor.set_condition('c', carry_set)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b00000101 + (1 if carry_set else 0))
        self.assert_register(other_reg).equals(0b00000001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_a_to_other_reg_with_overflow(self):
        # given
        self.given_register_contains_value('a', 0xfe)
        self.given_register_contains_value('b', 0x01)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0x88)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0x00)
        self.assert_register('b').equals(0x01)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_adc_a_to_itself_with_carry(self):
        # given
        self.given_register_contains_value('a', 0x00)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0x8f)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0x01)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_a_immediate_with_carry(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', True)

        self.given_next_instruction_is(0xce, 0b01000001)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01001010)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_a_immediate_without_carry(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', False)

        self.given_next_instruction_is(0xce, 0b01000001)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01001001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_a_hl_indirect_with_carry(self):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', True)

        self.given_register_pair_contains_value('hl', 0xa000)
        self.memory[0xa000] = 0b01000001

        self.given_next_instruction_is(0x8e)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01001010)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_adc_a_indexed_indirect_with_carry(self):
        values = [('ix', [0xdd, 0x8e]), ('iy', [0xfd, 0x8e])]
        for register, op_codes in values:
            yield self.check_adc_a_indexed_indirect, register, op_codes

    def check_adc_a_indexed_indirect(self, register, op_codes):
        # given
        self.given_register_contains_value('a', 0b00001000)
        self.processor.set_condition('c', True)
        self.processor.index_registers[register] = 0xbeef

        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)

        self.memory[0xbeef + signed_offset] = 0b01000001

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0b01001010)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()
