package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;

import static com.socialthingy.plusf.util.Bitwise.FULL_CARRY_BIT;
import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

public class OpAddHl16Reg implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register mysteryReg;
    private final BytePairRegister hlReg;
    private final Register sourceReg;

    public OpAddHl16Reg(final Processor processor, final Register sourceReg) {
        this.flagsRegister = processor.flagsRegister();
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
        this.mysteryReg = processor.register("?");
        this.sourceReg = sourceReg;
    }

    @Override
    public int execute() {
        final int result = Bitwise.addWord(hlReg.get(), sourceReg.get());
        final int answer = result & 0xffff;
        mysteryReg.set(hlReg.highReg().get());
        hlReg.set(answer);
        flagsRegister.set(FlagsRegister.Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, (result & FULL_CARRY_BIT) != 0);
        flagsRegister.setUndocumentedFlagsFromValue(answer >> 8);
        return 11;
    }

    @Override
    public String toString() {
        return "add hl, " + sourceReg.name();
    }
}
