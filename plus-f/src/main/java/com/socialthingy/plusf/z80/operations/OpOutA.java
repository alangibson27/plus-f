package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOutA extends Operation {
    private final Processor processor;
    private final IO io;
    private final Register accumulator;

    public OpOutA(final Processor processor, final IO io) {
        this.processor = processor;
        this.io = io;
        this.accumulator = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        final int lowByte = processor.fetchNextByte();
        final int highByte = accumulator.get();
        contentionModel.applyIOContention(lowByte, highByte);
        io.write(lowByte, highByte, highByte);
    }

    @Override
    public String toString() {
        return "out (n), a";
    }
}
