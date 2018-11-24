package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLd8RegFrom8Reg extends Operation {

    private final ByteRegister dest;
    private final ByteRegister source;

    public OpLd8RegFrom8Reg(final Register dest, final Register source) {
        this.dest = (ByteRegister) dest;
        this.source = (ByteRegister) source;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        dest.set(source.get());
    }

    @Override
    public String toString() {
        return String.format("ld %s, %s", dest.name(), source.name());
    }
}
