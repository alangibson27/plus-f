package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

abstract class SrlOperation extends ShiftOperation {

    protected SrlOperation(final Processor processor) {
        super(processor);
    }

    protected int shift(final int value) {
        final int carry = value & 0b1;
        final int result = value >> 1;
        updateFlags(result, carry);
        return result;
    }
}
