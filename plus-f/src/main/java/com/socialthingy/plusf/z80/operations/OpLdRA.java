package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdRA extends Operation {
    private final Register rReg;
    private final Register aReg;

    public OpLdRA(final Processor processor) {
        this.rReg = processor.register("r");
        this.aReg = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(irValue, 1);
        rReg.set(aReg.get());
    }

    @Override
    public String toString() {
        return "ld r, a";
    }
}
