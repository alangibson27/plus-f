package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpDjnz extends Operation {
    private final Processor processor;
    private final Register pcReg;
    private final Register bReg;

    public OpDjnz(final Processor processor, final Clock clock) {
        super(clock);
        this.processor = processor;
        this.pcReg = processor.register("pc");
        this.bReg = processor.register("b");
    }

    @Override
    public void execute() {
        final byte offset = (byte) processor.fetchNextByte();
        final int bValue = bReg.set(bReg.get() - 1);
        if (bValue > 0) {
            pcReg.set(pcReg.get() + offset);
            clock.tick(9);
        } else {
            clock.tick(4);
        }
    }

    @Override
    public String toString() {
        return "djnz n";
    }
}
