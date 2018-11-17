package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpPopIndexed extends Operation {
    private final Processor processor;
    private final IndexRegister indexRegister;

    public OpPopIndexed(final Processor processor, final IndexRegister indexRegister) {
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int sp = processor.register("sp").get();
        contentionModel.applyContention(sp, 3);
        contentionModel.applyContention(sp + 1, 3);
        indexRegister.set(Word.from(processor.popByte(), processor.popByte()));
    }

    @Override
    public String toString() {
        return "pop " + indexRegister.name();
    }
}
