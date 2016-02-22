package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;

public class Nop implements Operation {
    @Override
    public int execute() {
        return 4;
    }
}
