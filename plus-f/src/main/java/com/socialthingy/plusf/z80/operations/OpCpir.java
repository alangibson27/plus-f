package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpCpir extends BlockOperation {
    public OpCpir(final Processor processor, final Memory memory) {
        super(processor, memory, 1);
    }

    @Override
    public int execute() {
        if (blockCompare() == 0) {
            return 16;
        } else {
            return adjustPC();
        }
    }

    @Override
    public String toString() {
        return "cpir";
    }
}
