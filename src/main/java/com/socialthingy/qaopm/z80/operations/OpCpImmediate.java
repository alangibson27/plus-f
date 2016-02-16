package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpCpImmediate extends ArithmeticOperation {
    public OpCpImmediate(final Processor processor) {
        super(processor, false);
    }

    @Override
    public int execute() {
        sub(processor.fetchNextPC());
        return 7;
    }
}
