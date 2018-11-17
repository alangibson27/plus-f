package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpBitIndexedIndirect extends BitOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;
    private final Processor processor;

    public OpBitIndexedIndirect(final Processor processor, final Memory memory, final IndexRegister indexRegister, final int bitPosition) {
        super(processor, bitPosition);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int offset = processor.fetchRelative(-2);
        final int addr = indexRegister.withOffset(offset);
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 3, 3);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(initialPcValue + 3, 1);
        contentionModel.applyContention(addr, 3);
        contentionModel.applyContention(addr, 1);
        checkBit(memory.get(addr));
        flagsRegister.setUndocumentedFlagsFromValue((indexRegister.getHigh() + offset) & 0xff);
    }

    @Override
    public String toString() {
        return "bit (" + indexRegister.name() + ")";
    }
}
