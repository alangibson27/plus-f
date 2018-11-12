package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOutA extends Operation {
    private final Processor processor;
    private final IO io;
    private final Register accumulator;

    public OpOutA(final Processor processor, final IO io, final Clock clock) {
        super(clock);
        this.processor = processor;
        this.io = io;
        this.accumulator = processor.register("a");
    }

    @Override
    public void execute() {
        io.write(processor.fetchNextByte(), accumulator.get(), accumulator.get());
        clock.tick(4);
    }

    @Override
    public String toString() {
        return "out (n), a";
    }
}
