package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpPush16Reg extends Operation {
    private final BytePairRegister register;
    private final Processor processor;

    public OpPush16Reg(final Processor processor, final Register register) {
        this.processor = processor;
        this.register = BytePairRegister.class.cast(register);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(irValue, 1);
        final int sp = processor.register("sp").get();
        contentionModel.applyContention(sp - 1, 3);
        contentionModel.applyContention(sp - 2, 3);
        processor.pushByte(register.getHigh());
        processor.pushByte(register.getLow());
    }

    @Override
    public String toString() {
        return "push " + register.name();
    }
}
