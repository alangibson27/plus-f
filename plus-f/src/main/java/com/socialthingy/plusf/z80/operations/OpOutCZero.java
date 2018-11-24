package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOutCZero extends Operation {
    private final IO io;
    private final Register cReg;
    private final Register bReg;

    public OpOutCZero(final Processor processor, final IO io) {
        this.io = io;
        this.cReg = processor.register("c");
        this.bReg = processor.register("b");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int lowByte = cReg.get();
        final int highByte = bReg.get();
        contentionModel.applyIOContention(lowByte, highByte);
        io.write(lowByte, highByte, 0);
    }

    @Override
    public String toString() {
        return "out (c), 0";
    }
}
