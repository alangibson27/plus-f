package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLd8RegFrom16RegIndirect extends Operation {
    private final Register dest;
    private final Register sourceReference;
    private final Memory memory;

    public OpLd8RegFrom16RegIndirect(final Memory memory, final Register dest, final Register sourceReference) {
        this.memory = memory;
        this.dest = dest;
        this.sourceReference = sourceReference;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = sourceReference.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(address, 3);
        dest.set(memory.get(address));
    }

    @Override
    public String toString() {
        return String.format("ld %s, (%s)", dest.name(), sourceReference.name());
    }
}
