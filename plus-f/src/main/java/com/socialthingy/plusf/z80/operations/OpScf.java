package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpScf extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpScf(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, true);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
    }

    @Override
    public String toString() {
        return "scf";
    }
}
