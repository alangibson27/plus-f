package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpOrAImmediate extends OrOperation {

    private final Processor processor;

    public OpOrAImmediate(final Processor processor) {
        super(processor);
        this.processor = processor;
    }

    @Override
    public int execute() {
        or(processor.fetchNextPC());
        return 7;
    }
}
