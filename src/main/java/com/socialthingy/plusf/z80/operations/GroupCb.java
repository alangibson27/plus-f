package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class GroupCb implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final Operation[] operations;

    public GroupCb(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.memory = memory;
        this.operations = OperationTable.buildCbGroup(processor, memory);
    }

    @Override
    public int execute() {
        final Operation operation = operations[processor.fetchNextByte()];
        if (operation == null) {
            throw new IllegalStateException("Unimplemented operation");
        }
        return operation.execute();
    }
}
