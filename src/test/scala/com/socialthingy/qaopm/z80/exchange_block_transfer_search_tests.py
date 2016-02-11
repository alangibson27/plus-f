from nose.tools import *

from tests.processor.processor_tests import TestHelper


class TestExchangeBlockTransferAndSearch(TestHelper):
    def test_ex_de_hl(self):
        # given
        self.given_register_pair_contains_value('de', 0x1234)
        self.given_register_pair_contains_value('hl', 0xabcd)

        self.given_next_instruction_is(0xeb)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1234)
        self.assert_register_pair('de').equals(0xabcd)

    def test_ex_af_alt_af(self):
        # given
        self.given_register_pair_contains_value('af', 0x1234)
        self.given_alt_register_pair_contains_value('af', 0xabcd)

        self.given_next_instruction_is(0x08)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('af').equals(0xabcd)
        self.assert_alt_register_pair('af').equals(0x1234)

    def test_exx(self):
        # given
        self.given_register_pair_contains_value('bc', 0x1234)
        self.given_alt_register_pair_contains_value('bc', 0x4321)

        self.given_register_pair_contains_value('de', 0x5678)
        self.given_alt_register_pair_contains_value('de', 0x8765)

        self.given_register_pair_contains_value('hl', 0x9abc)
        self.given_alt_register_pair_contains_value('hl', 0xcba9)

        self.given_next_instruction_is(0xd9)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('bc').equals(0x4321)
        self.assert_alt_register_pair('bc').equals(0x1234)

        self.assert_register_pair('de').equals(0x8765)
        self.assert_alt_register_pair('de').equals(0x5678)

        self.assert_register_pair('hl').equals(0xcba9)
        self.assert_alt_register_pair('hl').equals(0x9abc)

    def test_ex_sp_indirect_hl(self):
        # given
        self.given_stack_pointer_is(0xbeef)
        self.given_register_pair_contains_value('hl', 0x1234)

        self.memory[0xbeef] = 0xba
        self.memory[0xbef0] = 0xbe

        self.given_next_instruction_is(0xe3)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef).contains(0x34)
        self.assert_memory(0xbef0).contains(0x12)
        self.assert_register_pair('hl').equals(0xbeba)

    def test_ex_sp_indirect_index_reg(self):
        operations = [([0xdd, 0xe3], 'ix'), ([0xfd, 0xe3], 'iy')]
        for op_codes, register_pair in operations:
            yield self.check_ex_sp_indirect_index_reg, op_codes, register_pair

    def check_ex_sp_indirect_index_reg(self, op_codes, register_pair):
        # given
        self.given_stack_pointer_is(0xbeef)
        self.memory[0xbeef] = 0x12
        self.memory[0xbef0] = 0x34

        self.processor.index_registers[register_pair] = 0xbeba

        self.given_next_instruction_is(op_codes)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef).contains(0xba)
        self.assert_memory(0xbef0).contains(0xbe)

        assert_equals(self.processor.index_registers[register_pair], 0x3412)

    def test_ldi_with_bc_decrementing_to_nonzero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0xbeef)

        self.memory[0x1000] = 0xba

        self.given_next_instruction_is(0xed, 0xa0)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x2000).contains(0xba)
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('de').equals(0x2001)
        self.assert_register_pair('bc').equals(0xbeee)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()

    def test_ldi_with_bc_decrementing_to_zero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x0001)

        self.memory[0x1000] = 0xba

        self.given_next_instruction_is(0xed, 0xa0)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x2000).contains(0xba)
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('de').equals(0x2001)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_ldir_with_bc_greater_than_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x000a)

        self.memory[0x1000] = 0xff

        self.given_next_instruction_is(0xed, 0xb0)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)
        self.assert_memory(0x2000).contains(0xff)
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('de').equals(0x2001)
        self.assert_register_pair('bc').equals(0x0009)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_ldir_with_bc_equal_to_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x0001)

        self.memory[0x1000] = 0xff

        self.given_next_instruction_is(0xed, 0xb0)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0002)
        self.assert_memory(0x2000).contains(0xff)
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('de').equals(0x2001)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_ldir_with_bc_equal_to_zero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x0000)

        self.memory[0x1000] = 0xff

        self.given_next_instruction_is(0xed, 0xb0)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)
        self.assert_memory(0x2000).contains(0xff)
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('de').equals(0x2001)
        self.assert_register_pair('bc').equals(0xffff)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_ldd_with_bc_decrementing_to_nonzero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0xbeef)

        self.memory[0x1000] = 0xba

        self.given_next_instruction_is(0xed, 0xa8)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x2000).contains(0xba)
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('de').equals(0x1fff)
        self.assert_register_pair('bc').equals(0xbeee)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()

    def test_ldd_with_bc_decrementing_to_zero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x0001)

        self.memory[0x1000] = 0xba

        self.given_next_instruction_is(0xed, 0xa8)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0x2000).contains(0xba)
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('de').equals(0x1fff)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_lddr_with_bc_greater_than_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x000a)

        self.memory[0x1000] = 0xff

        self.given_next_instruction_is(0xed, 0xb8)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)
        self.assert_memory(0x2000).contains(0xff)
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('de').equals(0x1fff)
        self.assert_register_pair('bc').equals(0x0009)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_lddr_with_bc_equal_to_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x0001)

        self.memory[0x1000] = 0xff

        self.given_next_instruction_is(0xed, 0xb8)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0002)
        self.assert_memory(0x2000).contains(0xff)
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('de').equals(0x1fff)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_lddr_with_bc_equal_to_zero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.given_register_pair_contains_value('de', 0x2000)
        self.given_register_pair_contains_value('bc', 0x0000)

        self.memory[0x1000] = 0xff

        self.given_next_instruction_is(0xed, 0xb8)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)
        self.assert_memory(0x2000).contains(0xff)
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('de').equals(0x1fff)
        self.assert_register_pair('bc').equals(0xffff)

        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_reset()

    def test_cpi_with_memory_equal_to_a_and_bc_greater_than_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0090)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xa1)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0x008f)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()

    def test_cpi_with_memory_equal_to_a_and_bc_equal_to_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xa1)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpi_with_memory_less_than_a_and_half_borrow(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0b00001000

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0b10000000)

        self.given_next_instruction_is(0xed, 0xa1)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpi_with_memory_greater_than_a_and_half_borrow(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0b00001000

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0b10000000)

        self.given_next_instruction_is(0xed, 0xa1)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpi_with_memory_greater_than_a_and_full_borrow(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0b10000000

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0b00000001)

        self.given_next_instruction_is(0xed, 0xa1)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpir_with_memory_equal_to_a_and_bc_greater_than_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0090)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xb1)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)

        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0x008f)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()

    def test_cpir_with_memory_equal_to_a_and_bc_equal_to_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xb1)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0002)

        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpir_with_memory_equal_to_a_and_bc_equal_to_zero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0000)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xb1)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)

        self.assert_register_pair('hl').equals(0x1001)
        self.assert_register_pair('bc').equals(0xffff)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()

    def test_cpd_with_memory_equal_to_a_and_bc_greater_than_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0090)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xa9)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0x008f)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()

    def test_cpd_with_memory_equal_to_a_and_bc_equal_to_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xa9)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpd_with_memory_less_than_a_and_half_borrow(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0b00001000

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0b10000000)

        self.given_next_instruction_is(0xed, 0xa9)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpd_with_memory_greater_than_a_and_half_borrow(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0b00001000

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0b10000000)

        self.given_next_instruction_is(0xed, 0xa9)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_set()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpd_with_memory_greater_than_a_and_full_borrow(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0b10000000

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0b00000001)

        self.given_next_instruction_is(0xed, 0xa9)

        # when
        self.processor.execute()

        # then
        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpdr_with_memory_equal_to_a_and_bc_greater_than_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0090)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xb9)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)

        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0x008f)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()

    def test_cpdr_with_memory_equal_to_a_and_bc_equal_to_one(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0001)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xb9)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0002)

        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0x0000)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_reset()
        self.assert_flag('n').is_set()

    def test_cpdr_with_memory_equal_to_a_and_bc_equal_to_zero(self):
        # given
        self.given_register_pair_contains_value('hl', 0x1000)
        self.memory[0x1000] = 0xbe

        self.given_register_pair_contains_value('bc', 0x0000)
        self.given_register_contains_value('a', 0xbe)

        self.given_next_instruction_is(0xed, 0xb9)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)

        self.assert_register_pair('hl').equals(0x0fff)
        self.assert_register_pair('bc').equals(0xffff)

        self.assert_flag('s').is_reset()
        self.assert_flag('z').is_set()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_set()
