package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpBitIndexedIndirect extends BitOperation {

    private final int[] memory;
    private final IndexRegister indexRegister;
    private final Processor processor;

    public OpBitIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister, final int bitPosition) {
        super(processor, bitPosition);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int offset = processor.fetchRelative(-2);
        checkBit(unsafe.getInt(memory, BASE + (indexRegister.withOffset(offset) * SCALE)));
        flagsRegister.setUndocumentedFlagsFromValue((indexRegister.getHigh() + offset) & 0xff);
        return 20;
    }

    @Override
    public String toString() {
        return "bit (" + indexRegister.name() + ")";
    }
}
