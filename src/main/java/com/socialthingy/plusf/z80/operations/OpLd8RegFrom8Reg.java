package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ByteRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpLd8RegFrom8Reg implements Operation {

    private final ByteRegister dest;
    private final ByteRegister source;

    public OpLd8RegFrom8Reg(final Register dest, final Register source) {
        this.dest = (ByteRegister) dest;
        this.source = (ByteRegister) source;
    }

    @Override
    public int execute() {
        dest.set(source.get());
        return 4;
    }
}
