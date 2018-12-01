package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLdAddressA extends Operation {

    private final Processor processor;
    private final Memory memory;
    private final Register aReg;

    public OpLdAddressA(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int destination = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(destination, 3);
        memory.set( destination, aReg.get());
    }

    @Override
    public String toString() {
        return "ld (nn), a";
    }
}
