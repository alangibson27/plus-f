package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpRetn extends RetOperation {
    public OpRetn(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        processor.setIff(0, processor.getIff(1));
        ret();
        return 14;
    }

    @Override
    public String toString() {
        return "retn";
    }
}
