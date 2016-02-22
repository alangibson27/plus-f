package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpAndAImmediate extends AndOperation {

    private final Processor processor;

    public OpAndAImmediate(final Processor processor) {
        super(processor);
        this.processor = processor;
    }

    @Override
    public int execute() {
        and(processor.fetchNextPC());
        return 7;
    }
}
