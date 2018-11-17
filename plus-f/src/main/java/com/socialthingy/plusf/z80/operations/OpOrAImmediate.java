package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpOrAImmediate extends OrOperation {

    private final Processor processor;

    public OpOrAImmediate(final Processor processor) {
        super(processor);
        this.processor = processor;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        or(processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return "or n";
    }
}
