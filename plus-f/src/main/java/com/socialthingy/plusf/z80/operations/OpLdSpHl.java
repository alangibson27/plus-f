package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdSpHl extends Operation {
    private final Register spReg;
    private final Register hlReg;

    public OpLdSpHl(final Processor processor, final Clock clock) {
        super(clock);
        this.spReg = processor.register("sp");
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute() {
        spReg.set(hlReg.get());
        clock.tick(2);
    }

    @Override
    public String toString() {
        return "ld sp, hl";
    }
}
