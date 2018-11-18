package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpJr extends Operation {
    private final Processor processor;
    private final Register pcReg;

    public OpJr(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 1, 1);
        contentionModel.applyContention(initialPcValue + 1, 1);
        contentionModel.applyContention(initialPcValue + 1, 1);
        contentionModel.applyContention(initialPcValue + 1, 1);
        contentionModel.applyContention(initialPcValue + 1, 1);
        final byte offset = (byte) processor.fetchNextByte();
        pcReg.set(pcReg.get() + offset);
    }

    @Override
    public String toString() {
        return "jr n";
    }
}
