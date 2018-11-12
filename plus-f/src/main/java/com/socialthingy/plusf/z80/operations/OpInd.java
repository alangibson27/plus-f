package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpInd extends BlockInOperation {
    public OpInd(final Processor processor, final Clock clock, final Memory memory, final IO io) {
        super(processor, clock, memory, io);
    }

    @Override
    public void execute() {
        readThenDecrementB(-1);
        flagsRegister.set(FlagsRegister.Flag.Z, bReg.get() == 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);
    }

    @Override
    public String toString() {
        return "ind";
    }
}
