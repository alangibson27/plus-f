package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAddAHlIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpAddAHlIndirect(final Processor processor, final int[] memory, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }


    @Override
    public int execute() {
        add(memory[hlReg.get()]);
        return 7;
    }

    @Override
    public String toString() {
        return useCarryFlag ? "adc a, (hl)" : "add a, (hl)";
    }
}
