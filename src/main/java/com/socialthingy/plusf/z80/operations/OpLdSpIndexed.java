package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdSpIndexed implements Operation {
    private final Register spReg;
    private final Register indexRegister;

    public OpLdSpIndexed(final Processor processor, final IndexRegister indexRegister) {
        this.spReg = processor.register("sp");
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        spReg.set(indexRegister.get());
        return 10;
    }
}
