package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpBitHlIndirect extends BitOperation {

    private final Memory memory;
    private final Register hlReg;
    private final String toString;

    public OpBitHlIndirect(final Processor processor, final Clock clock, final Memory memory, final int bitPosition) {
        super(processor, clock, bitPosition);
        this.memory = memory;
        this.hlReg = processor.register("hl");

        this.toString = String.format("bit %d, (hl)", bitPosition);
    }

    @Override
    public void execute() {
        checkBit(memory.get(hlReg.get()));
        clock.tick(1);
    }

    @Override
    public String toString() {
        return toString;
    }
}
