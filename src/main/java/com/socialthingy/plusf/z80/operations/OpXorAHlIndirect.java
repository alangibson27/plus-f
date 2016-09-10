package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpXorAHlIndirect extends XorOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpXorAHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        xor(unsafe.getInt(memory, 16L + ((hlReg.get()) * 4)));
        return 7;
    }

    @Override
    public String toString() {
        return "xor (hl)";
    }
}
