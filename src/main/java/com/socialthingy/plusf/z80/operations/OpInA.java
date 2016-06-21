package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpInA implements Operation {
    private final Processor processor;
    private final IO io;
    private final Register accumulator;

    public OpInA(final Processor processor, final IO io) {
        this.processor = processor;
        this.io = io;
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        accumulator.set(io.read(processor.fetchNextByte(), accumulator.get()));
        return 11;
    }

    @Override
    public String toString() {
        return "in a, (n)";
    }
}
