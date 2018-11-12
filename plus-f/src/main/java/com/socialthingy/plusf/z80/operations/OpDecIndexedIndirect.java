package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpDecIndexedIndirect extends DecOperation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpDecIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final IndexRegister indexRegister) {
        super(processor, clock);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        final int address = indexRegister.withOffset(processor.fetchNextByte());
        final int result = decrement(memory.get(address));
        memory.set( address, result);
        clock.tick(15);
    }

    @Override
    public String toString() {
        return "dec (" + indexRegister.name() + " + n)";
    }
}
