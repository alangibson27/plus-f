package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpCpir extends BlockOperation {
    public OpCpir(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock, memory, 1);
    }

    @Override
    public void execute() {
        if (blockCompare() == 0) {
            clock.tick(8);
        } else {
            clock.tick(adjustPC());
        }
    }

    @Override
    public String toString() {
        return "cpir";
    }
}
