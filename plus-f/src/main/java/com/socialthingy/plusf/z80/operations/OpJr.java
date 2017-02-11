package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJr implements Operation {

    private final Processor processor;
    private final Register pcReg;
    private final Register mysteryReg;

    public OpJr(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
        this.mysteryReg = processor.register("?");
    }

    @Override
    public int execute() {
        final byte offset = (byte) processor.fetchNextByte();
        final int target = (pcReg.get() + offset) & 0xffff;
        mysteryReg.set(target >> 8);
        pcReg.set(target);
        return 12;
    }

    @Override
    public String toString() {
        return "jr n";
    }
}
