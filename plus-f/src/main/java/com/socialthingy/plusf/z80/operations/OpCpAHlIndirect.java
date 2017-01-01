package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpCpAHlIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpCpAHlIndirect(final Processor processor, final int[] memory) {
        super(processor, false);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        sub(unsafe.getInt(memory, BASE + (hlReg.get() * SCALE)), false);
        return 7;
    }

    @Override
    public String toString() {
        return "cp (hl)";
    }
}

