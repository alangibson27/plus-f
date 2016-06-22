package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpRla extends RotateOperation {

    public OpRla(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        final int value = accumulator.get();
        final int highBit = value >> 7;
        accumulator.set(((value << 1) & 0xff) | (flagsRegister.get(FlagsRegister.Flag.C) ? 1 : 0));
        setCarryAndNegateAfterRotate(highBit);
        return 4;
    }

    @Override
    public String toString() {
        return "rla";
    }
}
