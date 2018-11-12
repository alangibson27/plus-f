package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOutC8Reg extends Operation {
    private final Register bReg;
    private final Register cReg;
    private final Register sourceRegister;
    private final IO io;

    public OpOutC8Reg(final Processor processor, final IO io, final Clock clock, final Register register) {
        super(clock);
        this.io = io;
        this.sourceRegister = register;
        this.bReg = processor.register("b");
        this.cReg = processor.register("c");
    }

    @Override
    public void execute() {
        io.write(cReg.get(), bReg.get(), sourceRegister.get());
    }

    @Override
    public String toString() {
        return "out (c), " + sourceRegister.name();
    }
}
