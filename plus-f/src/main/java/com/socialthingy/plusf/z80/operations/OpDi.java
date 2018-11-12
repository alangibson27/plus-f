package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpDi extends Operation {
    private final Processor processor;

    public OpDi(final Processor processor, final Clock clock) {
        super(clock);
        this.processor = processor;
    }

    @Override
    public void execute() {
        processor.setIff(0, false);
        processor.setIff(1, false);
    }

    @Override
    public String toString() {
        return "di";
    }
}
