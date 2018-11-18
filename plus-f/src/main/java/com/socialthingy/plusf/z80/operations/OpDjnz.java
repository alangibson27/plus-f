package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpDjnz extends Operation {
    private final Processor processor;
    private final Register pcReg;
    private final Register bReg;

    public OpDjnz(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
        this.bReg = processor.register("b");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(initialPcValue + 1, 3);
        final byte offset = (byte) processor.fetchNextByte();
        final int bValue = bReg.set(bReg.get() - 1);
        if (bValue > 0) {
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
        return "djnz n";
    }
}
