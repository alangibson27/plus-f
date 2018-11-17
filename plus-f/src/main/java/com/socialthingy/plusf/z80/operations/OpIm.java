package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpIm extends Operation {
    private final Processor processor;
    private final int mode;

    public OpIm(final Processor processor, final int mode) {
        this.processor = processor;
        this.mode = mode;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        processor.setInterruptMode(mode);
    }

    @Override
    public String toString() {
        return "im " + mode;
    }
}
