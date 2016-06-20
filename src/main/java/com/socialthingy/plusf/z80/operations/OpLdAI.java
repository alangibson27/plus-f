package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.FlagsRegister.Flag;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdAI implements Operation {
    private final Processor processor;
    private final Register iReg;
    private final Register aReg;
    private final FlagsRegister flags;

    public OpLdAI(final Processor processor) {
        this.processor = processor;
        this.iReg = processor.register("i");
        this.aReg = processor.register("a");
        this.flags = processor.flagsRegister();
    }

    @Override
    public int execute() {
        final int iValue = iReg.get();
        aReg.set(iValue);
        flags.set(Flag.P, processor.getIff(1));
        flags.set(Flag.Z, iValue == 0);
        flags.set(Flag.S, (byte) iValue < 0);
        flags.set(Flag.N, false);
        flags.set(Flag.H, false);
        return 9;
    }

    @Override
    public String toString() {
        return "ld a, i";
    }
}
