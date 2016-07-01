package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAddHl16Reg implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register hlReg;
    private final Register sourceReg;

    public OpAddHl16Reg(final Processor processor, final Register sourceReg) {
        this.flagsRegister = processor.flagsRegister();
        this.hlReg = processor.register("hl");
        this.sourceReg = sourceReg;
    }

    @Override
    public int execute() {
        final int[] result = Bitwise.addWord(hlReg.get(), sourceReg.get());
        hlReg.set(result[0]);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] == 1);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, result[2] == 1);
        flagsRegister.setUndocumentedFlagsFromValue(result[0] >> 8);
        return 11;
    }

    @Override
    public String toString() {
        return "add hl, " + sourceReg.name();
    }
}
