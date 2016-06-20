package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpAndAImmediate extends AndOperation {

    private final Processor processor;

    public OpAndAImmediate(final Processor processor) {
        super(processor);
        this.processor = processor;
    }

    @Override
    public int execute() {
        and(processor.fetchNextByte());
        return 7;
    }

    @Override
    public String toString() {
        return "and n";
    }
}
