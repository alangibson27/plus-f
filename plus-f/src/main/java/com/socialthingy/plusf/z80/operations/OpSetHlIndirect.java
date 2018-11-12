package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSetHlIndirect extends BitModificationOperation {

    private final Register hlReg;
    private final Memory memory;
    private final String toString;

    public OpSetHlIndirect(final Processor processor, final Clock clock, final Memory memory, final int bitPosition) {
        super(clock, bitPosition);
        this.hlReg = processor.register("hl");
        this.memory = memory;

        this.toString = String.format("set %d, (hl)", bitPosition);
    }

    @Override
    public void execute() {
        final int address = hlReg.get();
        memory.set( address, set(memory.get(address)));
        clock.tick(7);
    }

    @Override
    public String toString() {
        return toString;
    }
}
