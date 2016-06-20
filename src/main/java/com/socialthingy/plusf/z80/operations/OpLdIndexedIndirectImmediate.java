package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdIndexedIndirectImmediate implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpLdIndexedIndirectImmediate(final Processor processor, final int[] memory, final Register indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public int execute() {
        final int offset = processor.fetchNextByte();
        final int value = processor.fetchNextByte();
        Memory.set(memory, indexRegister.withOffset(offset), value);
        return 19;
    }

    @Override
    public String toString() {
        return String.format("ld (%s), n", indexRegister.name());
    }
}
