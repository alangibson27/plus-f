package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdIndexedIndirect8Reg extends Operation {
    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;
    private final Register source;

    public OpLdIndexedIndirect8Reg(final Processor processor, final Memory memory, final Register indexRegister, final Register source) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.source = source;
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
        memory.set(addr, source.get());
    }

    @Override
    public String toString() {
        return String.format("ld (%s + n), %s", indexRegister.name(), source.name());
    }
}
