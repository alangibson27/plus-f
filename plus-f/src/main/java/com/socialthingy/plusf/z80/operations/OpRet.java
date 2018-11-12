package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpRet extends RetOperation {
    public OpRet(final Processor processor, final Clock clock) {
        super(processor, clock);
    }

    @Override
    public void execute() {
        ret();
    }

    @Override
    public String toString() {
        return "ret";
    }
}
