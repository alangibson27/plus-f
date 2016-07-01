package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpScf implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpScf(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, true);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
        return 4;
    }

    @Override
    public String toString() {
        return "scf";
    }
}
