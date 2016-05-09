package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpRra extends RotateOperation {
    public OpRra(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        final int value = accumulator.get();
        final int lowBit = value & 0b1;
        accumulator.set((value >> 1) | (flagsRegister.get(FlagsRegister.Flag.C) ? 0b10000000 : 0));
        setCarryAndNegateAfterRotate(lowBit);
        return 4;
    }
}
