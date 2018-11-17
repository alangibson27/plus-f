package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;
import com.socialthingy.plusf.z80.FlagsRegister.Flag;

public class OpJrConditional extends Operation {
    private final Processor processor;
    private final Register pcReg;
    private final FlagsRegister flagsRegister;
    private final Flag flag;
    private final boolean whenSet;
    private final String toString;

    public OpJrConditional(final Processor processor, final Flag flag, final boolean whenSet) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
        this.flagsRegister = FlagsRegister.class.cast(processor.register("f"));
        this.flag = flag;
        this.whenSet = whenSet;

        if (whenSet) {
            this.toString = "jr " + flag.name().toLowerCase() + ", n";
        } else {
            this.toString = "jr n" + flag.name().toLowerCase() + ", n";
        }
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        final byte offset = (byte) processor.fetchNextByte();
        if (flagsRegister.get(flag) == whenSet) {
            contentionModel.applyContention(initialPcValue + 1, 1);
            contentionModel.applyContention(initialPcValue + 1, 1);
            contentionModel.applyContention(initialPcValue + 1, 1);
            contentionModel.applyContention(initialPcValue + 1, 1);
            contentionModel.applyContention(initialPcValue + 1, 1);
            pcReg.set(pcReg.get() + offset);
        }
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
