package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpRst extends CallOperation {
    private final int address;

    public OpRst(final Processor processor, final int address) {
        super(processor);
        this.address = address;
    }

    @Override
    public int execute() {
        call(address);
        return 11;
    }
}
