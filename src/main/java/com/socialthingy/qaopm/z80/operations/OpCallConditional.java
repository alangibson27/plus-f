package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Processor;

public class OpCallConditional extends CallOperation {
    private final FlagsRegister flagsRegister;
    private final FlagsRegister.Flag flag;
    private final boolean callState;

    public OpCallConditional(final Processor processor, final FlagsRegister.Flag flag, final boolean callState) {
        super(processor);
        this.flagsRegister = processor.flagsRegister();
        this.flag = flag;
        this.callState = callState;
    }

    @Override
    public int execute() {
        final int address = processor.fetchNextWord();
        if (flagsRegister.get(flag) == callState) {
            call(address);
            return 5;
        } else {
            return 3;
        }
    }
}
