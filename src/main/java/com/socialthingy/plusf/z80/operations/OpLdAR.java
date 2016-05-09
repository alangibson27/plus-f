package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdAR implements Operation {
    private final Processor processor;
    private final Register rReg;
    private final Register aReg;
    private final FlagsRegister flagsRegister;

    public OpLdAR(final Processor processor) {
        this.processor = processor;
        this.rReg = processor.register("r");
        this.aReg = processor.register("a");
        this.flagsRegister = processor.flagsRegister();
    }

    @Override
    public int execute() {
        final int value = rReg.get();
        aReg.set(value);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) value < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, value == 0);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, processor.getIff(1));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        return 9;
    }
}
