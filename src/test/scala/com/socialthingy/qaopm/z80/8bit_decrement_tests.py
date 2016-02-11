import random
from nose.tools import assert_equals
from z80.funcs import to_signed
from tests.processor.processor_tests import TestHelper


class Test8BitDecrementOps(TestHelper):
    def test_dec_reg_with_negative_result(self):
        values = [('a', 0x3d), ('b', 0x05), ('c', 0x0d), ('d', 0x15), ('e', 0x1d), ('h', 0x25), ('l', 0x2d)]
        for reg, op_code in values:
            yield self.check_dec_reg_with_negative_result, reg, op_code

    def check_dec_reg_with_negative_result(self, reg, op_code):
        # given
        self.given_register_contains_value(reg, 0b10000001)

        self.given_next_instruction_is(op_code)

        # when
        self.processor.execute()

        # then
        assert_equals(to_signed(self.processor.main_registers[reg]), -128)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_dec_reg_with_zero_result(self):
        # given
        self.given_register_contains_value('a', 0b00000001)

        self.given_next_instruction_is(0x3d)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_dec_reg_with_overflow(self):
        # given
        self.given_register_contains_value('b', 0b10000000)

        self.given_next_instruction_is(0x05)

        # when
        self.processor.execute()

        # then
        self.assert_register('b').equals(0b01111111)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()

    def test_dec_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0xbabe)
        self.memory[0xbabe] = 0b00000010

        self.given_next_instruction_is(0x35)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbabe).contains(0b00000001)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_dec_indexed_indirect(self):
        values = [('ix', [0xdd, 0x35]), ('iy', [0xfd, 0x35])]
        for register, op_codes in values:
            yield self.check_dec_indexed_indirect, register, op_codes

    def check_dec_indexed_indirect(self, register, op_codes):
        # given
        self.processor.index_registers[register] = 0xbeef
        offset = random.randint(0, 255)
        signed_offset = to_signed(offset)
        self.memory[0xbeef + signed_offset] = 0b00111000

        self.given_next_instruction_is(op_codes, offset)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef + signed_offset).contains(0b00110111)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()
