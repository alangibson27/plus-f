package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpRst extends CallOperation {
    private final int address;

    public OpRst(final Processor processor, final Clock clock, final int address) {
        super(processor, clock);
        this.address = address;
    }

    @Override
    public void execute() {
        call(address);
        clock.tick(1);
    }

    @Override
    public String toString() {
        return String.format("rst %02x", address);
    }
}
