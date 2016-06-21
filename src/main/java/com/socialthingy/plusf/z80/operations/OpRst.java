package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;

public class OpRst extends CallOperation {
    private final int address;

    public OpRst(final Processor processor, final int address) {
        super(processor);
        this.address = address;
    }

    @Override
    public int execute() {
        call(address);
        return 11;
    }

    @Override
    public String toString() {
        return String.format("rst %02x", address);
    }
}
