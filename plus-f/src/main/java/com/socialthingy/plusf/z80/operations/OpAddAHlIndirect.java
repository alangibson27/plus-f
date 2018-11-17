package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpAddAHlIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpAddAHlIndirect(final Processor processor, final Memory memory, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }


    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(address, 3);
        add(memory.get(address));
    }

    @Override
    public String toString() {
        return useCarryFlag ? "adc a, (hl)" : "add a, (hl)";
    }
}
