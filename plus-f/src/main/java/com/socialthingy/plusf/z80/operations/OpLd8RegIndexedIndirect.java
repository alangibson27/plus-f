package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLd8RegIndexedIndirect extends Operation {
    private final Processor processor;
    private final Memory memory;
    private final Register dest;
    private final IndexRegister indexRegister;

    public OpLd8RegIndexedIndirect(final Processor processor, final Memory memory, final Register dest, final Register indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
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
        dest.set(memory.get(addr));
    }

    @Override
    public String toString() {
        return String.format("ld %s, (%s + n)", dest.name(), indexRegister.name());
    }
}
