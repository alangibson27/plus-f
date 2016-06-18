package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLd8RegIndexedIndirect implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final Register dest;
    private final IndexRegister indexRegister;

    public OpLd8RegIndexedIndirect(final Processor processor, final int[] memory, final Register dest, final Register indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public int execute() {
        dest.set(memory[indexRegister.withOffset(processor.fetchNextPC())]);
        return 19;
    }

    @Override
    public String toString() {
        return String.format("ld %s, (%s + n)", dest.name(), indexRegister.name());
    }
}
