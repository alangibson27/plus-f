package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpEi implements Operation {
    private final Processor processor;

    public OpEi(final Processor processor) {
        this.processor = processor;
    }

    @Override
    public int execute() {
        processor.enableInterrupts();
        return 4;
    }
}
