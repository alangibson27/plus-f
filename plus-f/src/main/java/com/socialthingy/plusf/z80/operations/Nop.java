package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;

public class Nop implements Operation {
    @Override
    public int execute() {
        return 4;
    }

    @Override
    public String toString() {
        return "nop";
    }
}
