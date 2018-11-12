package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpDecHlIndirect extends DecOperation {

    private final Register hlReg;
    private final Memory memory;

    public OpDecHlIndirect(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int address = hlReg.get();
        final int result = decrement(memory.get(address));
        clock.tick(1);
        memory.set( address, result);
    }

    @Override
    public String toString() {
        return "dec (hl)";
    }
}
