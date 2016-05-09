package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpScf implements Operation {
    private final FlagsRegister flagsRegister;

    public OpScf(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
    }

    @Override
    public int execute() {
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, true);
        return 4;
    }
}
