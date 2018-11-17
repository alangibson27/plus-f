package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpJp extends Operation {
    private final Processor processor;
    private final Register pcReg;

    public OpJp(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        pcReg.set(processor.fetchNextWord());
    }

    @Override
    public String toString() {
        return "jp nn";
    }
}
