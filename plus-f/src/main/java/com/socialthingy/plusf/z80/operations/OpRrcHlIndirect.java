package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRrcHlIndirect extends RotateOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpRrcHlIndirect(final Processor processor, final Memory memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int result = rrcValue(memory.get(address));
        setSignZeroAndParity(result);
        memory.set( address, result);
        return 15;
    }

    @Override
    public String toString() {
        return "rrc (hl)";
    }
}
