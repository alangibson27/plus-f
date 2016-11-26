package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.Bitwise.FULL_CARRY_BIT;
import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

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
        final int result = Bitwise.addWord(hlValue, sourceReg.get() + carry);
        final int answer = result & 0xffff;

        flagsRegister.set(FlagsRegister.Flag.S, (answer & 0x8000) > 0);
        flagsRegister.set(FlagsRegister.Flag.Z, answer == 0);
        flagsRegister.set(FlagsRegister.Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(FlagsRegister.Flag.P, ((short) hlValue < 0) != ((short) answer < 0));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, (result & FULL_CARRY_BIT) != 0);
        flagsRegister.setUndocumentedFlagsFromValue(answer >> 8);

        hlReg.set(answer);
        return 15;
    }

    @Override
    public String toString() {
        return "adc hl, " + sourceReg.name();
    }
}
