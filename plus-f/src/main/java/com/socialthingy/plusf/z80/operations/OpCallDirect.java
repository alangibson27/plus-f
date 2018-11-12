package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpCallDirect extends CallOperation {
    private final int address;

    public OpCallDirect(final Processor processor, final Clock clock, final int address) {
        super(processor, clock);
        this.address = address;
    }

    @Override
    public void execute() {
        call(address);
        clock.tick(9);
    }
}
