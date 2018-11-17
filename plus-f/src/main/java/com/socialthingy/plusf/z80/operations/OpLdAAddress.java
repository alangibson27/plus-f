package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLdAAddress extends Operation {

    private final Processor processor;
    private final Register aReg;
    private final Memory memory;

    public OpLdAAddress(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(address, 3);
        aReg.set(memory.get(address));
    }

    @Override
    public String toString() {
        return "ld a, (nn)";
    }
}
