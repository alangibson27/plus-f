package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.ByteRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Register;

public class OpLd8RegFrom8Reg implements Operation {

    private ByteRegister dest;
    private ByteRegister source;

    public OpLd8RegFrom8Reg(Register dest, Register source) {
        this.dest = (ByteRegister) dest;
        this.source = (ByteRegister) source;
    }

    @Override
    public int execute() {
        dest.set(source.get());
        return 4;
    }
}
