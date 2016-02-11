from nose.tools import assert_equals

from tests.processor.processor_tests import TestHelper, random_byte
from z80.funcs import big_endian_value

__author__ = 'alan'


class Test16BitLoadGroup(TestHelper):
    def test_ld_16reg_16reg(self):
        operations = [([0xf9], 'hl'), ([0xdd, 0xf9], 'ix'), ([0xfd, 0xf9], 'iy')]
        for op_codes, register_pair in operations:
            yield self.check_ld_16reg_16reg, op_codes, register_pair

    def check_ld_16reg_16reg(self, op_codes, register_pair):
        # given
        if register_pair == 'ix' or register_pair == 'iy':
            self.processor.index_registers[register_pair] = 0xbeef
        else:
            self.given_register_pair_contains_value(register_pair, 0xbeef)

        self.given_next_instruction_is(op_codes)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.special_registers['sp'], 0xbeef)

    def test_ld_16reg_immediate(self):
        operations = [
            ([0x01], 'bc'),
            ([0x11], 'de'),
            ([0x21], 'hl'),
            ([0x31], 'sp'),
            ([0xdd, 0x21], 'ix'),
            ([0xfd, 0x21], 'iy')
        ]

        for op_codes, register_pair in operations:
            yield self.check_ld_16reg_immediate, op_codes, register_pair

    def check_ld_16reg_immediate(self, op_codes, register_pair):
        # given
        little_endian_address = [random_byte(), random_byte()]
        self.given_next_instruction_is(op_codes, little_endian_address)

        # when
        self.processor.execute()

        # then
        if register_pair == 'ix':
            assert_equals(self.processor.index_registers['ix'], big_endian_value(little_endian_address))
        elif register_pair == 'iy':
            assert_equals(self.processor.index_registers['iy'], big_endian_value(little_endian_address))
        elif register_pair == 'sp':
            assert_equals(self.processor.special_registers['sp'], big_endian_value(little_endian_address))
        else:
            assert_equals(self.processor.main_registers[register_pair[0]], little_endian_address[1])
            assert_equals(self.processor.main_registers[register_pair[1]], little_endian_address[0])

    def test_push_without_wraparound(self):
        operations = [
            ([0xf5], 'af'),
            ([0xc5], 'bc'),
            ([0xd5], 'de'),
            ([0xe5], 'hl'),
            ([0xdd, 0xe5], 'ix'),
            ([0xfd, 0xe5], 'iy')
        ]

        for op_codes, register_pair in operations:
            yield self.check_push_without_wraparound, op_codes, register_pair

    def check_push_without_wraparound(self, op_codes, register_pair):
        lsb = random_byte()
        msb = random_byte()

        # given
        if register_pair == 'ix' or register_pair == 'iy':
            self.given_register_contains_value(register_pair, big_endian_value([lsb, msb]))
        else:
            self.given_register_contains_value(register_pair[0], msb)
            self.given_register_contains_value(register_pair[1], lsb)

        self.given_stack_pointer_is(0xffff)

        self.given_next_instruction_is(op_codes)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.special_registers['sp'], 0xfffd)
        assert_equals(self.memory[0xfffe], msb)
        assert_equals(self.memory[0xfffd], lsb)

    def test_push_with_wraparound(self):
        # given
        self.given_stack_pointer_is(0x0000)
        self.given_register_pair_contains_value('hl', 0xabcd)

        self.given_next_instruction_is(0xe5)

        # when
        self.processor.execute()

        # then
        assert_equals(self.memory[0xffff], 0xab)
        assert_equals(self.memory[0xfffe], 0xcd)

    def test_pop_without_wraparound(self):
        operations = [
            ([0xf1], 'af'), ([0xc1], 'bc'), ([0xd1], 'de'), ([0xe1], 'hl'), ([0xdd, 0xe1], 'ix'), ([0xfd, 0xe1], 'iy')
        ]

        for op_codes, register_pair in operations:
            yield self.check_pop_without_workaround, op_codes, register_pair

    def check_pop_without_workaround(self, op_codes, register_pair):
        # given
        msb = random_byte()
        lsb = random_byte()

        self.memory[0xfff0] = lsb
        self.memory[0xfff1] = msb

        self.given_stack_pointer_is(0xfff0)

        self.given_next_instruction_is(op_codes)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.special_registers['sp'], 0xfff2)

        if register_pair == 'ix' or register_pair == 'iy':
            assert_equals(self.processor.index_registers[register_pair], big_endian_value([lsb, msb]))
        else:
            assert_equals(self.processor.main_registers[register_pair[0]], msb)
            assert_equals(self.processor.main_registers[register_pair[1]], lsb)

    def test_pop_with_wraparound(self):
        # given
        self.memory[0xffff] = 0xab
        self.memory[0x0000] = 0xcd

        self.given_stack_pointer_is(0xffff)

        self.given_program_counter_is(0x1000)
        self.given_next_instruction_is(0xe1)

        # when
        self.processor.execute()

        # then
        assert_equals(self.processor.main_registers['h'], 0xcd)
        assert_equals(self.processor.main_registers['l'], 0xab)
        assert_equals(self.processor.special_registers['sp'], 0x0001)

    def test_ld_ext_addr_16reg(self):
        operations = [
            ([0x22], 'hl'),
            ([0xed, 0x43], 'bc'),
            ([0xed, 0x53], 'de'),
            ([0xed, 0x63], 'hl'),
            ([0xed, 0x73], 'sp'),
            ([0xdd, 0x22], 'ix'),
            ([0xfd, 0x22], 'iy')
        ]

        for op_codes, dest_register_pair in operations:
            yield self.check_ld_ext_addr_16reg, op_codes, dest_register_pair

    def check_ld_ext_addr_16reg(self, op_codes, dest_register_pair):
        # given
        if dest_register_pair == 'ix' or dest_register_pair == 'iy':
            self.processor.index_registers[dest_register_pair] = 0x1234
        elif dest_register_pair == 'sp':
            self.processor.special_registers[dest_register_pair] = 0x1234
        else:
            self.processor.main_registers[dest_register_pair[0]] = 0x12
            self.processor.main_registers[dest_register_pair[1]] = 0x34

        self.given_next_instruction_is(op_codes, 0xee, 0xbe)

        # when
        self.processor.execute()

        # then
        assert_equals(self.memory[0xbeee], 0x34)
        assert_equals(self.memory[0xbeef], 0x12)

    def test_ld_16reg_ext_addr_without_wraparound(self):
        operations = [
            ([0xed, 0x4b], 'bc'),
            ([0xed, 0x5b], 'de'),
            ([0x2a], 'hl'),
            ([0xed, 0x6b], 'hl'),
            ([0xed, 0x7b], 'sp'),
            ([0xdd, 0x2a], 'ix'),
            ([0xfd, 0x2a], 'iy')
        ]

        for op_codes, dest_register in operations:
            yield self.check_ld_16reg_ext_addr_without_wraparound, op_codes, dest_register

    def check_ld_16reg_ext_addr_without_wraparound(self, op_codes, dest_register):
        # given
        self.memory[0x1000] = 0x10
        self.memory[0x1001] = 0x20

        self.given_next_instruction_is(op_codes, 0x00, 0x10)

        # when
        self.processor.execute()

        # then
        if dest_register == 'ix' or dest_register == 'iy':
            assert_equals(self.processor.index_registers[dest_register], 0x2010)
        elif dest_register == 'sp':
            assert_equals(self.processor.special_registers['sp'], 0x2010)
        else:
            assert_equals(self.processor.main_registers[dest_register[0]], 0x20)
            assert_equals(self.processor.main_registers[dest_register[1]], 0x10)