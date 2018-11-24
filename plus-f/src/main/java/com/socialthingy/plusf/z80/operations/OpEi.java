package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpEi extends Operation {
    private final Processor processor;

    public OpEi(final Processor processor) {
        this.processor = processor;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        processor.enableInterrupts();
    }

    @Override
    public String toString() {
        return "ei";
    }
}
