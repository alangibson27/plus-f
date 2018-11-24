package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpCallDirect extends CallOperation {
    private final int address;

    public OpCallDirect(final Processor processor, final int address) {
        super(processor);
        this.address = address;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        call(address);
    }
}
