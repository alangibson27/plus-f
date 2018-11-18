package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpIncIndexedIndirect extends IncOperation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpIncIndexedIndirect(final Processor processor, final Memory memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = indexRegister.withOffset(processor.fetchNextByte());
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(initialPcValue + 2, 1);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 3);
        final int result = increment(memory.get(address));
        memory.set(address, result);
    }

    @Override
    public String toString() {
        return "inc (" + indexRegister.name() + " + n)";
    }
}
