package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdRA extends Operation {
    private final Register rReg;
    private final Register aReg;

    public OpLdRA(final Processor processor, final Clock clock) {
        super(clock);
        this.rReg = processor.register("r");
        this.aReg = processor.register("a");
    }

    @Override
    public void execute() {
        rReg.set(aReg.get());
        clock.tick(1);
    }

    @Override
    public String toString() {
        return "ld r, a";
    }
}
