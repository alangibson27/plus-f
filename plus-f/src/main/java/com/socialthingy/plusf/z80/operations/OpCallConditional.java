package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpCallConditional extends CallOperation {
    private final FlagsRegister flagsRegister;
    private final FlagsRegister.Flag flag;
    private final boolean callState;
    private final String toString;

    public OpCallConditional(final Processor processor, final FlagsRegister.Flag flag, final boolean callState) {
        super(processor);
        this.flagsRegister = processor.flagsRegister();
        this.flag = flag;
        this.callState = callState;

        if (callState) {
            this.toString = "call " + flag.name().toLowerCase() + ", nn";
        } else {
            this.toString = "call n" + flag.name().toLowerCase() + ", nn";
        }
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = processor.fetchNextWord();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        if (flagsRegister.get(flag) == callState) {
            contentionModel.applyContention(initialPcValue + 2, 1);
            final int sp = processor.register("sp").get();
            contentionModel.applyContention(sp - 1, 3);
            contentionModel.applyContention(sp - 2, 3);
            call(address);
        }
    }

    @Override
    public String toString() {
        return toString;
    }
}
