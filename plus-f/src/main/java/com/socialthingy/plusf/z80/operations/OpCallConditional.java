package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpCallConditional extends CallOperation {
    private final FlagsRegister flagsRegister;
    private final FlagsRegister.Flag flag;
    private final boolean callState;
    private final String toString;

    public OpCallConditional(final Processor processor, final Clock clock, final FlagsRegister.Flag flag, final boolean callState) {
        super(processor, clock);
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
    public void execute() {
        final int address = processor.fetchNextWord();
        if (flagsRegister.get(flag) == callState) {
            call(address);
            clock.tick(13);
        } else {
            clock.tick(6);
        }
    }

    @Override
    public String toString() {
        return toString;
    }
}
