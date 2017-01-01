package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpOrAHlIndirect extends OrOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpOrAHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        or(unsafe.getInt(memory, BASE + ((hlReg.get()) * SCALE)));
        return 7;
    }

    @Override
    public String toString() {
        return "or (hl)";
    }
}
