package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpResIndexedIndirect extends BitModificationOperation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpResIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final IndexRegister indexRegister, final int bitPosition) {
        super(clock, bitPosition);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        final int offset = processor.fetchRelative(-2);
        final int address = indexRegister.withOffset(offset);
        memory.set( address, reset(memory.get(address)));
        clock.tick(11);
    }

    @Override
    public String toString() {
        return "res (" + indexRegister.name() + ")";
    }
}
