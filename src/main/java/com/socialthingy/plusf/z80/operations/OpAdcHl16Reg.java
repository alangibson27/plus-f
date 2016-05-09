package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAdcHl16Reg implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register hlReg;
    private final Register sourceReg;

    public OpAdcHl16Reg(final Processor processor, final Register sourceReg) {
        this.flagsRegister = processor.flagsRegister();
        this.hlReg = processor.register("hl");
        this.sourceReg = sourceReg;
    }

    @Override
    public int execute() {
        final int hlValue = hlReg.get();
        final int carry = flagsRegister.get(FlagsRegister.Flag.C) ? 1 : 0;
        final int[] result = Bitwise.addWord(hlValue, (sourceReg.get() + carry) & 0xffff);

        flagsRegister.set(FlagsRegister.Flag.S, (result[0] & 0x8000) > 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result[0] == 0);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] == 1);
        flagsRegister.set(FlagsRegister.Flag.P, ((short) hlValue < 0) != ((short) result[0] < 0));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, result[2] == 1);

        hlReg.set(result[0]);
        return 15;
    }
}
