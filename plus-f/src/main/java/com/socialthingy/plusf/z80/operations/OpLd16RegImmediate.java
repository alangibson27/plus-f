package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLd16RegImmediate extends Operation {
    private final Register destReg;
    private final Processor processor;

    public OpLd16RegImmediate(final Processor processor, final Register destReg) {
        this.processor = processor;
        this.destReg = destReg;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        final int value = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        destReg.set(value);
    }

    @Override
    public String toString() {
        return String.format("ld %s, nn", destReg.name());
    }
}
