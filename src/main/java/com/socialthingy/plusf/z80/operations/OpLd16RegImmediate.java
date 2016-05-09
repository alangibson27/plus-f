package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLd16RegImmediate implements Operation {
    private final Register destReg;
    private final Processor processor;

    public OpLd16RegImmediate(final Processor processor, final Register destReg) {
        this.processor = processor;
        this.destReg = destReg;
    }

    @Override
    public int execute() {
        final int value = Word.from(processor.fetchNextPC(), processor.fetchNextPC());
        destReg.set(value);
        return 10;
    }
}
