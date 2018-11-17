package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;

import static com.socialthingy.plusf.util.Bitwise.FULL_CARRY_BIT;
import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

public class OpAddIndexedReg extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register indexRegister;
    private final Register sourceReg;

    public OpAddIndexedReg(final Processor processor, final Register indexRegister, final Register sourceReg) {
        this.flagsRegister = processor.flagsRegister();
        this.indexRegister = indexRegister;
        this.sourceReg = sourceReg;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);

        final int result = Bitwise.addWord(indexRegister.get(), sourceReg.get());
        final int answer = result & 0xffff;
        indexRegister.set(answer);
        flagsRegister.set(FlagsRegister.Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, (result & FULL_CARRY_BIT) != 0);
        flagsRegister.setUndocumentedFlagsFromValue(answer >> 8);
    }

    @Override
    public String toString() {
        return "add " + indexRegister.name() + ", " + sourceReg.name();
    }
}
