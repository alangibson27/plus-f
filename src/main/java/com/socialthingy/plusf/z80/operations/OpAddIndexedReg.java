package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAddIndexedReg implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register indexRegister;
    private final Register sourceReg;

    public OpAddIndexedReg(final Processor processor, final Register indexRegister, final Register sourceReg) {
        this.flagsRegister = processor.flagsRegister();
        this.indexRegister = indexRegister;
        this.sourceReg = sourceReg;
    }

    @Override
    public int execute() {
        final int[] result = Bitwise.addWord(indexRegister.get(), sourceReg.get());
        indexRegister.set(result[0]);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] == 1);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, result[2] == 1);
        return 15;
    }
}
