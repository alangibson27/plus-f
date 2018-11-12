package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpLd8RegFrom16RegIndirect extends Operation {
    private final Register dest;
    private final Register sourceReference;
    private final Memory memory;

    public OpLd8RegFrom16RegIndirect(final Clock clock, final Memory memory, final Register dest, final Register sourceReference) {
        super(clock);
        this.memory = memory;
        this.dest = dest;
        this.sourceReference = sourceReference;
    }

    @Override
    public void execute() {
        dest.set(memory.get(sourceReference.get()));
        clock.tick(3);
    }

    @Override
    public String toString() {
        return String.format("ld %s, (%s)", dest.name(), sourceReference.name());
    }
}
