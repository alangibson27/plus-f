package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpJpHlIndirect extends Operation {

    private final Register hlReg;
    private final Register pcReg;

    public OpJpHlIndirect(final Processor processor) {
        this.hlReg = processor.register("hl");
        this.pcReg = processor.register("pc");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        pcReg.set(hlReg.get());
    }

    @Override
    public String toString() {
        return "jp (hl)";
    }
}
