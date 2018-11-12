package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpInA extends Operation {
    private final Processor processor;
    private final IO io;
    private final Register accumulator;

    public OpInA(final Processor processor, final Clock clock, final IO io) {
        super(clock);
        this.processor = processor;
        this.io = io;
        this.accumulator = processor.register("a");
    }

    @Override
    public void execute() {
        accumulator.set(io.read(processor.fetchNextByte(), accumulator.get()));
    }

    @Override
    public String toString() {
        return "in a, (n)";
    }
}
