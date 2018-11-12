package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdIA extends Operation {
    private final Register iReg;
    private final Register aReg;

    public OpLdIA(final Processor processor, final Clock clock) {
        super(clock);
        this.iReg = processor.register("i");
        this.aReg = processor.register("a");
    }

    @Override
    public void execute() {
        iReg.set(aReg.get());
        clock.tick(1);
    }

    @Override
    public String toString() {
        return "ld i, a";
    }
}
