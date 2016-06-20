package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpCpImmediate extends ArithmeticOperation {
    public OpCpImmediate(final Processor processor) {
        super(processor, false);
    }

    @Override
    public int execute() {
        sub(processor.fetchNextByte());
        return 7;
    }

    @Override
    public String toString() {
        return "cp n";
    }
}
