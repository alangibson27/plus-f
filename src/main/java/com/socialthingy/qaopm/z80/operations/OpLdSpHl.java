package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLdSpHl implements Operation {
    private final Register spReg;
    private final Register hlReg;

    public OpLdSpHl(final Processor processor) {
        this.spReg = processor.register("sp");
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        spReg.set(hlReg.get());
        return 6;
    }
}
