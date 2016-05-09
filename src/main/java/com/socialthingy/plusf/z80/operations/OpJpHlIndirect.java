package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJpHlIndirect implements Operation {

    private final Register hlReg;
    private final Register pcReg;

    public OpJpHlIndirect(final Processor processor) {
        this.hlReg = processor.register("hl");
        this.pcReg = processor.register("pc");
    }

    @Override
    public int execute() {
        pcReg.set(hlReg.get());
        return 4;
    }
}
