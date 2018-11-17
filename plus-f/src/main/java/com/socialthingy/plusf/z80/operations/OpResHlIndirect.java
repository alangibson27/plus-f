package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpResHlIndirect extends BitModificationOperation {

    private final Register hlReg;
    private final Memory memory;
    private final String toString;

    public OpResHlIndirect(final Processor processor, final Memory memory, final int bitPosition) {
        super(bitPosition);
        this.hlReg = processor.register("hl");
        this.memory = memory;
        this.toString = String.format("res %d, (hl)", bitPosition);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 3);
        final int result = reset(memory.get(address));
        memory.set(address, result);
    }

    @Override
    public String toString() {
        return toString;
    }
}
