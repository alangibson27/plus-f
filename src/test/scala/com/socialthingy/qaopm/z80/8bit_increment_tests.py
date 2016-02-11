import random
from nose.tools import assert_equals
from z80.funcs import to_signed
from tests.processor.processor_tests import TestHelper


class Test8BitIncrementOps(TestHelper):
    def test_inc_reg_with_negative_result(self):
        values = [('a', 0x3c), ('b', 0x04), ('c', 0x0c), ('d', 0x14), ('e', 0x1c), ('h', 0x24), ('l', 0x2c)]
        for reg, op_code in values:
            yield self.check_inc_reg_with_negative_result, reg, op_code

    def check_inc_reg_with_negative_result(self, reg, op_code):
        # given
        self.given_register_contains_value(reg, 0b10000001)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        assert_equals(to_signed(self.processor.main_registers[reg]), -126)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_inc_reg_with_zero_result(self):
        # given
        self.given_register_contains_value('a', 0b11111111)

        self.given_next_instruction_is(0x3c)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_inc_reg_with_overflow(self):
        # given
        self.given_register_contains_value('b', 0b01111111)

        self.given_next_instruction_is(0x04)

        # when
        self.processor.execute()

        # then
        self.assert_register('b').equals(0b10000000)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()

    def test_inc_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0xbabe)
        self.memory[0xbabe] = 0b00000001

        self.given_next_instruction_is(0x34)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbabe).contains(0b00000010)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_inc_indexed_indirect(self):
        values = [('ix', [0xdd, 0x34]), ('iy', [0xfd, 0x34])]
        for register, op_codes in values:
            yield self.check_inc_indexed_indirect, register, op_codes

    def check_inc_indexed_indirect(self, register, op_codes):
        # given
        self.processor.index_registers[register] = 0xbeef
        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)
        self.memory[0xbeef + signed_offset] = 0b00111000

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef + signed_offset).contains(0b00111001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
