package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpSraIndexedIndirect extends SraOperation {
    private final IndexRegister indexRegister;
    private final Memory memory;
    private final Processor processor;

    public OpSraIndexedIndirect(final Processor processor, final Memory memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = indexRegister.withOffset(processor.fetchRelative(-2));
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 3, 3);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 3);
        final int result = shift(memory.get(address));
        memory.set(address, result);
    }
}
