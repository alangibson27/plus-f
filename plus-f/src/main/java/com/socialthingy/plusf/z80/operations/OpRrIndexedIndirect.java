package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpRrIndexedIndirect extends RotateOperation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpRrIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final IndexRegister indexRegister) {
        super(processor, clock);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        final int address = indexRegister.withOffset(processor.fetchRelative(-2));
        final int result = rrValue(memory.get(address));
        setSignZeroAndParity(result);
        memory.set( address, result);
        clock.tick(11);
    }
}
