package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpOrAImmediate extends OrOperation {

    private final Processor processor;

    public OpOrAImmediate(final Processor processor, final Clock clock) {
        super(processor, clock);
        this.processor = processor;
    }

    @Override
    public void execute() {
        or(processor.fetchNextByte());
        clock.tick(3);
    }

    @Override
    public String toString() {
        return "or n";
    }
}
