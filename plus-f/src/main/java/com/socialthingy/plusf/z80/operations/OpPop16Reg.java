package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpPop16Reg extends Operation {
    private final Processor processor;
    private final Register register;

    public OpPop16Reg(final Processor processor, final Register register) {
        this.processor = processor;
        this.register = register;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        final int sp = processor.register("sp").get();
        contentionModel.applyContention(sp, 3);
        contentionModel.applyContention(sp + 1, 3);
        register.set(Word.from(processor.popByte(), processor.popByte()));
    }

    @Override
    public String toString() {
        return "pop " + register.name();
    }
}
