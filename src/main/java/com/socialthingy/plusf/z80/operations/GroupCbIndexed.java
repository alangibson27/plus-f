package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class GroupCbIndexed implements Operation {

    private final IndexRegister indexRegister;
    private final Processor processor;
    private final int[] memory;
    private final Operation[] operations = new Operation[0x100];

    public GroupCbIndexed(final IndexRegister indexRegister, final Processor processor, final int[] memory) {
        this.indexRegister = indexRegister;
        this.processor = processor;
        this.memory = memory;

        prepareOperations();
    }

    private final void prepareOperations() {
            operations[0x06] = new OpRlcIndexedIndirect(processor, memory, indexRegister);
            operations[0x0e] = new OpRrcIndexedIndirect(processor, memory, indexRegister);
            operations[0x16] = new OpRlIndexedIndirect(processor, memory, indexRegister);
            operations[0x1e] = new OpRrIndexedIndirect(processor, memory, indexRegister);
            operations[0x26] = new OpSlaIndexedIndirect(processor, memory, indexRegister);
            operations[0x2e] = new OpSraIndexedIndirect(processor, memory, indexRegister);
            operations[0x3e] = new OpSrlIndexedIndirect(processor, memory, indexRegister);

            operations[0x46] = new OpBitIndexedIndirect(processor, memory, indexRegister, 0);
            operations[0x4e] = new OpBitIndexedIndirect(processor, memory, indexRegister, 1);
            operations[0x56] = new OpBitIndexedIndirect(processor, memory, indexRegister, 2);
            operations[0x5e] = new OpBitIndexedIndirect(processor, memory, indexRegister, 3);
            operations[0x66] = new OpBitIndexedIndirect(processor, memory, indexRegister, 4);
            operations[0x6e] = new OpBitIndexedIndirect(processor, memory, indexRegister, 5);
            operations[0x76] = new OpBitIndexedIndirect(processor, memory, indexRegister, 6);
            operations[0x7e] = new OpBitIndexedIndirect(processor, memory, indexRegister, 7);

            operations[0x86] = new OpResIndexedIndirect(processor, memory, indexRegister, 0);
            operations[0x8e] = new OpResIndexedIndirect(processor, memory, indexRegister, 1);
            operations[0x96] = new OpResIndexedIndirect(processor, memory, indexRegister, 2);
            operations[0x9e] = new OpResIndexedIndirect(processor, memory, indexRegister, 3);
            operations[0xa6] = new OpResIndexedIndirect(processor, memory, indexRegister, 4);
            operations[0xae] = new OpResIndexedIndirect(processor, memory, indexRegister, 5);
            operations[0xb6] = new OpResIndexedIndirect(processor, memory, indexRegister, 6);
            operations[0xbe] = new OpResIndexedIndirect(processor, memory, indexRegister, 7);

            operations[0xc6] = new OpSetIndexedIndirect(processor, memory, indexRegister, 0);
            operations[0xce] = new OpSetIndexedIndirect(processor, memory, indexRegister, 1);
            operations[0xd6] = new OpSetIndexedIndirect(processor, memory, indexRegister, 2);
            operations[0xde] = new OpSetIndexedIndirect(processor, memory, indexRegister, 3);
            operations[0xe6] = new OpSetIndexedIndirect(processor, memory, indexRegister, 4);
            operations[0xee] = new OpSetIndexedIndirect(processor, memory, indexRegister, 5);
            operations[0xf6] = new OpSetIndexedIndirect(processor, memory, indexRegister, 6);
            operations[0xfe] = new OpSetIndexedIndirect(processor, memory, indexRegister, 7);
    }

    @Override
    public int execute() {
        processor.fetchNextByte();
        final Operation operation = operations[processor.fetchNextByte()];
        if (operation == null) {
            throw new IllegalStateException("Unimplemented operation");
        }
        return operation.execute();
    }
}
