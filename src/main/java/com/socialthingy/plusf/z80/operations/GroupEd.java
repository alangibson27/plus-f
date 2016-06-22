package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class GroupEd implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final IO io;
    private final Operation[] operations;

    public GroupEd(final Processor processor, final int[] memory, final IO io) {
        this.processor = processor;
        this.memory = memory;
        this.io = io;

        operations = OperationTable.buildEdGroup(processor, memory, io);
    }

    @Override
    public int execute() {
        final Operation operation = operations[processor.fetchNextOpcode()];
        processor.setLastOp(operation);
        if (operation == null) {
            throw new IllegalStateException("Unimplemented operation");
        }
        return operation.execute();
    }
}
