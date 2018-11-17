package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdSpHl extends Operation {
    private final Register spReg;
    private final Register hlReg;

    public OpLdSpHl(final Processor processor) {
        this.spReg = processor.register("sp");
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        spReg.set(hlReg.get());
    }

    @Override
    public String toString() {
        return "ld sp, hl";
    }
}
