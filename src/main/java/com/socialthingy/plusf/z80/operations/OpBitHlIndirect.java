package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpBitHlIndirect extends BitOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpBitHlIndirect(final Processor processor, final int[] memory, final int bitPosition) {
        super(processor, bitPosition);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        checkBit(memory[hlReg.get()]);
        return 12;
    }

    @Override
    public String toString() {
        return "bit (hl)";
    }
}
