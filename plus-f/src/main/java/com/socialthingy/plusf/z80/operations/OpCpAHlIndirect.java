package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpCpAHlIndirect extends ArithmeticOperation {
    private final Memory memory;
    private final Register hlReg;

    public OpCpAHlIndirect(final Processor processor, final Memory memory) {
        super(processor, false);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(address, 3);
        sub(memory.get(address), false);
    }

    @Override
    public String toString() {
        return "cp (hl)";
    }
}

