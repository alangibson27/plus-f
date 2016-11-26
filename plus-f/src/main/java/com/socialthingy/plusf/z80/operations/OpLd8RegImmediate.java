package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLd8RegImmediate implements Operation {

    private final Processor processor;
    private final Register dest;

    public OpLd8RegImmediate(final Processor processor, final Register dest) {
        this.processor = processor;
        this.dest = dest;
    }

    @Override
    public int execute() {
        dest.set(processor.fetchNextByte());
        return 7;
    }

    @Override
    public String toString() {
        return String.format("ld %s, n", dest.name());
    }
}
