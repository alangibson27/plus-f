package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpXorAHlIndirect extends XorOperation {
    private final Register hlReg;
    private final Memory memory;

    public OpXorAHlIndirect(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute() {
        xor(memory.get(hlReg.get()));
        clock.tick(3);
    }

    @Override
    public String toString() {
        return "xor (hl)";
    }
}
