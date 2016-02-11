package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.ByteRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Register;

public class OpLd8RegFrom16RegIndirect implements Operation {

    private Register dest;
    private Register sourceReference;
    private int[] memory;

    public OpLd8RegFrom16RegIndirect(int[] memory, Register dest, Register sourceReference) {
        this.memory = memory;
        this.dest = dest;
        this.sourceReference = sourceReference;
    }

    @Override
    public int execute() {
        dest.set(memory[sourceReference.get()]);
        return 7;
    }
}
