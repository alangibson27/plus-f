package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class GroupDdFd implements Operation {

    private final Processor processor;
    private final Operation[] operations;

    public GroupDdFd(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        this.processor = processor;
        operations = OperationTable.buildIndexedGroup(processor, memory, indexRegister);
    }

    @Override
    public int execute() {
        final Operation operation = operations[processor.fetchNextOpcode()];
        if (operation == null) {
            throw new IllegalStateException("Unimplemented operation");
        }
        return operation.execute();
    }
}
