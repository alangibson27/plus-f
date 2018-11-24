package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdIndexedIndirectImmediate extends Operation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpLdIndexedIndirectImmediate(final Processor processor, final Memory memory, final Register indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int offset = processor.fetchNextByte();
        final int value = processor.fetchNextByte();
        final int addr = indexRegister.withOffset(offset);
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 3, 3);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(addr, 3);

        memory.set(addr , value);
    }

    @Override
    public String toString() {
        return String.format("ld (%s + n), n", indexRegister.name());
    }
}
