package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJp extends Operation {
    private final Processor processor;
    private final Register pcReg;

    public OpJp(final Processor processor, final Clock clock) {
        super(clock);
        this.processor = processor;
        this.pcReg = processor.register("pc");
    }

    @Override
    public void execute() {
        pcReg.set(processor.fetchNextWord());
    }

    @Override
    public String toString() {
        return "jp nn";
    }
}
