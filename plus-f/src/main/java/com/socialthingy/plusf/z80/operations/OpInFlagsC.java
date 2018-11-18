package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;

public class OpInFlagsC extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register bReg;
    private final Register cReg;
    private final IO io;

    public OpInFlagsC(final Processor processor, final IO io) {
        this.flagsRegister = processor.flagsRegister();
        this.io = io;
        this.bReg = processor.register("b");
        this.cReg = processor.register("c");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int value = io.read(cReg.get(), bReg.get());
        flagsRegister.set(FlagsRegister.Flag.S, (byte) value < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, value == 0);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(value));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(value);
    }

    @Override
    public String toString() {
        return "in (c)";
    }
}
