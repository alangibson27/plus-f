package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpRet extends RetOperation {
    public OpRet(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        ret();
        return 10;
    }
}
