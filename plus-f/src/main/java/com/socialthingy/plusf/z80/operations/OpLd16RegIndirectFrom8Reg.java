package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpLd16RegIndirectFrom8Reg implements Operation {

    private final Register destReference;
    private final Register source;
    private final Memory memory;

    public OpLd16RegIndirectFrom8Reg(final Memory memory, final Register destReference, final Register source) {
        this.memory = memory;
        this.destReference = destReference;
        this.source = source;
    }

    @Override
    public int execute() {
        memory.set( destReference.get(), source.get());
        return 7;
    }

    @Override
    public String toString() {
        return String.format("ld (%s), %s", destReference.name(), source.name());
    }
}
