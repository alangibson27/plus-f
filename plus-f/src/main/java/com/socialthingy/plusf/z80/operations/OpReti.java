package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpReti extends RetOperation {
    public OpReti(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        ret();
        return 14;
    }

    @Override
    public String toString() {
        return "reti";
    }
}
