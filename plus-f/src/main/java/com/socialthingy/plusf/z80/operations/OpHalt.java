package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpHalt extends Operation {
    private final Processor processor;

    public OpHalt(final Processor processor, final Clock clock) {
        super(clock);
        this.processor = processor;
    }

    @Override
    public void execute() {
        processor.halt();
    }

    @Override
    public String toString() {
        return "halt";
    }
}
