package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLd8RegImmediate extends Operation {
    private final Processor processor;
    private final Register dest;

    public OpLd8RegImmediate(final Processor processor, final Clock clock, final Register dest) {
        super(clock);
        this.processor = processor;
        this.dest = dest;
    }

    @Override
    public void execute() {
        dest.set(processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return String.format("ld %s, n", dest.name());
    }
}
