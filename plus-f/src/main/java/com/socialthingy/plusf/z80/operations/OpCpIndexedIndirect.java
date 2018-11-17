package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpCpIndexedIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpCpIndexedIndirect(final Processor processor, final Memory memory, final Register indexRegister) {
        super(processor, false);
        this.memory = memory;
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
        sub(memory.get(addr), false);
    }

    @Override
    public String toString() {
        return "cp (" + indexRegister.name() + " + n)";
    }
}
