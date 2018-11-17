package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpJpConditional extends Operation {
    private final Processor processor;
    private final FlagsRegister flags;
    private final Register pcReg;
    private final FlagsRegister.Flag flag;
    private final boolean whenSet;
    private final String toString;

    public OpJpConditional(final Processor processor, final FlagsRegister.Flag flag, final boolean whenSet) {
        this.pcReg = processor.register("pc");
        this.flags = FlagsRegister.class.cast(processor.register("f"));
        this.processor = processor;
        this.flag = flag;
        this.whenSet = whenSet;

        if (whenSet) {
            this.toString = "jp " + flag.name().toLowerCase() + ", nn";
        } else {
            this.toString = "jp n" + flag.name().toLowerCase() + ", nn";
        }
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        final int destination = processor.fetchNextWord();
        if (flags.get(flag) == whenSet) {
            pcReg.set(destination);
        }
    }

    @Override
    public String toString() {
        return toString;
    }
}
