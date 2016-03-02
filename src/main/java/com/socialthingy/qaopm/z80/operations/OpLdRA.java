package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLdRA implements Operation {
    private final Register rReg;
    private final Register aReg;

    public OpLdRA(final Processor processor) {
        this.rReg = processor.register("r");
        this.aReg = processor.register("a");
    }

    @Override
    public int execute() {
        rReg.set(aReg.get());
        return 9;
    }
}
