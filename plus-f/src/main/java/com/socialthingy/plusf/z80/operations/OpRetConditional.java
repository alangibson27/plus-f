package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpRetConditional extends RetOperation {
    private final FlagsRegister flagsRegister;
    private final FlagsRegister.Flag flag;
    private final boolean retState;
    private final String toString;

    public OpRetConditional(final Processor processor, final FlagsRegister.Flag flag, final boolean retState) {
        super(processor);
        this.flagsRegister = processor.flagsRegister();
        this.flag = flag;
        this.retState = retState;

        if (retState) {
            this.toString = "ret " + flag.name().toLowerCase();
        } else {
            this.toString = "ret n" + flag.name().toLowerCase();
        }
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

    @Override
    public String toString() {
        return toString;
    }
}
