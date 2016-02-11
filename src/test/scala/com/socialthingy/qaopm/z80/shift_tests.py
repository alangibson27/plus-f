import random

from z80.funcs import to_signed
from processor_tests import TestHelper


class TestShift(TestHelper):
    def test_sla_reg(self):
        regs = [(0x20, 'b'), (0x21, 'c'), (0x22, 'd'), (0x23, 'e'), (0x24, 'h'), (0x25, 'l'), (0x27, 'a')]
        values = [(0b10101010, True,  0b01010100, True, False, False, False),
                  (0b10101010, False, 0b01010100, True, False, False, False),
                  (0b01010101, True,  0b10101010, False, True, False, True),
                  (0b01010101, False, 0b10101010, False, True, False, True),
                  (0b00000000, False, 0b00000000, False, False, True, True)]
        for op_code, reg in regs:
            for initial_acc, initial_carry, final_acc, final_carry, s, z, p in values:
                yield self.check_reg_shift, reg, op_code, initial_acc, initial_carry, final_acc, final_carry, s, z, p

    def check_reg_shift(self, reg, op_code, initial_acc, initial_carry, final_acc, final_carry, s, z, p):
        # given
        self.given_register_contains_value(reg, initial_acc)
        self.processor.set_condition('c', initial_carry)

        self.given_next_instruction_is(0xcb, op_code)

        # when
        self.processor.execute()

        # then
        self.assert_register(reg).equals(final_acc)

        self.assert_flag('s').equals(s)
        self.assert_flag('z').equals(z)
        self.assert_flag('h').is_reset()
        self.assert_flag('p').equals(p)
        self.assert_flag('n').is_reset()
        self.assert_flag('c').equals(final_carry)

    def test_sla_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0x4000)
        self.processor.set_condition('c', False)
        self.memory[0x4000] = 0b10101010

        self.given_next_instruction_is(0xcb, 0x26)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x4000).contains(0b01010100)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_sla_indexed_indirect(self):
        values = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in values:
            yield self.check_sla_indexed_indirect, op_code, reg

    def check_sla_indexed_indirect(self, op_code, reg):
        # given
        offset = random.randint(0, 255)
        address = 0x4000 + to_signed(offset)
        self.processor.index_registers[reg] = 0x4000
        self.processor.set_condition('c', False)
        self.memory[address] = 0b10101010

        self.given_next_instruction_is(op_code, 0xcb, offset, 0x26)

        # when
        self.processor.execute()

        # then
        self.assert_memory(address).contains(0b01010100)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_sra_reg(self):
        regs = [(0x28, 'b'), (0x29, 'c'), (0x2a, 'd'), (0x2b, 'e'), (0x2c, 'h'), (0x2d, 'l'), (0x2f, 'a')]
        values = [(0b10101010, True,  0b11010101, False, True, False, False),
                  (0b10101010, False, 0b11010101, False, True, False, False),
                  (0b01010101, True,  0b00101010, True, False, False, False),
                  (0b01010101, False, 0b00101010, True, False, False, False),
                  (0b00000000, False, 0b00000000, False, False, True, True)]

        for op_code, reg in regs:
            for initial_acc, initial_carry, final_acc, final_carry, s, z, p in values:
                yield self.check_reg_shift, reg, op_code, initial_acc, initial_carry, final_acc, final_carry, s, z, p

    def test_sra_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0x4000)
        self.processor.set_condition('c', False)
        self.memory[0x4000] = 0b10101010

        self.given_next_instruction_is(0xcb, 0x2e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x4000).contains(0b11010101)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_sra_indexed_indirect(self):
        values = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in values:
            yield self.check_sra_indexed_indirect, op_code, reg

    def check_sra_indexed_indirect(self, op_code, reg):
        # given
        offset = random.randint(0, 255)
        address = 0x4000 + to_signed(offset)
        self.processor.index_registers[reg] = 0x4000
        self.processor.set_condition('c', False)
        self.memory[address] = 0b10101010

        self.given_next_instruction_is(op_code, 0xcb, offset, 0x2e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(address).contains(0b11010101)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_srl_reg(self):
        regs = [(0x38, 'b'), (0x39, 'c'), (0x3a, 'd'), (0x3b, 'e'), (0x3c, 'h'), (0x3d, 'l'), (0x3f, 'a')]
        values = [(0b10101010, True,  0b01010101, False, False, False, True),
                  (0b10101010, False, 0b01010101, False, False, False, True),
                  (0b01010101, True,  0b00101010, True, False, False, False),
                  (0b01010101, False, 0b00101010, True, False, False, False),
                  (0b00000000, False, 0b00000000, False, False, True, True)]

        for op_code, reg in regs:
            for initial_acc, initial_carry, final_acc, final_carry, s, z, p in values:
                yield self.check_reg_shift, reg, op_code, initial_acc, initial_carry, final_acc, final_carry, s, z, p

    def test_srl_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0x4000)
        self.processor.set_condition('c', False)
        self.memory[0x4000] = 0b10101010

        self.given_next_instruction_is(0xcb, 0x3e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x4000).contains(0b01010101)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_sll_reg(self):
        regs = [(0x30, 'b'), (0x31, 'c'), (0x32, 'd'), (0x33, 'e'), (0x34, 'h'), (0x35, 'l'), (0x37, 'a')]
        values = [(0b10101010, True,  0b01010101, True, False, False, True),
                  (0b10101010, False, 0b01010101, True, False, False, True),
                  (0b01010101, True,  0b10101011, False, True, False, False),
                  (0b01010101, False, 0b10101011, False, True, False, False),
                  (0b00000000, False, 0b00000001, False, False, False, False)]

        for op_code, reg in regs:
            for initial_acc, initial_carry, final_acc, final_carry, s, z, p in values:
                yield self.check_reg_shift, reg, op_code, initial_acc, initial_carry, final_acc, final_carry, s, z, p

    def test_sll_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0x4000)
        self.processor.set_condition('c', False)
        self.memory[0x4000] = 0b10101010

        self.given_next_instruction_is(0xcb, 0x36)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x4000).contains(0b01010101)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_srl_indexed_indirect(self):
        values = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in values:
            yield self.check_srl_indexed_indirect, op_code, reg

    def check_srl_indexed_indirect(self, op_code, reg):
        # given
        offset = random.randint(0, 255)
        address = 0x4000 + to_signed(offset)
        self.processor.index_registers[reg] = 0x4000
        self.processor.set_condition('c', False)
        self.memory[address] = 0b10101010

        self.given_next_instruction_is(op_code, 0xcb, offset, 0x3e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(address).contains(0b01010101)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_rl_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0x4000)
        self.processor.set_condition('c', False)
        self.memory[0x4000] = 0b10101010

        self.given_next_instruction_is(0xcb, 0x16)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x4000).contains(0b01010100)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_rl_indexed_indirect(self):
        values = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in values:
            yield self.check_rl_indexed_indirect, op_code, reg

    def check_rl_indexed_indirect(self, op_code, reg):
        # given
        offset = random.randint(0, 255)
        address = 0x4000 + to_signed(offset)
        self.processor.index_registers[reg] = 0x4000
        self.processor.set_condition('c', False)
        self.memory[address] = 0b10101010

        self.given_next_instruction_is(op_code, 0xcb, offset, 0x16)

        # when
        self.processor.execute()

        # then
        self.assert_memory(address).contains(0b01010100)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_set()

    def test_rrc_reg(self):
        regs = [(0x08, 'b'), (0x09, 'c'), (0x0a, 'd'), (0x0b, 'e'), (0x0c, 'h'), (0x0d, 'l'), (0x0f, 'a')]
        values = [(0b10101010, True,  0b01010101, False, False, False, True),
                  (0b10101010, False, 0b01010101, False, False, False, True),
                  (0b01010101, True,  0b10101010, True, True, False, True),
                  (0b01010101, False, 0b10101010, True, True, False, True),
                  (0b00000000, False, 0b00000000, False, False, True, True)]

        for op_code, reg in regs:
            for initial_acc, initial_carry, final_acc, final_carry, s, z, p in values:
                yield self.check_reg_shift, reg, op_code, initial_acc, initial_carry, final_acc, final_carry, s, z, p

    def test_rrc_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0x4000)
        self.processor.set_condition('c', False)
        self.memory[0x4000] = 0b10101010

        self.given_next_instruction_is(0xcb, 0x0e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x4000).contains(0b01010101)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_rrc_indexed_indirect(self):
        values = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in values:
            yield self.check_rrc_indexed_indirect, op_code, reg

    def check_rrc_indexed_indirect(self, op_code, reg):
        # given
        offset = random.randint(0, 255)
        address = 0x4000 + to_signed(offset)
        self.processor.index_registers[reg] = 0x4000
        self.processor.set_condition('c', False)
        self.memory[address] = 0b10101010

        self.given_next_instruction_is(op_code, 0xcb, offset, 0x0e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(address).contains(0b01010101)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_rr_reg(self):
        regs = [(0x18, 'b'), (0x19, 'c'), (0x1a, 'd'), (0x1b, 'e'), (0x1c, 'h'), (0x1d, 'l'), (0x1f, 'a')]
        values = [(0b10101010, True,  0b11010101, False, True, False, False),
                  (0b10101010, False, 0b01010101, False, False, False, True),
                  (0b01010101, True,  0b10101010, True, True, False, True),
                  (0b01010101, False, 0b00101010, True, False, False, False),
                  (0b00000000, False, 0b00000000, False, False, True, True)]

        for op_code, reg in regs:
            for initial_acc, initial_carry, final_acc, final_carry, s, z, p in values:
                yield self.check_reg_shift, reg, op_code, initial_acc, initial_carry, final_acc, final_carry, s, z, p

    def test_rr_hl_indirect(self):
        # given
        self.given_register_pair_contains_value('hl', 0x4000)
        self.processor.set_condition('c', True)
        self.memory[0x4000] = 0b10101010

        self.given_next_instruction_is(0xcb, 0x1e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x4000).contains(0b11010101)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()

    def test_rr_indexed_indirect(self):
        values = [(0xdd, 'ix'), (0xfd, 'iy')]
        for op_code, reg in values:
            yield self.check_rr_indexed_indirect, op_code, reg

    def check_rr_indexed_indirect(self, op_code, reg):
        # given
        offset = random.randint(0, 255)
        address = 0x4000 + to_signed(offset)
        self.processor.index_registers[reg] = 0x4000
        self.processor.set_condition('c', True)
        self.memory[address] = 0b10101010

        self.given_next_instruction_is(op_code, 0xcb, offset, 0x1e)

        # when
        self.processor.execute()

        # then
        self.assert_memory(address).contains(0b11010101)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()
        self.assert_flag('c').is_reset()
