package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOutCZero extends Operation {
    private final IO io;
    private final Register cReg;
    private final Register bReg;

    public OpOutCZero(final Processor processor, final Clock clock, final IO io) {
        super(clock);
        this.io = io;
        this.cReg = processor.register("c");
        this.bReg = processor.register("b");
    }

    @Override
    public void execute() {
        io.write(cReg.get(), bReg.get(), 0);
    }

    @Override
    public String toString() {
        return "out (c), 0";
    }
}
