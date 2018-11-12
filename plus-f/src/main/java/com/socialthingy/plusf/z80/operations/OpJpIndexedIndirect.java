package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJpIndexedIndirect extends Operation {
    private final Register pcReg;
    private final Register indexRegister;

    public OpJpIndexedIndirect(final Processor processor, final Clock clock, final Register indexRegister) {
        super(clock);
        this.pcReg = processor.register("pc");
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        pcReg.set(indexRegister.get());
    }

    @Override
    public String toString() {
        return "jp (" + indexRegister.name() + ")";
    }
}
