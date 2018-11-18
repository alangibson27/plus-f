package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpCpl extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpCpl(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        accumulator.set(0xff - accumulator.get());
        flagsRegister.set(FlagsRegister.Flag.H, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
    }

    @Override
    public String toString() {
        return "cpl";
    }
}
