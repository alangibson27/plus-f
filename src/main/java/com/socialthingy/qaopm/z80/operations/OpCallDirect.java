package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpCallDirect extends CallOperation {
    private final int address;

    public OpCallDirect(final Processor processor, final int address) {
        super(processor);
        this.address = address;
    }

    @Override
    public int execute() {
        call(address);
        return 17;
    }
}
