package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpRetn extends RetOperation {
    public OpRetn(final Processor processor, final Clock clock) {
        super(processor, clock);
    }

    @Override
    public void execute() {
        processor.setIff(0, processor.getIff(1));
        ret();
    }

    @Override
    public String toString() {
        return "retn";
    }
}
