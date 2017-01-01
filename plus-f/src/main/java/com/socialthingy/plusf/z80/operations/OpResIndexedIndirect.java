package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpResIndexedIndirect extends BitModificationOperation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpResIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister, final int bitPosition) {
        super(bitPosition);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int offset = processor.fetchRelative(-2);
        final int address = indexRegister.withOffset(offset);
        Memory.set(memory, address, reset(unsafe.getInt(memory, BASE + ((address) * SCALE))));
        return 23;
    }

    @Override
    public String toString() {
        return "res (" + indexRegister.name() + ")";
    }
}
