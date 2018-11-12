package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpAndAImmediate extends AndOperation {

    private final Processor processor;

    public OpAndAImmediate(final Processor processor, final Clock clock) {
        super(processor, clock);
        this.processor = processor;
    }

    @Override
    public void execute() {
        and(processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return "and n";
    }
}
