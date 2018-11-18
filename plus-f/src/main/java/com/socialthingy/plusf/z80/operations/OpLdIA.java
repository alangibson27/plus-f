package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdIA extends Operation {
    private final Register iReg;
    private final Register aReg;

    public OpLdIA(final Processor processor) {
        this.iReg = processor.register("i");
        this.aReg = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(irValue, 1);
        iReg.set(aReg.get());
    }

    @Override
    public String toString() {
        return "ld i, a";
    }
}
