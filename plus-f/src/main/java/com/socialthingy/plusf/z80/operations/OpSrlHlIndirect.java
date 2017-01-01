package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.UnsafeUtil.BASE;
import static com.socialthingy.plusf.util.UnsafeUtil.SCALE;

public class OpSrlHlIndirect extends SrlOperation {
    private final Register hlReg;
    private final int[] memory;

    public OpSrlHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        Memory.set(memory, address, shift(unsafe.getInt(memory, BASE + ((address) * SCALE))));
        return 15;
    }

    @Override
    public String toString() {
        return "srl (hl)";
    }
}
