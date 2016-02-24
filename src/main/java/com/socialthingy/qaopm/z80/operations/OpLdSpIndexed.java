package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

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
