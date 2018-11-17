package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpJpIndexedIndirect extends Operation {
    private final Register pcReg;
    private final Register indexRegister;

    public OpJpIndexedIndirect(final Processor processor, final Register indexRegister) {
        this.pcReg = processor.register("pc");
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        pcReg.set(indexRegister.get());
    }

    @Override
    public String toString() {
        return "jp (" + indexRegister.name() + ")";
    }
}
