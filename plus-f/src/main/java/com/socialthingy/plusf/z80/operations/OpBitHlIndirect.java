package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpBitHlIndirect extends BitOperation {

    private final Memory memory;
    private final Register hlReg;
    private final String toString;

    public OpBitHlIndirect(final Processor processor, final Memory memory, final int bitPosition) {
        super(processor, bitPosition);
        this.memory = memory;
        this.hlReg = processor.register("hl");

        this.toString = String.format("bit %d, (hl)", bitPosition);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int addr = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(addr, 3);
        contentionModel.applyContention(addr, 1);
        checkBit(memory.get(addr));
    }

    @Override
    public String toString() {
        return toString;
    }
}
