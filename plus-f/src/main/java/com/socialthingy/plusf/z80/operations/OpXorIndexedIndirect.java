package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpXorIndexedIndirect extends XorOperation {
    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpXorIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final IndexRegister indexRegister) {
        super(processor, clock);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public void execute() {
        xor(memory.get(indexRegister.withOffset(processor.fetchNextByte())));
        clock.tick(11);
    }

    @Override
    public String toString() {
        return "xor (" + indexRegister.name() + " + n)";
    }
}
