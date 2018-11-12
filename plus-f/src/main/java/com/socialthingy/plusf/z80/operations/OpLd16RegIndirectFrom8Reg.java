package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpLd16RegIndirectFrom8Reg extends Operation {
    private final Register destReference;
    private final Register source;
    private final Memory memory;

    public OpLd16RegIndirectFrom8Reg(final Clock clock, final Memory memory, final Register destReference, final Register source) {
        super(clock);
        this.memory = memory;
        this.destReference = destReference;
        this.source = source;
    }

    @Override
    public void execute() {
        memory.set( destReference.get(), source.get());
        clock.tick(3);
    }

    @Override
    public String toString() {
        return String.format("ld (%s), %s", destReference.name(), source.name());
    }
}
