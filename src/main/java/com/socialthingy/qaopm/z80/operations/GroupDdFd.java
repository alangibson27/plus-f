package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class GroupDdFd implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;
    private final Operation[] operations = new Operation[0x100];

    public GroupDdFd(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;

        prepareOperations();
    }

    private void prepareOperations() {
        operations[0x09] = new OpAddIndexedReg(processor, indexRegister, processor.register("bc"));
        operations[0x19] = new OpAddIndexedReg(processor, indexRegister, processor.register("de"));

        operations[0x21] = new OpLdIndexedImmediate(processor, indexRegister);
        operations[0x22] = new OpLdAddress16Reg(processor, memory, indexRegister);
        operations[0x23] = new OpIncIndexed(indexRegister);
        operations[0x24] = new OpIncIndexed8Reg(processor, indexRegister.highReg());
        operations[0x25] = new OpDecIndexed8Reg(processor, indexRegister.highReg());
        operations[0x26] = new OpLdIndexed8RegImmediate(processor, indexRegister.highReg());
        operations[0x29] = new OpAddIndexedReg(processor, indexRegister, indexRegister);
        operations[0x2a] = new OpLd16RegAddress(processor, memory, indexRegister);
        operations[0x2b] = new OpDecIndexed(indexRegister);
        operations[0x2c] = new OpIncIndexed8Reg(processor, indexRegister.lowReg());
        operations[0x2d] = new OpDecIndexed8Reg(processor, indexRegister.lowReg());
        operations[0x2e] = new OpLdIndexed8RegImmediate(processor, indexRegister.lowReg());

        operations[0x34] = new OpIncIndexedIndirect(processor, memory, indexRegister);
        operations[0x35] = new OpDecIndexedIndirect(processor, memory, indexRegister);
        operations[0x36] = new OpLdIndexedIndirectImmediate(processor, memory, indexRegister);
        operations[0x39] = new OpAddIndexedReg(processor, indexRegister, processor.register("sp"));

        operations[0x44] = new OpLdIndexed8RegFrom8Reg(processor.register("b"), indexRegister.highReg());
        operations[0x45] = new OpLdIndexed8RegFrom8Reg(processor.register("b"), indexRegister.lowReg());
        operations[0x46] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("b"), indexRegister);
        operations[0x4c] = new OpLdIndexed8RegFrom8Reg(processor.register("c"), indexRegister.highReg());
        operations[0x4d] = new OpLdIndexed8RegFrom8Reg(processor.register("c"), indexRegister.lowReg());
        operations[0x4e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("c"), indexRegister);

        operations[0x54] = new OpLdIndexed8RegFrom8Reg(processor.register("d"), indexRegister.highReg());
        operations[0x55] = new OpLdIndexed8RegFrom8Reg(processor.register("d"), indexRegister.lowReg());
        operations[0x56] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("d"), indexRegister);
        operations[0x5c] = new OpLdIndexed8RegFrom8Reg(processor.register("e"), indexRegister.highReg());
        operations[0x5d] = new OpLdIndexed8RegFrom8Reg(processor.register("e"), indexRegister.lowReg());
        operations[0x5e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("e"), indexRegister);

        operations[0x60] = new OpLdIndexed8RegFrom8Reg(indexRegister.highReg(), processor.register("b"));
        operations[0x61] = new OpLdIndexed8RegFrom8Reg(indexRegister.highReg(), processor.register("c"));
        operations[0x62] = new OpLdIndexed8RegFrom8Reg(indexRegister.highReg(), processor.register("d"));
        operations[0x63] = new OpLdIndexed8RegFrom8Reg(indexRegister.highReg(), processor.register("e"));
        operations[0x64] = new OpLdIndexed8RegFrom8Reg(indexRegister.highReg(), indexRegister.highReg());
        operations[0x65] = new OpLdIndexed8RegFrom8Reg(indexRegister.highReg(), indexRegister.lowReg());
        operations[0x66] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("h"), indexRegister);
        operations[0x67] = new OpLdIndexed8RegFrom8Reg(indexRegister.highReg(), processor.register("a"));
        operations[0x68] = new OpLdIndexed8RegFrom8Reg(indexRegister.lowReg(), processor.register("b"));
        operations[0x69] = new OpLdIndexed8RegFrom8Reg(indexRegister.lowReg(), processor.register("c"));
        operations[0x6a] = new OpLdIndexed8RegFrom8Reg(indexRegister.lowReg(), processor.register("d"));
        operations[0x6b] = new OpLdIndexed8RegFrom8Reg(indexRegister.lowReg(), processor.register("e"));
        operations[0x6c] = new OpLdIndexed8RegFrom8Reg(indexRegister.lowReg(), indexRegister.highReg());
        operations[0x6d] = new OpLdIndexed8RegFrom8Reg(indexRegister.lowReg(), indexRegister.lowReg());
        operations[0x6e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("l"), indexRegister);
        operations[0x6f] = new OpLdIndexed8RegFrom8Reg(indexRegister.lowReg(), processor.register("a"));

        operations[0x70] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("b"));
        operations[0x71] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("c"));
        operations[0x72] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("d"));
        operations[0x73] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("e"));
        operations[0x74] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("h"));
        operations[0x75] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("l"));
        operations[0x77] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("a"));
        operations[0x7c] = new OpLdIndexed8RegFrom8Reg(processor.register("a"), indexRegister.highReg());
        operations[0x7d] = new OpLdIndexed8RegFrom8Reg(processor.register("a"), indexRegister.lowReg());
        operations[0x7e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("a"), indexRegister);

        operations[0x84] = new OpAddAIndexed8Reg(processor, indexRegister.highReg(), false);
        operations[0x85] = new OpAddAIndexed8Reg(processor, indexRegister.lowReg(), false);
        operations[0x86] = new OpAddAIndexedIndirect(processor, memory, indexRegister, false);
        operations[0x8c] = new OpAddAIndexed8Reg(processor, indexRegister.highReg(), true);
        operations[0x8d] = new OpAddAIndexed8Reg(processor, indexRegister.lowReg(), true);
        operations[0x8e] = new OpAddAIndexedIndirect(processor, memory, indexRegister, true);

        operations[0x95] = new OpSubAIndexed8Reg(processor, indexRegister.lowReg(), false);
        operations[0x94] = new OpSubAIndexed8Reg(processor, indexRegister.highReg(), false);
        operations[0x96] = new OpSubAIndexedIndirect(processor, memory, indexRegister, false);
        operations[0x9c] = new OpSubAIndexed8Reg(processor, indexRegister.highReg(), true);
        operations[0x9d] = new OpSubAIndexed8Reg(processor, indexRegister.lowReg(), true);
        operations[0x9e] = new OpSubAIndexedIndirect(processor, memory, indexRegister, true);

        operations[0xa4] = new OpAndAIndexed8Reg(processor, indexRegister.highReg());
        operations[0xa5] = new OpAndAIndexed8Reg(processor, indexRegister.lowReg());
        operations[0xa6] = new OpAndIndexedIndirect(processor, memory, indexRegister);
        operations[0xac] = new OpXorAIndexed8Reg(processor, indexRegister.highReg());
        operations[0xad] = new OpXorAIndexed8Reg(processor, indexRegister.lowReg());
        operations[0xae] = new OpXorIndexedIndirect(processor, memory, indexRegister);

        operations[0xb4] = new OpOrAIndexed8Reg(processor, indexRegister.highReg());
        operations[0xb5] = new OpOrAIndexed8Reg(processor, indexRegister.lowReg());
        operations[0xb6] = new OpOrIndexedIndirect(processor, memory, indexRegister);
        operations[0xbc] = new OpCpAIndexed8Reg(processor, indexRegister.highReg());
        operations[0xbd] = new OpCpAIndexed8Reg(processor, indexRegister.lowReg());
        operations[0xbe] = new OpCpIndexedIndirect(processor, memory, indexRegister);

        operations[0xcb] = new GroupCbIndexed(indexRegister, processor, memory);

        operations[0xe1] = new OpPopIndexed(processor, indexRegister);
        operations[0xe3] = new OpExSpIndirectIndexed(processor, indexRegister, memory);
        operations[0xe5] = new OpPushIndexed(processor, indexRegister);
        operations[0xe9] = new OpJpIndexedIndirect(processor, indexRegister);

        operations[0xf9] = new OpLdSpIndexed(processor, indexRegister);
    }

    @Override
    public int execute() {
        final Operation operation = operations[processor.fetchNextPC()];
        if (operation == null) {
            throw new IllegalStateException("Unimplemented operation");
        }
        return operation.execute();
    }
}
