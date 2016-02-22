package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpRrca extends RotateOperation {
    public OpRrca(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        final int value = accumulator.get();
        final int lowBit = value & 0b1;
        accumulator.set(value >> 1 | (lowBit * 0b10000000));
        setCarryAndNegateAfterRotate(lowBit);
        return 4;
    }
}
