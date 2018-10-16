package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpLd8RegFrom16RegIndirect implements Operation {

    private final Register dest;
    private final Register sourceReference;
    private final Memory memory;

    public OpLd8RegFrom16RegIndirect(final Memory memory, final Register dest, final Register sourceReference) {
        this.memory = memory;
        this.dest = dest;
        this.sourceReference = sourceReference;
    }

    @Override
    public int execute() {
        dest.set(memory.get(sourceReference.get()));
        return 7;
    }

    @Override
    public String toString() {
        return String.format("ld %s, (%s)", dest.name(), sourceReference.name());
    }
}
