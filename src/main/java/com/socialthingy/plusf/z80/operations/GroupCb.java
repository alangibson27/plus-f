package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class GroupCb implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final Operation[] operations = new Operation[0x100];

    public GroupCb(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.memory = memory;

        prepareOperations();
    }

    private void prepareOperations() {
        operations[0x00] = new OpRlcReg(processor, processor.register("b"));
        operations[0x01] = new OpRlcReg(processor, processor.register("c"));
        operations[0x02] = new OpRlcReg(processor, processor.register("d"));
        operations[0x03] = new OpRlcReg(processor, processor.register("e"));
        operations[0x04] = new OpRlcReg(processor, processor.register("h"));
        operations[0x05] = new OpRlcReg(processor, processor.register("l"));
        operations[0x06] = new OpRlcHlIndirect(processor, memory);
        operations[0x07] = new OpRlcReg(processor, processor.register("a"));
        operations[0x08] = new OpRrcReg(processor, processor.register("b"));
        operations[0x09] = new OpRrcReg(processor, processor.register("c"));
        operations[0x0a] = new OpRrcReg(processor, processor.register("d"));
        operations[0x0b] = new OpRrcReg(processor, processor.register("e"));
        operations[0x0c] = new OpRrcReg(processor, processor.register("h"));
        operations[0x0d] = new OpRrcReg(processor, processor.register("l"));
        operations[0x0e] = new OpRrcHlIndirect(processor, memory);
        operations[0x0f] = new OpRrcReg(processor, processor.register("a"));

        operations[0x10] = new OpRlReg(processor, processor.register("b"));
        operations[0x11] = new OpRlReg(processor, processor.register("c"));
        operations[0x12] = new OpRlReg(processor, processor.register("d"));
        operations[0x13] = new OpRlReg(processor, processor.register("e"));
        operations[0x14] = new OpRlReg(processor, processor.register("h"));
        operations[0x15] = new OpRlReg(processor, processor.register("l"));
        operations[0x16] = new OpRlHlIndirect(processor, memory);
        operations[0x17] = new OpRlReg(processor, processor.register("a"));
        operations[0x18] = new OpRrReg(processor, processor.register("b"));
        operations[0x19] = new OpRrReg(processor, processor.register("c"));
        operations[0x1a] = new OpRrReg(processor, processor.register("d"));
        operations[0x1b] = new OpRrReg(processor, processor.register("e"));
        operations[0x1c] = new OpRrReg(processor, processor.register("h"));
        operations[0x1d] = new OpRrReg(processor, processor.register("l"));
        operations[0x1e] = new OpRrHlIndirect(processor, memory);
        operations[0x1f] = new OpRrReg(processor, processor.register("a"));

        operations[0x20] = new OpSlaReg(processor, processor.register("b"));
        operations[0x21] = new OpSlaReg(processor, processor.register("c"));
        operations[0x22] = new OpSlaReg(processor, processor.register("d"));
        operations[0x23] = new OpSlaReg(processor, processor.register("e"));
        operations[0x24] = new OpSlaReg(processor, processor.register("h"));
        operations[0x25] = new OpSlaReg(processor, processor.register("l"));
        operations[0x26] = new OpSlaHlIndirect(processor, memory);
        operations[0x27] = new OpSlaReg(processor, processor.register("a"));
        operations[0x28] = new OpSraReg(processor, processor.register("b"));
        operations[0x29] = new OpSraReg(processor, processor.register("c"));
        operations[0x2a] = new OpSraReg(processor, processor.register("d"));
        operations[0x2b] = new OpSraReg(processor, processor.register("e"));
        operations[0x2c] = new OpSraReg(processor, processor.register("h"));
        operations[0x2d] = new OpSraReg(processor, processor.register("l"));
        operations[0x2e] = new OpSraHlIndirect(processor, memory);
        operations[0x2f] = new OpSraReg(processor, processor.register("a"));

        operations[0x30] = new OpSllReg(processor, processor.register("b"));
        operations[0x31] = new OpSllReg(processor, processor.register("c"));
        operations[0x32] = new OpSllReg(processor, processor.register("d"));
        operations[0x33] = new OpSllReg(processor, processor.register("e"));
        operations[0x34] = new OpSllReg(processor, processor.register("h"));
        operations[0x35] = new OpSllReg(processor, processor.register("l"));
        operations[0x36] = new OpSllHlIndirect(processor, memory);
        operations[0x37] = new OpSllReg(processor, processor.register("a"));
        operations[0x38] = new OpSrlReg(processor, processor.register("b"));
        operations[0x39] = new OpSrlReg(processor, processor.register("c"));
        operations[0x3a] = new OpSrlReg(processor, processor.register("d"));
        operations[0x3b] = new OpSrlReg(processor, processor.register("e"));
        operations[0x3c] = new OpSrlReg(processor, processor.register("h"));
        operations[0x3d] = new OpSrlReg(processor, processor.register("l"));
        operations[0x3e] = new OpSrlHlIndirect(processor, memory);
        operations[0x3f] = new OpSrlReg(processor, processor.register("a"));

        operations[0x40] = new OpBitReg(processor, processor.register("b"), 0);
        operations[0x41] = new OpBitReg(processor, processor.register("c"), 0);
        operations[0x42] = new OpBitReg(processor, processor.register("d"), 0);
        operations[0x43] = new OpBitReg(processor, processor.register("e"), 0);
        operations[0x44] = new OpBitReg(processor, processor.register("h"), 0);
        operations[0x45] = new OpBitReg(processor, processor.register("l"), 0);
        operations[0x46] = new OpBitHlIndirect(processor, memory, 0);
        operations[0x47] = new OpBitReg(processor, processor.register("a"), 0);
        operations[0x48] = new OpBitReg(processor, processor.register("b"), 1);
        operations[0x49] = new OpBitReg(processor, processor.register("c"), 1);
        operations[0x4a] = new OpBitReg(processor, processor.register("d"), 1);
        operations[0x4b] = new OpBitReg(processor, processor.register("e"), 1);
        operations[0x4c] = new OpBitReg(processor, processor.register("h"), 1);
        operations[0x4d] = new OpBitReg(processor, processor.register("l"), 1);
        operations[0x4e] = new OpBitHlIndirect(processor, memory, 1);
        operations[0x4f] = new OpBitReg(processor, processor.register("a"), 1);

        operations[0x50] = new OpBitReg(processor, processor.register("b"), 2);
        operations[0x51] = new OpBitReg(processor, processor.register("c"), 2);
        operations[0x52] = new OpBitReg(processor, processor.register("d"), 2);
        operations[0x53] = new OpBitReg(processor, processor.register("e"), 2);
        operations[0x54] = new OpBitReg(processor, processor.register("h"), 2);
        operations[0x55] = new OpBitReg(processor, processor.register("l"), 2);
        operations[0x56] = new OpBitHlIndirect(processor, memory, 2);
        operations[0x57] = new OpBitReg(processor, processor.register("a"), 2);
        operations[0x58] = new OpBitReg(processor, processor.register("b"), 3);
        operations[0x59] = new OpBitReg(processor, processor.register("c"), 3);
        operations[0x5a] = new OpBitReg(processor, processor.register("d"), 3);
        operations[0x5b] = new OpBitReg(processor, processor.register("e"), 3);
        operations[0x5c] = new OpBitReg(processor, processor.register("h"), 3);
        operations[0x5d] = new OpBitReg(processor, processor.register("l"), 3);
        operations[0x5e] = new OpBitHlIndirect(processor, memory, 3);
        operations[0x5f] = new OpBitReg(processor, processor.register("a"), 3);

        operations[0x60] = new OpBitReg(processor, processor.register("b"), 4);
        operations[0x61] = new OpBitReg(processor, processor.register("c"), 4);
        operations[0x62] = new OpBitReg(processor, processor.register("d"), 4);
        operations[0x63] = new OpBitReg(processor, processor.register("e"), 4);
        operations[0x64] = new OpBitReg(processor, processor.register("h"), 4);
        operations[0x65] = new OpBitReg(processor, processor.register("l"), 4);
        operations[0x66] = new OpBitHlIndirect(processor, memory, 4);
        operations[0x67] = new OpBitReg(processor, processor.register("a"), 4);
        operations[0x68] = new OpBitReg(processor, processor.register("b"), 5);
        operations[0x69] = new OpBitReg(processor, processor.register("c"), 5);
        operations[0x6a] = new OpBitReg(processor, processor.register("d"), 5);
        operations[0x6b] = new OpBitReg(processor, processor.register("e"), 5);
        operations[0x6c] = new OpBitReg(processor, processor.register("h"), 5);
        operations[0x6d] = new OpBitReg(processor, processor.register("l"), 5);
        operations[0x6e] = new OpBitHlIndirect(processor, memory, 5);
        operations[0x6f] = new OpBitReg(processor, processor.register("a"), 5);

        operations[0x70] = new OpBitReg(processor, processor.register("b"), 6);
        operations[0x71] = new OpBitReg(processor, processor.register("c"), 6);
        operations[0x72] = new OpBitReg(processor, processor.register("d"), 6);
        operations[0x73] = new OpBitReg(processor, processor.register("e"), 6);
        operations[0x74] = new OpBitReg(processor, processor.register("h"), 6);
        operations[0x75] = new OpBitReg(processor, processor.register("l"), 6);
        operations[0x76] = new OpBitHlIndirect(processor, memory, 6);
        operations[0x77] = new OpBitReg(processor, processor.register("a"), 6);
        operations[0x78] = new OpBitReg(processor, processor.register("b"), 7);
        operations[0x79] = new OpBitReg(processor, processor.register("c"), 7);
        operations[0x7a] = new OpBitReg(processor, processor.register("d"), 7);
        operations[0x7b] = new OpBitReg(processor, processor.register("e"), 7);
        operations[0x7c] = new OpBitReg(processor, processor.register("h"), 7);
        operations[0x7d] = new OpBitReg(processor, processor.register("l"), 7);
        operations[0x7e] = new OpBitHlIndirect(processor, memory, 7);
        operations[0x7f] = new OpBitReg(processor, processor.register("a"), 7);

        operations[0x80] = new OpResReg(processor.register("b"), 0);
        operations[0x81] = new OpResReg(processor.register("c"), 0);
        operations[0x82] = new OpResReg(processor.register("d"), 0);
        operations[0x83] = new OpResReg(processor.register("e"), 0);
        operations[0x84] = new OpResReg(processor.register("h"), 0);
        operations[0x85] = new OpResReg(processor.register("l"), 0);
        operations[0x86] = new OpResHlIndirect(processor, memory, 0);
        operations[0x87] = new OpResReg(processor.register("a"), 0);
        operations[0x88] = new OpResReg(processor.register("b"), 1);
        operations[0x89] = new OpResReg(processor.register("c"), 1);
        operations[0x8a] = new OpResReg(processor.register("d"), 1);
        operations[0x8b] = new OpResReg(processor.register("e"), 1);
        operations[0x8c] = new OpResReg(processor.register("h"), 1);
        operations[0x8d] = new OpResReg(processor.register("l"), 1);
        operations[0x8e] = new OpResHlIndirect(processor, memory, 1);
        operations[0x8f] = new OpResReg(processor.register("a"), 1);

        operations[0x90] = new OpResReg(processor.register("b"), 2);
        operations[0x91] = new OpResReg(processor.register("c"), 2);
        operations[0x92] = new OpResReg(processor.register("d"), 2);
        operations[0x93] = new OpResReg(processor.register("e"), 2);
        operations[0x94] = new OpResReg(processor.register("h"), 2);
        operations[0x95] = new OpResReg(processor.register("l"), 2);
        operations[0x96] = new OpResHlIndirect(processor, memory, 2);
        operations[0x97] = new OpResReg(processor.register("a"), 2);
        operations[0x98] = new OpResReg(processor.register("b"), 3);
        operations[0x99] = new OpResReg(processor.register("c"), 3);
        operations[0x9a] = new OpResReg(processor.register("d"), 3);
        operations[0x9b] = new OpResReg(processor.register("e"), 3);
        operations[0x9c] = new OpResReg(processor.register("h"), 3);
        operations[0x9d] = new OpResReg(processor.register("l"), 3);
        operations[0x9e] = new OpResHlIndirect(processor, memory, 3);
        operations[0x9f] = new OpResReg(processor.register("a"), 3);

        operations[0xa0] = new OpResReg(processor.register("b"), 4);
        operations[0xa1] = new OpResReg(processor.register("c"), 4);
        operations[0xa2] = new OpResReg(processor.register("d"), 4);
        operations[0xa3] = new OpResReg(processor.register("e"), 4);
        operations[0xa4] = new OpResReg(processor.register("h"), 4);
        operations[0xa5] = new OpResReg(processor.register("l"), 4);
        operations[0xa6] = new OpResHlIndirect(processor, memory, 4);
        operations[0xa7] = new OpResReg(processor.register("a"), 4);
        operations[0xa8] = new OpResReg(processor.register("b"), 5);
        operations[0xa9] = new OpResReg(processor.register("c"), 5);
        operations[0xaa] = new OpResReg(processor.register("d"), 5);
        operations[0xab] = new OpResReg(processor.register("e"), 5);
        operations[0xac] = new OpResReg(processor.register("h"), 5);
        operations[0xad] = new OpResReg(processor.register("l"), 5);
        operations[0xae] = new OpResHlIndirect(processor, memory, 5);
        operations[0xaf] = new OpResReg(processor.register("a"), 5);

        operations[0xb0] = new OpResReg(processor.register("b"), 6);
        operations[0xb1] = new OpResReg(processor.register("c"), 6);
        operations[0xb2] = new OpResReg(processor.register("d"), 6);
        operations[0xb3] = new OpResReg(processor.register("e"), 6);
        operations[0xb4] = new OpResReg(processor.register("h"), 6);
        operations[0xb5] = new OpResReg(processor.register("l"), 6);
        operations[0xb6] = new OpResHlIndirect(processor, memory, 6);
        operations[0xb7] = new OpResReg(processor.register("a"), 6);
        operations[0xb8] = new OpResReg(processor.register("b"), 7);
        operations[0xb9] = new OpResReg(processor.register("c"), 7);
        operations[0xba] = new OpResReg(processor.register("d"), 7);
        operations[0xbb] = new OpResReg(processor.register("e"), 7);
        operations[0xbc] = new OpResReg(processor.register("h"), 7);
        operations[0xbd] = new OpResReg(processor.register("l"), 7);
        operations[0xbe] = new OpResHlIndirect(processor, memory, 7);
        operations[0xbf] = new OpResReg(processor.register("a"), 7);

        operations[0xc0] = new OpSetReg(processor.register("b"), 0);
        operations[0xc1] = new OpSetReg(processor.register("c"), 0);
        operations[0xc2] = new OpSetReg(processor.register("d"), 0);
        operations[0xc3] = new OpSetReg(processor.register("e"), 0);
        operations[0xc4] = new OpSetReg(processor.register("h"), 0);
        operations[0xc5] = new OpSetReg(processor.register("l"), 0);
        operations[0xc6] = new OpSetHlIndirect(processor, memory, 0);
        operations[0xc7] = new OpSetReg(processor.register("a"), 0);
        operations[0xc8] = new OpSetReg(processor.register("b"), 1);
        operations[0xc9] = new OpSetReg(processor.register("c"), 1);
        operations[0xca] = new OpSetReg(processor.register("d"), 1);
        operations[0xcb] = new OpSetReg(processor.register("e"), 1);
        operations[0xcc] = new OpSetReg(processor.register("h"), 1);
        operations[0xcd] = new OpSetReg(processor.register("l"), 1);
        operations[0xce] = new OpSetHlIndirect(processor, memory, 1);
        operations[0xcf] = new OpSetReg(processor.register("a"), 1);

        operations[0xd0] = new OpSetReg(processor.register("b"), 2);
        operations[0xd1] = new OpSetReg(processor.register("c"), 2);
        operations[0xd2] = new OpSetReg(processor.register("d"), 2);
        operations[0xd3] = new OpSetReg(processor.register("e"), 2);
        operations[0xd4] = new OpSetReg(processor.register("h"), 2);
        operations[0xd5] = new OpSetReg(processor.register("l"), 2);
        operations[0xd6] = new OpSetHlIndirect(processor, memory, 2);
        operations[0xd7] = new OpSetReg(processor.register("a"), 2);
        operations[0xd8] = new OpSetReg(processor.register("b"), 3);
        operations[0xd9] = new OpSetReg(processor.register("c"), 3);
        operations[0xda] = new OpSetReg(processor.register("d"), 3);
        operations[0xdb] = new OpSetReg(processor.register("e"), 3);
        operations[0xdc] = new OpSetReg(processor.register("h"), 3);
        operations[0xdd] = new OpSetReg(processor.register("l"), 3);
        operations[0xde] = new OpSetHlIndirect(processor, memory, 3);
        operations[0xdf] = new OpSetReg(processor.register("a"), 3);

        operations[0xe0] = new OpSetReg(processor.register("b"), 4);
        operations[0xe1] = new OpSetReg(processor.register("c"), 4);
        operations[0xe2] = new OpSetReg(processor.register("d"), 4);
        operations[0xe3] = new OpSetReg(processor.register("e"), 4);
        operations[0xe4] = new OpSetReg(processor.register("h"), 4);
        operations[0xe5] = new OpSetReg(processor.register("l"), 4);
        operations[0xe6] = new OpSetHlIndirect(processor, memory, 4);
        operations[0xe7] = new OpSetReg(processor.register("a"), 4);
        operations[0xe8] = new OpSetReg(processor.register("b"), 5);
        operations[0xe9] = new OpSetReg(processor.register("c"), 5);
        operations[0xea] = new OpSetReg(processor.register("d"), 5);
        operations[0xeb] = new OpSetReg(processor.register("e"), 5);
        operations[0xec] = new OpSetReg(processor.register("h"), 5);
        operations[0xed] = new OpSetReg(processor.register("l"), 5);
        operations[0xee] = new OpSetHlIndirect(processor, memory, 5);
        operations[0xef] = new OpSetReg(processor.register("a"), 5);

        operations[0xf0] = new OpSetReg(processor.register("b"), 6);
        operations[0xf1] = new OpSetReg(processor.register("c"), 6);
        operations[0xf2] = new OpSetReg(processor.register("d"), 6);
        operations[0xf3] = new OpSetReg(processor.register("e"), 6);
        operations[0xf4] = new OpSetReg(processor.register("h"), 6);
        operations[0xf5] = new OpSetReg(processor.register("l"), 6);
        operations[0xf6] = new OpSetHlIndirect(processor, memory, 6);
        operations[0xf7] = new OpSetReg(processor.register("a"), 6);
        operations[0xf8] = new OpSetReg(processor.register("b"), 7);
        operations[0xf9] = new OpSetReg(processor.register("c"), 7);
        operations[0xfa] = new OpSetReg(processor.register("d"), 7);
        operations[0xfb] = new OpSetReg(processor.register("e"), 7);
        operations[0xfc] = new OpSetReg(processor.register("h"), 7);
        operations[0xfd] = new OpSetReg(processor.register("l"), 7);
        operations[0xfe] = new OpSetHlIndirect(processor, memory, 7);
        operations[0xff] = new OpSetReg(processor.register("a"), 7);
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
