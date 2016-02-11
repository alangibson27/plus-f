from unittest import SkipTest

from nose.tools import assert_equals, assert_false, assert_true
from processor_tests import TestHelper
from z80.processor import InterruptRequest


class AckReceiver:
    def __init__(self):
        self.ack = False

    def acknowledge(self):
        self.ack = True


class TestInterrupts(TestHelper):
    def test_memory_refresh_increments_after_operation(self):
        values = [(0b11111111, 0b10000000), (0b10000001, 0b10000010), (0b01111111, 0b00000001)]
        for initial_value, final_value in values:
            yield self.check_memory_refresh_increments_after_operation, initial_value, final_value

    def check_memory_refresh_increments_after_operation(self, initial_value, final_value):
        # given
        self.given_register_contains_value('r', 0b11111111)
        self.given_next_instruction_is(0x00)

        # when
        self.processor.execute()

        # then
        self.assert_special_register('r').equals(0b10000000)

    def test_ld_i_a(self):
        # given
        self.given_register_contains_value('a', 0xbe)
        self.given_next_instruction_is(0xed, 0x47)

        # when
        self.processor.execute()

        # then
        self.assert_special_register('i').equals(0xbe)

    def test_ld_a_i(self):
        # given
        self.given_register_contains_value('i', 0xff)
        self.given_next_instruction_is(0xed, 0x57)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0xff)
        self.assert_pc_address().equals(0x0002)

    def test_ld_r_a(self):
        # given
        self.given_register_contains_value('a', 0xbe)
        self.given_next_instruction_is(0xed, 0x4f)

        # when
        self.processor.execute()

        # then
        self.assert_special_register('r').equals(0xc0)

    def test_ld_a_r_preserves_iff2_value(self):
        for iff2_value in [True, False]:
            yield self.check_ld_a_r_preserves_iff2_value, iff2_value

    def check_ld_a_r_preserves_iff2_value(self, iff2_value):
        # given
        self.processor.iff[1] = iff2_value
        self.given_register_contains_value('r', 0xbe)
        self.given_next_instruction_is(0xed, 0x5f)

        # when
        self.processor.execute()

        # then
        self.assert_register('a').equals(0xbe)
        self.assert_flag('s').equals(True)
        self.assert_flag('z').equals(False)
        self.assert_flag('h').equals(False)
        self.assert_flag('p').equals(iff2_value)
        self.assert_flag('n').equals(False)

    def test_ei_enables_interrupts_after_following_instruction(self):
        # given
        self.maskable_interrupts_are_disabled()
        self.maskable_interrupt_mode_is(1)
        ack_receiver = self.an_im1_interrupt_is_generated()

        self.given_next_instruction_is(0x3c)
        self.given_next_instruction_is(0xfb)
        self.given_next_instruction_is(0x3c)
        self.given_next_instruction_is(0x3c)

        # when
        self.processor.execute()
        self.processor.execute()
        self.processor.execute()
        self.processor.execute()

        # then
        assert_true(ack_receiver.ack)
        self.assert_register('a').equals(0x02)
        self.assert_pc_address().equals(0x0038)

    def test_reti_returns_to_pc(self):
        # given
        self.routine_at(0x0000).does([0xfb,         # ei
                                      0xed, 0x56,   # im 1
                                      0x3c,         # inc a
                                      0x04])        # inc b

        self.routine_at(0x0038).does([0x3c,         # inc a
                                      0xed, 0x4d])  # reti

        ack = self.an_im1_interrupt_is_generated()

        # when
        self.execute_until_nop()

        # then
        assert_true(ack.ack)
        self.assert_register('a').equals(2)
        self.assert_register('b').equals(1)

    def test_reti_notifies_devices_of_interrupt_completion(self):
        raise SkipTest()

    def test_im0_invoked_when_maskable_interrupts_enabled(self):
        raise SkipTest()

    def test_im0_not_invoked_when_maskable_interrupts_disabled(self):
        raise SkipTest()

    def test_im1_invoked_when_maskable_interrupts_enabled(self):
        # given
        self.maskable_interrupts_are_enabled()
        self.given_stack_pointer_is(0xffff)

        self.maskable_interrupt_mode_is(1)
        self.given_next_instruction_is(0xcb, 0xc0)

        ack_receiver = self.an_im1_interrupt_is_generated()

        # when
        self.processor.execute()

        # then
        assert_true(ack_receiver.ack)
        self.assert_pc_address().equals(0x0038)
        self.assert_stack_pointer().equals(0xfffd)
        self.assert_memory(0xfffd).contains(0x04)
        self.assert_memory(0xfffe).contains(0x00)
        self.assert_register('b').equals(0)

    def test_im1_not_invoked_when_maskable_interrupts_disabled(self):
        # given
        self.maskable_interrupts_are_disabled()
        self.given_stack_pointer_is(0xffff)

        self.maskable_interrupt_mode_is(1)
        self.given_next_instruction_is(0xcb, 0xc0)

        ack_receiver = self.an_im1_interrupt_is_generated()

        # when
        self.processor.execute()

        # then
        assert_false(ack_receiver.ack)
        self.assert_pc_address().equals(0x0005)
        self.assert_stack_pointer().equals(0xffff)
        self.assert_register('b').equals(0x01)

    def test_im2_invoked_when_maskable_interrupts_enabled(self):
        # given
        self.maskable_interrupts_are_enabled()
        self.given_stack_pointer_is(0xffff)

        self.maskable_interrupt_mode_is(2)
        self.given_next_instruction_is(0xcb, 0xc0)

        self.memory[0xb0e0] = 0xef
        self.memory[0xb0e1] = 0xbe

        self.given_register_contains_value('i', 0xb0)
        self.given_register_contains_value('r', 0xe1)

        ack_receiver = self.an_im2_interrupt_is_generated()

        # when
        self.processor.execute()

        # then
        assert_true(ack_receiver.ack)
        self.assert_pc_address().equals(0xbeef)
        self.assert_stack_pointer().equals(0xfffd)
        self.assert_memory(0xfffd).contains(0x04)
        self.assert_memory(0xfffe).contains(0x00)
        self.assert_register('b').equals(0x00)

    def test_im2_not_invoked_when_maskable_interrupts_enabled(self):
        # given
        self.maskable_interrupts_are_disabled()
        self.given_stack_pointer_is(0xffff)

        self.maskable_interrupt_mode_is(2)
        self.given_next_instruction_is(0xcb, 0xc0)

        self.memory[0xb0e0] = 0xef
        self.memory[0xb0e1] = 0xbe

        self.given_register_contains_value('i', 0xb0)
        self.given_register_contains_value('r', 0xe1)

        ack_receiver = self.an_im2_interrupt_is_generated()

        # when
        self.processor.execute()

        # then
        assert_false(ack_receiver.ack)
        self.assert_pc_address().equals(0x0005)
        self.assert_stack_pointer().equals(0xffff)
        self.assert_register('b').equals(0x01)

    def test_nmi_when_maskable_interrupts_disabled(self):
        # given
        self.maskable_interrupts_are_disabled()
        self.given_stack_pointer_is(0xffff)

        # nmi service routine
        self.memory[0x0066] = 0xcb
        self.memory[0x0067] = 0xc7

        # when
        self.processor.nmi()
        self.an_im1_interrupt_is_generated()
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0068)
        self.assert_register('a').equals(0x01)

        self.assert_stack_pointer().equals(0xfffd)
        self.assert_memory(0xfffd).contains(0x01)
        self.assert_memory(0xfffe).contains(0x00)

    def test_nmi_when_maskable_interrupts_enabled(self):
        # given
        self.maskable_interrupts_are_enabled()
        self.given_stack_pointer_is(0xffff)

        # nmi service routine
        self.memory[0x0066] = 0xcb
        self.memory[0x0067] = 0xc7

        # when
        self.processor.nmi()
        self.an_im1_interrupt_is_generated()
        self.processor.execute()

        # then
        self.assert_pc_address().equals(0x0068)
        self.assert_register('a').equals(0x01)

        self.assert_stack_pointer().equals(0xfffd)
        self.assert_memory(0xfffd).contains(0x02)
        self.assert_memory(0xfffe).contains(0x00)

    def test_halt_executes_nops_until_non_maskable_interrupt(self):
        # given
        self.given_next_instruction_is(0x76)

        self.routine_at(0x0066).does([0x3c,         # inc a
                                      0xed, 0x45])  # retn

        # when
        self.processor.execute()

        self.processor.execute()
        assert_equals(str(self.processor.last_operation), 'nop')

        self.processor.nmi()
        self.processor.execute()

        # then
        assert_equals(str(self.processor.last_operation), 'inc a')
        self.assert_register('a').equals(0x01)

    def test_halt_executes_nops_until_maskable_interrupt_when_maskable_interrupts_enabled(self):
        # given
        self.routine_at(0x0038).does([0x3c,         # inc a
                                      0xed, 0x4d])  # retn
        self.maskable_interrupt_mode_is(1)
        self.maskable_interrupts_are_enabled()

        self.given_next_instruction_is(0x76)

        # when
        self.processor.execute()

        self.processor.execute()
        assert_equals(str(self.processor.last_operation), 'nop')

        self.an_im1_interrupt_is_generated()
        self.processor.execute()
        self.processor.execute()

        # then
        assert_equals(str(self.processor.last_operation), 'inc a')
        self.assert_register('a').equals(0x01)

    def test_halt_executes_nops_despite_maskable_interrupt_when_maskable_interrupts_disabled(self):
        # given
        self.routine_at(0x0038).does([0x3c,         # inc a
                                      0xed, 0x4d])  # retn
        self.maskable_interrupt_mode_is(1)
        self.maskable_interrupts_are_disabled()

        self.given_next_instruction_is(0x76)

        # when
        self.processor.execute()

        self.processor.execute()
        assert_equals(str(self.processor.last_operation), 'nop')

        self.an_im1_interrupt_is_generated()
        self.processor.execute()

        # then
        assert_equals(str(self.processor.last_operation), 'nop')

    def test_retn_reenables_maskable_interrupts(self):
        # given
        self.routine_at(0x0000).does([0xfb,         # ei
                                      0xed, 0x56,   # im 0
                                      0x3c,         # inc a
                                      0x04])        # inc b

        self.routine_at(0x0066).does([0x3c,         # inc a
                                      0xed, 0x45])  # retn

        self.routine_at(0x0038).does([0x3c,         # inc a
                                      0xed, 0x4d])  # reti

        # when
        self.execute_range(0x0000, 0x0003)
        ack = self.an_im1_interrupt_is_generated()
        self.processor.nmi()
        self.execute_until_nop()

        # then
        assert_true(ack.ack)
        self.assert_register('a').equals(3)
        self.assert_register('b').equals(1)

    def maskable_interrupts_are_enabled(self):
        self.given_next_instruction_is(0xfb)
        self.processor.execute()
        self.given_next_instruction_is(0x00)
        self.processor.execute()

    def maskable_interrupts_are_disabled(self):
        self.given_next_instruction_is(0xf3)
        self.processor.execute()

    interrupt_mode_op_codes = {0: [0xed, 0x46], 1: [0xed, 0x56], 2: [0xed, 0x5e]}

    def maskable_interrupt_mode_is(self, mode):
        self.given_next_instruction_is(self.interrupt_mode_op_codes[mode])
        self.processor.execute()

    def an_im1_interrupt_is_generated(self):
        ack_receiver = AckReceiver()
        request = InterruptRequest(ack_receiver.acknowledge)
        self.processor.interrupt(request)
        return ack_receiver

    def an_im2_interrupt_is_generated(self):
        ack_receiver = AckReceiver()
        request = InterruptRequest(ack_receiver.acknowledge)
        self.processor.interrupt(request)
        return ack_receiver

    def routine_at(self, address):
        return RoutineBuilder(self.memory, address)

    def execute_until_nop(self):
        self.processor.execute()
        while str(self.processor.last_operation) != 'nop':
            self.processor.execute()

    def execute_range(self, from_addr, to_addr):
        self.processor.special_registers['pc'] = from_addr
        while self.processor.special_registers['pc'] <= to_addr:
            self.processor.execute()


class RoutineBuilder:
    def __init__(self, memory, address):
        self.memory = memory
        self.address = address

    def does(self, op_codes):
        for op_code in op_codes:
            self.memory[self.address] = op_code
            self.address += 1