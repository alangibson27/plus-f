package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Processor;

public class OpRetConditional extends RetOperation {
    private final FlagsRegister flagsRegister;
    private final FlagsRegister.Flag flag;
    private final boolean retState;

    public OpRetConditional(final Processor processor, final FlagsRegister.Flag flag, final boolean retState) {
        super(processor);
        this.flagsRegister = processor.flagsRegister();
        this.flag = flag;
        this.retState = retState;
    }

    @Override
    public int execute() {
        if (flagsRegister.get(flag) == retState) {
            ret();
            return 11;
        } else {
            return 5;
        }
    }
}
