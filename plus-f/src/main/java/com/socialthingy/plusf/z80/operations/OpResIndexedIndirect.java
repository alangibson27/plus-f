package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpResIndexedIndirect extends BitModificationOperation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpResIndexedIndirect(final Processor processor, final Memory memory, final IndexRegister indexRegister, final int bitPosition) {
        super(bitPosition);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int offset = processor.fetchRelative(-2);
        final int address = indexRegister.withOffset(offset);
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 3, 3);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 3);
        final int result = reset(memory.get(address));
        memory.set(address, result);
    }

    @Override
    public String toString() {
        return "res (" + indexRegister.name() + ")";
    }
}
