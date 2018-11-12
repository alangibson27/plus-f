package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpSraIndexedIndirect extends SraOperation {
    private final IndexRegister indexRegister;
    private final Memory memory;
    private final Processor processor;

    public OpSraIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final IndexRegister indexRegister) {
        super(processor, clock);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int address = indexRegister.withOffset(processor.fetchRelative(-2));
        clock.tick(1);
        final int result = shift(memory.get(address));
        clock.tick(1);
        memory.set(address, result);
    }
}
