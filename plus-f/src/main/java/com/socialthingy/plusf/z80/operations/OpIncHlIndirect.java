package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpIncHlIndirect extends IncOperation {

    private final Register hlReg;
    private final Memory memory;

    public OpIncHlIndirect(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int address = hlReg.get();
        final int result = increment(memory.get(address));
        memory.set( address, result);
        clock.tick(7);
    }

    @Override
    public String toString() {
        return "inc (hl)";
    }
}
