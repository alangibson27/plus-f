package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpOutCZero implements Operation {
    private final IO io;
    private final Register cReg;
    private final Register bReg;

    public OpOutCZero(final Processor processor, final IO io) {
        this.io = io;
        this.cReg = processor.register("c");
        this.bReg = processor.register("b");
    }

    @Override
    public int execute() {
        io.write(cReg.get(), bReg.get(), 0);
        return 12;
    }
}
