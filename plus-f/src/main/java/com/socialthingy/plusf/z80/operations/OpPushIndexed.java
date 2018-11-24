package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpPushIndexed extends Operation {
    private final Processor processor;
    private final IndexRegister indexRegister;

    public OpPushIndexed(final Processor processor, final IndexRegister indexRegister) {
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(irValue, 1);
        final int sp = processor.register("sp").get();
        contentionModel.applyContention(sp - 1, 3);
        contentionModel.applyContention(sp - 2, 3);
        final int value = indexRegister.get();
        processor.pushByte((value & 0xff00) >> 8);
        processor.pushByte(value & 0x00ff);
    }

    @Override
    public String toString() {
        return "push " + indexRegister.name();
    }
}
