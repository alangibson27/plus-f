package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
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
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(irValue, 1);
        if (flagsRegister.get(flag) == retState) {
            final int sp = processor.register("sp").get();
            contentionModel.applyContention(sp, 3);
            contentionModel.applyContention(sp + 1, 3);
            ret();
        }
    }

    @Override
    public String toString() {
        return toString;
    }
}
