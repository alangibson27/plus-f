package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLdIndexedImmediate extends Operation {
    private final Processor processor;
    private final Register indexRegister;

    public OpLdIndexedImmediate(final Processor processor, final IndexRegister indexRegister) {
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 3, 3);
        final int value = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        indexRegister.set(value);
    }

    @Override
    public String toString() {
        return String.format("ld %s, nn", indexRegister.name());
    }
}
