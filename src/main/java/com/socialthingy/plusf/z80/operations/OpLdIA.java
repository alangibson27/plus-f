package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdIA implements Operation {
    private final Register iReg;
    private final Register aReg;

    public OpLdIA(final Processor processor) {
        this.iReg = processor.register("i");
        this.aReg = processor.register("a");
    }

    @Override
    public int execute() {
        iReg.set(aReg.get());
        return 9;
    }

    @Override
    public String toString() {
        return "ld i, a";
    }
}
