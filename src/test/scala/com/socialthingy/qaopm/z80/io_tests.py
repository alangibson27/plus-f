from processor_tests import TestHelper


class TestIO(TestHelper):
    def test_in_a_reads_value(self):
        # given
        self.io.stub_read(0xff, 0xbe, 0xaa)

        self.given_register_contains_value('a', 0xbe)
        self.given_next_instruction_is(0xdb, 0xff)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0xaa)

    def test_in_8reg_c_reads_value(self):
        values = [([0xed, 0x40], 'b'), ([0xed, 0x48], 'c'), ([0xed, 0x50], 'd'), ([0xed, 0x58], 'e'),
                  ([0xed, 0x60], 'h'), ([0xed, 0x68], 'l'), ([0xed, 0x78], 'a')]
        for op_codes, destination in values:
            yield self.check_in_8reg_c_reads_value, op_codes, destination

    def check_in_8reg_c_reads_value(self, op_codes, destination):
        # given
        self.io.stub_read(0xfe, 0xef, 0xaa)

        self.given_register_contains_value('b', 0xef)
        self.given_register_contains_value('c', 0xfe)

        self.given_next_instruction_is(op_codes)

        # when
        self.processor.execute()

        # then
        self.assert_register(destination).equals(0xaa)
        self.assert_flag('s').is_set()
        self.assert_flag('z').is_reset()
        self.assert_flag('h').is_reset()
        self.assert_flag('p').is_set()
        self.assert_flag('n').is_reset()

    def test_ini_reads_value_and_decrements_b_to_zero(self):
        # given
        self.io.stub_read(0xef, 0x01, 0xaa)

        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xa2)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_ini_reads_value_and_decrements_b_to_nonzero(self):
        # given
        self.io.stub_read(0xef, 0x10, 0xaa)

        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xa2)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_reset()
        self.assert_flag('n').is_set()

    def test_inir_reads_value_and_does_not_repeat_when_b_decrements_to_zero(self):
        # given
        self.io.stub_read(0xef, 0x01, 0xaa)

        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xb2)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0002)
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_inir_reads_value_and_repeats_when_b_decrements_to_nonzero(self):
        # given
        self.io.stub_read(0xef, 0x10, 0xaa)

        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xb2)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_ind_reads_value_and_decrements_b_to_zero(self):
        # given
        self.io.stub_read(0xef, 0x01, 0xaa)

        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xaa)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_ind_reads_value_and_decrements_b_to_nonzero(self):
        # given
        self.io.stub_read(0xef, 0x10, 0xaa)

        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xaa)

        # when
        self.processor.execute()

        # then
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_reset()
        self.assert_flag('n').is_set()

    def test_indr_reads_value_and_does_not_repeat_when_b_decrements_to_zero(self):
        # given
        self.io.stub_read(0xef, 0x01, 0xaa)

        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xba)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0002)
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_indr_reads_value_and_repeats_when_b_decrements_to_nonzero(self):
        # given
        self.io.stub_read(0xef, 0x10, 0xaa)

        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)

        self.given_next_instruction_is(0xed, 0xba)

        # when
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0000)
        self.assert_memory(0xbeef).contains(0xaa)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_out_a_writes_value(self):
        # given
        self.given_register_contains_value('a', 0xbe)
        self.given_next_instruction_is(0xd3, 0xff)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xff, 0xbe, 0xbe)

    def test_out_c_zero_writes_zero(self):
        # given
        self.given_register_contains_value('b', 0xbe)
        self.given_register_contains_value('c', 0xef)

        self.given_next_instruction_is(0xed, 0x71)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0xbe, 0x00)

    def test_out_8reg_c_writes_value(self):
        values = [([0xed, 0x41], 'b'), ([0xed, 0x49], 'c'), ([0xed, 0x51], 'd'), ([0xed, 0x59], 'e'),
                  ([0xed, 0x61], 'h'), ([0xed, 0x69], 'l'), ([0xed, 0x79], 'a')]
        for op_codes, destination in values:
            yield self.check_out_8reg_c_writes_value, op_codes, destination

    def check_out_8reg_c_writes_value(self, op_codes, destination):
        # given
        self.given_register_contains_value(destination, 0xef)
        self.given_register_contains_value('b', 0xef)
        self.given_register_contains_value('c', 0xfe)

        self.given_next_instruction_is(op_codes)

        # when
        self.processor.execute()

        # then
        if destination == 'c':
            self.io.verify_write(0xfe, 0xef, 0xfe)
        else:
            self.io.verify_write(0xfe, 0xef, 0xef)

    def test_outi_writes_value_and_decrements_b_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xa3)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x00, 0xaa)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_outi_reads_value_and_decrements_b_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xa3)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x0f, 0xaa)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_reset()
        self.assert_flag('n').is_set()

    def test_otir_reads_value_and_does_not_repeat_when_b_decrements_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xb3)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x00, 0xaa)
        self.assert_pc_address().equals(0x0002)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_otir_reads_value_and_repeats_when_b_decrements_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xb3)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x0f, 0xaa)
        self.assert_pc_address().equals(0x0000)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbef0)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_outd_writes_value_and_decrements_b_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xab)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x00, 0xaa)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_outd_reads_value_and_decrements_b_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xab)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x0f, 0xaa)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_reset()
        self.assert_flag('n').is_set()

    def test_otdr_reads_value_and_does_not_repeat_when_b_decrements_to_zero(self):
        # given
        self.given_register_contains_value('b', 0x01)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xbb)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x00, 0xaa)
        self.assert_pc_address().equals(0x0002)
        self.assert_register('b').equals(0x00)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()

    def test_otdr_reads_value_and_repeats_when_b_decrements_to_nonzero(self):
        # given
        self.given_register_contains_value('b', 0x10)
        self.given_register_contains_value('c', 0xef)

        self.given_register_pair_contains_value('hl', 0xbeef)
        self.memory[0xbeef] = 0xaa

        self.given_next_instruction_is(0xed, 0xbb)

        # when
        self.processor.execute()

        # then
        self.io.verify_write(0xef, 0x0f, 0xaa)
        self.assert_pc_address().equals(0x0000)
        self.assert_register('b').equals(0x0f)
        self.assert_register_pair('hl').equals(0xbeee)
        self.assert_flag('z').is_set()
        self.assert_flag('n').is_set()