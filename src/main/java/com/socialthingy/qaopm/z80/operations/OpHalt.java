package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class OpHalt implements Operation {
    private final Processor processor;

    public OpHalt(final Processor processor) {
        this.processor = processor;
    }

    @Override
    public int execute() {
        processor.halt();
        return 4;
    }
}