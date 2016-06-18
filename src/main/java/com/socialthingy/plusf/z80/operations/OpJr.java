package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJr implements Operation {

    private final Processor processor;
    private final Register pcReg;

    public OpJr(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
    }

    @Override
    public int execute() {
        final byte offset = (byte) processor.fetchNextByte();
        pcReg.set(pcReg.get() + offset);
        return 12;
    }
}
