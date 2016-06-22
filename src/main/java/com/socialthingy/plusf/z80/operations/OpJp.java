package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJp implements Operation {

    private final Processor processor;
    private final Register pcReg;

    public OpJp(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
    }

    @Override
    public int execute() {
        pcReg.set(processor.fetchNextWord());
        return 10;
    }

    @Override
    public String toString() {
        return "jp nn";
    }
}
