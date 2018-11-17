package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpRst extends CallOperation {
    private final int address;

    public OpRst(final Processor processor, final int address) {
        super(processor);
        this.address = address;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(irValue, 1);
        final int sp = processor.register("sp").get();
        contentionModel.applyContention(sp, 3);
        contentionModel.applyContention(sp + 1, 3);
        call(address);
    }

    @Override
    public String toString() {
        return String.format("rst %02x", address);
    }
}
