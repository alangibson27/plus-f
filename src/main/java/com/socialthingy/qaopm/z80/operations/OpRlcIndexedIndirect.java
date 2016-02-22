package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class OpRlcIndexedIndirect extends RotateOperation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpRlcIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int address = indexRegister.withOffset(processor.fetchRelative(-2));
        final int result = rlcValue(memory[address]);
        setSignZeroAndParity(result);
        memory[address] = result;
        return 23;
    }
}
