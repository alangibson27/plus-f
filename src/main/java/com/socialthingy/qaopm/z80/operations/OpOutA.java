package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IO;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpOutA implements Operation {
    private final Processor processor;
    private final IO io;
    private final Register accumulator;

    public OpOutA(final Processor processor, final IO io) {
        this.processor = processor;
        this.io = io;
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        io.write(processor.fetchNextPC(), accumulator.get(), accumulator.get());
        return 11;
    }
}
