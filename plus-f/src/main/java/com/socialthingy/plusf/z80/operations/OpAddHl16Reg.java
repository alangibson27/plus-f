package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;

import static com.socialthingy.plusf.util.Bitwise.FULL_CARRY_BIT;
import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

public class OpAddHl16Reg extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register hlReg;
    private final Register sourceReg;

    public OpAddHl16Reg(final Processor processor, final Clock clock, final Register sourceReg) {
        super(clock);
        this.flagsRegister = processor.flagsRegister();
        this.hlReg = processor.register("hl");
        this.sourceReg = sourceReg;
    }

    @Override
    public void execute() {
        final int result = Bitwise.addWord(hlReg.get(), sourceReg.get());
        final int answer = result & 0xffff;
        hlReg.set(answer);
        flagsRegister.set(FlagsRegister.Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, (result & FULL_CARRY_BIT) != 0);
        flagsRegister.setUndocumentedFlagsFromValue(answer >> 8);
        clock.tick(7);
    }

    @Override
    public String toString() {
        return "add hl, " + sourceReg.name();
    }
}
