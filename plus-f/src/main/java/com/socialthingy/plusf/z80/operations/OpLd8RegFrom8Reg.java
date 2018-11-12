package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ByteRegister;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpLd8RegFrom8Reg extends Operation {

    private final ByteRegister dest;
    private final ByteRegister source;

    public OpLd8RegFrom8Reg(final Clock clock, final Register dest, final Register source) {
        super(clock);
        this.dest = (ByteRegister) dest;
        this.source = (ByteRegister) source;
    }

    @Override
    public void execute() {
        dest.set(source.get());
    }

    @Override
    public String toString() {
        return String.format("ld %s, %s", dest.name(), source.name());
    }
}
