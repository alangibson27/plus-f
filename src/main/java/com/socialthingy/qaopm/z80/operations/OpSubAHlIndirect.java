package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpSubAHlIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpSubAHlIndirect(final Processor processor, final int[] memory, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }


    @Override
    public int execute() {
        accumulator.set(sub(memory[hlReg.get()]));
        return 7;
    }
}
