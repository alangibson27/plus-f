package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpLdi extends BlockOperation {
    public OpLdi(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock, memory, 1);
    }

    @Override
    public void execute() {
        blockTransfer();
        clock.tick(8);
    }

    @Override
    public String toString() {
        return "ldi";
    }
}
