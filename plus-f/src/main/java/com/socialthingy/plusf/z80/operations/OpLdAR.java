package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdAR extends Operation {
    private final Processor processor;
    private final Register rReg;
    private final Register aReg;
    private final FlagsRegister flagsRegister;

    public OpLdAR(final Processor processor, final Clock clock) {
        super(clock);
        this.processor = processor;
        this.rReg = processor.register("r");
        this.aReg = processor.register("a");
        this.flagsRegister = processor.flagsRegister();
    }

    @Override
    public void execute() {
        final int value = rReg.get();
        aReg.set(value);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) value < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, value == 0);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, processor.getIff(1));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(value);
        clock.tick(1);
    }

    @Override
    public String toString() {
        return "ld a, r";
    }
}
