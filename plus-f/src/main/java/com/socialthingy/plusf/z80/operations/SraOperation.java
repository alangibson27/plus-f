package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

abstract class SraOperation extends ShiftOperation {

    protected SraOperation(final Processor processor) {
        super(processor);
    }

    protected int shift(final int value) {
        final int carry = value & 0b1;
        final int highBit = value & 0b10000000;
        final int result = (value >> 1) | highBit;
        updateFlags(result, carry);
        return result;
    }
}
