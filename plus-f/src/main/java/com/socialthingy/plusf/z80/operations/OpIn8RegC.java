package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;

public class OpIn8RegC extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register bReg;
    private final Register cReg;
    private final Register destRegister;
    private final IO io;

    public OpIn8RegC(final Processor processor, final IO io, final Register register) {
        this.flagsRegister = processor.flagsRegister();
        this.io = io;
        this.destRegister = register;
        this.bReg = processor.register("b");
        this.cReg = processor.register("c");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int lowByte = cReg.get();
        final int highByte = bReg.get();
        contentionModel.applyIOContention(lowByte, highByte);

        final int value = io.read(lowByte, highByte);
        destRegister.set(value);

        flagsRegister.set(FlagsRegister.Flag.S, (byte) value < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, value == 0);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(value));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(destRegister.get());
    }

    @Override
    public String toString() {
        return "in " + destRegister.name() + ", (c)";
    }
}
