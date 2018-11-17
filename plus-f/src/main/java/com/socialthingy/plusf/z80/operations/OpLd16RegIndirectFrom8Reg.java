package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLd16RegIndirectFrom8Reg extends Operation {
    private final Register destReference;
    private final Register source;
    private final Memory memory;

    public OpLd16RegIndirectFrom8Reg(final Memory memory, final Register destReference, final Register source) {
        this.memory = memory;
        this.destReference = destReference;
        this.source = source;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = source.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(address, 3);
        memory.set( destReference.get(), address);
    }

    @Override
    public String toString() {
        return String.format("ld (%s), %s", destReference.name(), source.name());
    }
}
