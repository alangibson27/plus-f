package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpBitIndexedIndirect extends BitOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;
    private final Processor processor;

    public OpBitIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final IndexRegister indexRegister, final int bitPosition) {
        super(processor, clock, bitPosition);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int offset = processor.fetchRelative(-2);
        checkBit(memory.get(indexRegister.withOffset(offset)));
        flagsRegister.setUndocumentedFlagsFromValue((indexRegister.getHigh() + offset) & 0xff);
        clock.tick(8);
    }

    @Override
    public String toString() {
        return "bit (" + indexRegister.name() + ")";
    }
}
