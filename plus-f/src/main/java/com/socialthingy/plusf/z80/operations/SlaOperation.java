package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

abstract class SlaOperation extends ShiftOperation {

    protected SlaOperation(final Processor processor) {
        super(processor);
    }

    protected int shift(final int value) {
        final int carry = value >> 7;
        final int result = (value << 1) & 0xff;
        updateFlags(result, carry);
        return result;
    }
}
