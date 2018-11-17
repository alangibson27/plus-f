package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdSpIndexed extends Operation {
    private final Register spReg;
    private final Register indexRegister;

    public OpLdSpIndexed(final Processor processor, final IndexRegister indexRegister) {
        this.spReg = processor.register("sp");
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        spReg.set(indexRegister.get());
    }

    @Override
    public String toString() {
        return String.format("ld sp, %s", indexRegister.name());
    }
}
