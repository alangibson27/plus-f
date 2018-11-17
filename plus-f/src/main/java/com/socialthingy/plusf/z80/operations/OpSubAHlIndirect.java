package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpSubAHlIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpSubAHlIndirect(final Processor processor, final Memory memory, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }


    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(address, 3);
        accumulator.set(sub(memory.get(address), true));
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
    }

    @Override
    public String toString() {
        return useCarryFlag ? "sbc a, (hl)" : "sub (hl)";
    }
}
