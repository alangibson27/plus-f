package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

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
        return 18;
    }
}
