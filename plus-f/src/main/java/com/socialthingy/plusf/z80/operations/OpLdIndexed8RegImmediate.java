package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdIndexed8RegImmediate extends Operation {

    private final Processor processor;
    private final Register dest;

    public OpLdIndexed8RegImmediate(final Processor processor, final Register dest) {
        this.processor = processor;
        this.dest = dest;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        dest.set(processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return String.format("ld %s, n", dest.name());
    }
}
