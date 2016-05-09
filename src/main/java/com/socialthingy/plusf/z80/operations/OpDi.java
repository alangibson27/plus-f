package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpDi implements Operation {
    private final Processor processor;

    public OpDi(final Processor processor) {
        this.processor = processor;
    }

    @Override
    public int execute() {
        processor.setIff(0, false);
        processor.setIff(1, false);
        return 4;
    }
}
