package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJpHlIndirect extends Operation {

    private final Register hlReg;
    private final Register pcReg;

    public OpJpHlIndirect(final Processor processor, final Clock clock) {
        super(clock);
        this.hlReg = processor.register("hl");
        this.pcReg = processor.register("pc");
    }

    @Override
    public void execute() {
        pcReg.set(hlReg.get());
    }

    @Override
    public String toString() {
        return "jp (hl)";
    }
}
