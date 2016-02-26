package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpDdFdGroup implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;
    private final Operation[] operations = new Operation[0x100];

    public OpDdFdGroup(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;

        prepareOperations();
    }

    private void prepareOperations() {
        operations[0x09] = new OpAddIndexedReg(processor, indexRegister, processor.register("bc"));
//
        operations[0x19] = new OpAddIndexedReg(processor, indexRegister, processor.register("de"));
//
        operations[0x21] = new OpLdIndexedImmediate(processor, indexRegister);
        operations[0x22] = new OpLdAddress16Reg(processor, memory, indexRegister);
        operations[0x23] = new OpIncIndexed(indexRegister);
//        operations[0x24] = new Undocumented("inc ixh");
//        operations[0x25] = new Undocumented("dec ixh");
//        operations[0x26] = new Undocumented("ld ixh, n");
        operations[0x29] = new OpAddIndexedReg(processor, indexRegister, indexRegister);
        operations[0x2a] = new OpLd16RegAddress(processor, memory, indexRegister);
        operations[0x2b] = new OpDecIndexed(indexRegister);
//        operations[0x2c] = new Undocumented("inc ixl");
//        operations[0x2d] = new Undocumented("dec ixl");
//        operations[0x2e] = new Undocumented("ld ixl, n");
//
        operations[0x34] = new OpIncIndexedIndirect(processor, memory, indexRegister);
        operations[0x35] = new OpDecIndexedIndirect(processor, memory, indexRegister);
        operations[0x36] = new OpLdIndexedIndirectImmediate(processor, memory, indexRegister);
        operations[0x39] = new OpAddIndexedReg(processor, indexRegister, processor.register("sp"));
//
//        operations[0x44] = new Undocumented("ld b, ixh");
//        operations[0x45] = new Undocumented("ld b, ixl");
        operations[0x46] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("b"), indexRegister);
//        operations[0x4c] = new Undocumented("ld c, ixh");
//        operations[0x4d] = new Undocumented("ld c, ixl");
        operations[0x4e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("c"), indexRegister);
//
//        operations[0x54] = new Undocumented("ld d, ixh");
//        operations[0x55] = new Undocumented("ld d, ixl");
        operations[0x56] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("d"), indexRegister);
//        operations[0x5c] = new Undocumented("ld e, ixh");
//        operations[0x5d] = new Undocumented("ld e, ixl");
        operations[0x5e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("e"), indexRegister);
//
//        operations[0x60] = new Undocumented("ld ixh, b");
//        operations[0x61] = new Undocumented("ld ixh, c");
//        operations[0x62] = new Undocumented("ld ixh, d");
//        operations[0x63] = new Undocumented("ld ixh, e");
//        operations[0x64] = new Undocumented("ld ixh, h");
//        operations[0x65] = new Undocumented("ld ixh, l");
        operations[0x66] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("h"), indexRegister);
//        operations[0x67] = new Undocumented("ld ixl, a");
//        operations[0x68] = new Undocumented("ld ixl, b");
//        operations[0x69] = new Undocumented("ld ixl, c");
//        operations[0x6a] = new Undocumented("ld ixl, d");
//        operations[0x6b] = new Undocumented("ld ixl, e");
//        operations[0x6c] = new Undocumented("ld ixl, h");
//        operations[0x6d] = new Undocumented("ld ixl, l");
        operations[0x6e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("l"), indexRegister);
//        operations[0x6f] = new Undocumented("ld ixl, a");
//
        operations[0x70] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("b"));
        operations[0x71] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("c"));
        operations[0x72] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("d"));
        operations[0x73] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("e"));
        operations[0x74] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("h"));
        operations[0x75] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("l"));
        operations[0x77] = new OpLdIndexedIndirect8Reg(processor, memory, indexRegister, processor.register("a"));
//        operations[0x7c] = new Undocumented("ld a, ixh");
//        operations[0x7d] = new Undocumented("ld a, ixl");
        operations[0x7e] = new OpLd8RegIndexedIndirect(processor, memory, processor.register("a"), indexRegister);
//
//        operations[0x84] = new Undocumented("add a, ixh");
//        operations[0x85] = new Undocumented("add a, ixl");
        operations[0x86] = new OpAddAIndexedIndirect(processor, memory, indexRegister, false);
//        operations[0x8c] = new Undocumented("adc a, ixh");
//        operations[0x8d] = new Undocumented("adc a, ixl");
        operations[0x8e] = new OpAddAIndexedIndirect(processor, memory, indexRegister, true);
//
//        operations[0x94] = new Undocumented("sub ixh");
//        operations[0x95] = new Undocumented("sub ixl");
        operations[0x96] = new OpSubAIndexedIndirect(processor, memory, indexRegister, false);
//        operations[0x9c] = new Undocumented("sbc a, ixh");
//        operations[0x9d] = new Undocumented("sbc a, ixl");
        operations[0x9e] = new OpSubAIndexedIndirect(processor, memory, indexRegister, true);
//
//        operations[0xa4] = new Undocumented("and ixh");
//        operations[0xa5] = new Undocumented("and ixl");
        operations[0xa6] = new OpAndIndexedIndirect(processor, memory, indexRegister);
//        operations[0xac] = new Undocumented("xor a, ixh");
//        operations[0xad] = new Undocumented("xor a, ixl");
        operations[0xae] = new OpXorIndexedIndirect(processor, memory, indexRegister);
//
//        operations[0xb4] = new Undocumented("or ixh");
//        operations[0xb5] = new Undocumented("or ixl");
        operations[0xb6] = new OpOrIndexedIndirect(processor, memory, indexRegister);
//        operations[0xbc] = new Undocumented("cp ixh");
//        operations[0xbd] = new Undocumented("cp ixl");
        operations[0xbe] = new OpCpIndexedIndirect(processor, memory, indexRegister);
//
        operations[0xcb] = new OpIndexedCbGroup(indexRegister, processor, memory);
//
        operations[0xe1] = new OpPopIndexed(processor, indexRegister);
        operations[0xe3] = new OpExSpIndirectIndexed(processor, indexRegister, memory);
        operations[0xe5] = new OpPushIndexed(processor, indexRegister);
        operations[0xe9] = new OpJpIndexedIndirect(processor, indexRegister);
//
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
