package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOrIndexedIndirect extends OrOperation {
    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpOrIndexedIndirect(final Processor processor, final Memory memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int addr = indexRegister.withOffset(processor.fetchNextByte());
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(addr, 3);
        or(memory.get(addr));
    }

    @Override
    public String toString() {
        return "or (" + indexRegister.name() + " + n)";
    }
}
