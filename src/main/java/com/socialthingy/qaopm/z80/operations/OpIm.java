package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class OpIm implements Operation {
    private final Processor processor;
    private final int mode;

    public OpIm(final Processor processor, final int mode) {
        this.processor = processor;
        this.mode = mode;
    }

    @Override
    public int execute() {
        processor.setInterruptMode(mode);
        return 8;
    }
}
