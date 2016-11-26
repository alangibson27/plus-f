package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpXorIndexedIndirect extends XorOperation {
    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpXorIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        xor(unsafe.getInt(memory, 16L + ((indexRegister.withOffset(processor.fetchNextByte())) * 4)));
        return 19;
    }

    @Override
    public String toString() {
        return "xor (" + indexRegister.name() + " + n)";
    }
}
