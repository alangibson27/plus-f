package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpLdd extends BlockOperation {
    public OpLdd(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock, memory, -1);
    }

    @Override
    public void execute() {
        blockTransfer();
        clock.tick(2);
    }

    @Override
    public String toString() {
        return "ldd";
    }
}
