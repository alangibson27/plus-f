package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdSpIndexed extends Operation {
    private final Register spReg;
    private final Register indexRegister;

    public OpLdSpIndexed(final Processor processor, final Clock clock, final IndexRegister indexRegister) {
        super(clock);
        this.spReg = processor.register("sp");
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        spReg.set(indexRegister.get());
        clock.tick(2);
    }

    @Override
    public String toString() {
        return String.format("ld sp, %s", indexRegister.name());
    }
}
