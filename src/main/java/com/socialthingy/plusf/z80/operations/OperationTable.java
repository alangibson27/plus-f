package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OperationTable {
    private OperationTable() {}
    
    public static Operation[] build(
        final Processor processor,
        final int[] memory,
        final IO io
    ) {
        final Operation[] operations = new Operation[0x100];
        operations[0x00] = new Nop();

        operations[0x01] = new OpLd16RegImmediate(processor, processor.register("bc"));
        operations[0x02] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("bc"), processor.register("a"));
        operations[0x03] = new OpInc16Reg(processor.register("bc"));
        operations[0x04] = new OpInc8Reg(processor, processor.register("b"));
        operations[0x05] = new OpDec8Reg(processor, processor.register("b"));
        operations[0x06] = new OpLd8RegImmediate(processor, processor.register("b"));
        operations[0x07] = new OpRlca(processor);
        operations[0x08] = new OpExRegister(processor.register("af"), processor.register("af'"));
        operations[0x09] = new OpAddHl16Reg(processor, processor.register("bc"));
        operations[0x0a] = new OpLd8RegFrom16RegIndirect(memory, processor.register("a"), processor.register("bc"));
        operations[0x0b] = new OpDec16Reg(processor.register("bc"));
        operations[0x0c] = new OpInc8Reg(processor, processor.register("c"));
        operations[0x0d] = new OpDec8Reg(processor, processor.register("c"));
        operations[0x0e] = new OpLd8RegImmediate(processor, processor.register("c"));
        operations[0x0f] = new OpRrca(processor);

        operations[0x10] = new OpDjnz(processor);
        operations[0x11] = new OpLd16RegImmediate(processor, processor.register("de"));
        operations[0x12] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("de"), processor.register("a"));
        operations[0x13] = new OpInc16Reg(processor.register("de"));
        operations[0x14] = new OpInc8Reg(processor, processor.register("d"));
        operations[0x15] = new OpDec8Reg(processor, processor.register("d"));
        operations[0x16] = new OpLd8RegImmediate(processor, processor.register("d"));
        operations[0x17] = new OpRla(processor);
        operations[0x18] = new OpJr(processor);
        operations[0x19] = new OpAddHl16Reg(processor, processor.register("de"));
        operations[0x1a] = new OpLd8RegFrom16RegIndirect(memory, processor.register("a"), processor.register("de"));
        operations[0x1b] = new OpDec16Reg(processor.register("de"));
        operations[0x1c] = new OpInc8Reg(processor, processor.register("e"));
        operations[0x1d] = new OpDec8Reg(processor, processor.register("e"));
        operations[0x1e] = new OpLd8RegImmediate(processor, processor.register("e"));
        operations[0x1f] = new OpRra(processor);

        operations[0x20] = new OpJrConditional(processor, FlagsRegister.Flag.Z, false);
        operations[0x21] = new OpLd16RegImmediate(processor, processor.register("hl"));
        operations[0x22] = new OpLdAddressHl(processor, memory);
        operations[0x23] = new OpInc16Reg(processor.register("hl"));
        operations[0x24] = new OpInc8Reg(processor, processor.register("h"));
        operations[0x25] = new OpDec8Reg(processor, processor.register("h"));
        operations[0x26] = new OpLd8RegImmediate(processor, processor.register("h"));
        operations[0x27] = new OpDaa(processor);
        operations[0x28] = new OpJrConditional(processor, FlagsRegister.Flag.Z, true);
        operations[0x29] = new OpAddHl16Reg(processor, processor.register("hl"));
        operations[0x2a] = new OpLdHlAddress(processor, memory);
        operations[0x2b] = new OpDec16Reg(processor.register("hl"));
        operations[0x2c] = new OpInc8Reg(processor, processor.register("l"));
        operations[0x2d] = new OpDec8Reg(processor, processor.register("l"));
        operations[0x2e] = new OpLd8RegImmediate(processor, processor.register("l"));
        operations[0x2f] = new OpCpl(processor);

        operations[0x30] = new OpJrConditional(processor, FlagsRegister.Flag.C, false);
        operations[0x31] = new OpLd16RegImmediate(processor, processor.register("sp"));
        operations[0x32] = new OpLdAddressA(processor, memory);
        operations[0x33] = new OpInc16Reg(processor.register("sp"));
        operations[0x34] = new OpIncHlIndirect(processor, memory);
        operations[0x35] = new OpDecHlIndirect(processor, memory);
        operations[0x36] = new OpLdHlIndirectImmediate(processor, memory);
        operations[0x37] = new OpScf(processor);
        operations[0x38] = new OpJrConditional(processor, FlagsRegister.Flag.C, true);
        operations[0x39] = new OpAddHl16Reg(processor, processor.register("sp"));
        operations[0x3a] = new OpLdAAddress(processor, memory);
        operations[0x3b] = new OpDec16Reg(processor.register("sp"));
        operations[0x3c] = new OpInc8Reg(processor, processor.register("a"));
        operations[0x3d] = new OpDec8Reg(processor, processor.register("a"));
        operations[0x3e] = new OpLd8RegImmediate(processor, processor.register("a"));
        operations[0x3f] = new OpCcf(processor);

        operations[0x40] = new OpLd8RegFrom8Reg(processor.register("b"), processor.register("b"));
        operations[0x41] = new OpLd8RegFrom8Reg(processor.register("b"), processor.register("c"));
        operations[0x42] = new OpLd8RegFrom8Reg(processor.register("b"), processor.register("d"));
        operations[0x43] = new OpLd8RegFrom8Reg(processor.register("b"), processor.register("e"));
        operations[0x44] = new OpLd8RegFrom8Reg(processor.register("b"), processor.register("h"));
        operations[0x45] = new OpLd8RegFrom8Reg(processor.register("b"), processor.register("l"));
        operations[0x46] = new OpLd8RegFrom16RegIndirect(memory, processor.register("b"), processor.register("hl"));
        operations[0x47] = new OpLd8RegFrom8Reg(processor.register("b"), processor.register("a"));
        operations[0x48] = new OpLd8RegFrom8Reg(processor.register("c"), processor.register("b"));
        operations[0x49] = new OpLd8RegFrom8Reg(processor.register("c"), processor.register("c"));
        operations[0x4a] = new OpLd8RegFrom8Reg(processor.register("c"), processor.register("d"));
        operations[0x4b] = new OpLd8RegFrom8Reg(processor.register("c"), processor.register("e"));
        operations[0x4c] = new OpLd8RegFrom8Reg(processor.register("c"), processor.register("h"));
        operations[0x4d] = new OpLd8RegFrom8Reg(processor.register("c"), processor.register("l"));
        operations[0x4e] = new OpLd8RegFrom16RegIndirect(memory, processor.register("c"), processor.register("hl"));
        operations[0x4f] = new OpLd8RegFrom8Reg(processor.register("c"), processor.register("a"));

        operations[0x50] = new OpLd8RegFrom8Reg(processor.register("d"), processor.register("b"));
        operations[0x51] = new OpLd8RegFrom8Reg(processor.register("d"), processor.register("c"));
        operations[0x52] = new OpLd8RegFrom8Reg(processor.register("d"), processor.register("d"));
        operations[0x53] = new OpLd8RegFrom8Reg(processor.register("d"), processor.register("e"));
        operations[0x54] = new OpLd8RegFrom8Reg(processor.register("d"), processor.register("h"));
        operations[0x55] = new OpLd8RegFrom8Reg(processor.register("d"), processor.register("l"));
        operations[0x56] = new OpLd8RegFrom16RegIndirect(memory, processor.register("d"), processor.register("hl"));
        operations[0x57] = new OpLd8RegFrom8Reg(processor.register("d"), processor.register("a"));
        operations[0x58] = new OpLd8RegFrom8Reg(processor.register("e"), processor.register("b"));
        operations[0x59] = new OpLd8RegFrom8Reg(processor.register("e"), processor.register("c"));
        operations[0x5a] = new OpLd8RegFrom8Reg(processor.register("e"), processor.register("d"));
        operations[0x5b] = new OpLd8RegFrom8Reg(processor.register("e"), processor.register("e"));
        operations[0x5c] = new OpLd8RegFrom8Reg(processor.register("e"), processor.register("h"));
        operations[0x5d] = new OpLd8RegFrom8Reg(processor.register("e"), processor.register("l"));
        operations[0x5e] = new OpLd8RegFrom16RegIndirect(memory, processor.register("e"), processor.register("hl"));
        operations[0x5f] = new OpLd8RegFrom8Reg(processor.register("e"), processor.register("a"));

        operations[0x60] = new OpLd8RegFrom8Reg(processor.register("h"), processor.register("b"));
        operations[0x61] = new OpLd8RegFrom8Reg(processor.register("h"), processor.register("c"));
        operations[0x62] = new OpLd8RegFrom8Reg(processor.register("h"), processor.register("d"));
        operations[0x63] = new OpLd8RegFrom8Reg(processor.register("h"), processor.register("e"));
        operations[0x64] = new OpLd8RegFrom8Reg(processor.register("h"), processor.register("h"));
        operations[0x65] = new OpLd8RegFrom8Reg(processor.register("h"), processor.register("l"));
        operations[0x66] = new OpLd8RegFrom16RegIndirect(memory, processor.register("h"), processor.register("hl"));
        operations[0x67] = new OpLd8RegFrom8Reg(processor.register("h"), processor.register("a"));
        operations[0x68] = new OpLd8RegFrom8Reg(processor.register("l"), processor.register("b"));
        operations[0x69] = new OpLd8RegFrom8Reg(processor.register("l"), processor.register("c"));
        operations[0x6a] = new OpLd8RegFrom8Reg(processor.register("l"), processor.register("d"));
        operations[0x6b] = new OpLd8RegFrom8Reg(processor.register("l"), processor.register("e"));
        operations[0x6c] = new OpLd8RegFrom8Reg(processor.register("l"), processor.register("h"));
        operations[0x6d] = new OpLd8RegFrom8Reg(processor.register("l"), processor.register("l"));
        operations[0x6e] = new OpLd8RegFrom16RegIndirect(memory, processor.register("l"), processor.register("hl"));
        operations[0x6f] = new OpLd8RegFrom8Reg(processor.register("l"), processor.register("a"));

        operations[0x70] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("hl"), processor.register("b"));
        operations[0x71] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("hl"), processor.register("c"));
        operations[0x72] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("hl"), processor.register("d"));
        operations[0x73] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("hl"), processor.register("e"));
        operations[0x74] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("hl"), processor.register("h"));
        operations[0x75] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("hl"), processor.register("l"));
        operations[0x76] = new OpHalt(processor);
        operations[0x77] = new OpLd16RegIndirectFrom8Reg(memory, processor.register("hl"), processor.register("a"));
        operations[0x78] = new OpLd8RegFrom8Reg(processor.register("a"), processor.register("b"));
        operations[0x79] = new OpLd8RegFrom8Reg(processor.register("a"), processor.register("c"));
        operations[0x7a] = new OpLd8RegFrom8Reg(processor.register("a"), processor.register("d"));
        operations[0x7b] = new OpLd8RegFrom8Reg(processor.register("a"), processor.register("e"));
        operations[0x7c] = new OpLd8RegFrom8Reg(processor.register("a"), processor.register("h"));
        operations[0x7d] = new OpLd8RegFrom8Reg(processor.register("a"), processor.register("l"));
        operations[0x7e] = new OpLd8RegFrom16RegIndirect(memory, processor.register("a"), processor.register("hl"));
        operations[0x7f] = new OpLd8RegFrom8Reg(processor.register("a"), processor.register("a"));

        operations[0x80] = new OpAddA8Reg(processor, processor.register("b"), false);
        operations[0x81] = new OpAddA8Reg(processor, processor.register("c"), false);
        operations[0x82] = new OpAddA8Reg(processor, processor.register("d"), false);
        operations[0x83] = new OpAddA8Reg(processor, processor.register("e"), false);
        operations[0x84] = new OpAddA8Reg(processor, processor.register("h"), false);
        operations[0x85] = new OpAddA8Reg(processor, processor.register("l"), false);
        operations[0x86] = new OpAddAHlIndirect(processor, memory, false);
        operations[0x87] = new OpAddA8Reg(processor, processor.register("a"), false);
        operations[0x88] = new OpAddA8Reg(processor, processor.register("b"), true);
        operations[0x89] = new OpAddA8Reg(processor, processor.register("c"), true);
        operations[0x8a] = new OpAddA8Reg(processor, processor.register("d"), true);
        operations[0x8b] = new OpAddA8Reg(processor, processor.register("e"), true);
        operations[0x8c] = new OpAddA8Reg(processor, processor.register("h"), true);
        operations[0x8d] = new OpAddA8Reg(processor, processor.register("l"), true);
        operations[0x8e] = new OpAddAHlIndirect(processor, memory, true);
        operations[0x8f] = new OpAddA8Reg(processor, processor.register("a"), true);

        operations[0x90] = new OpSubA8Reg(processor, processor.register("b"), false);
        operations[0x91] = new OpSubA8Reg(processor, processor.register("c"), false);
        operations[0x92] = new OpSubA8Reg(processor, processor.register("d"), false);
        operations[0x93] = new OpSubA8Reg(processor, processor.register("e"), false);
        operations[0x94] = new OpSubA8Reg(processor, processor.register("h"), false);
        operations[0x95] = new OpSubA8Reg(processor, processor.register("l"), false);
        operations[0x96] = new OpSubAHlIndirect(processor, memory, false);
        operations[0x97] = new OpSubA8Reg(processor, processor.register("a"), false);
        operations[0x98] = new OpSubA8Reg(processor, processor.register("b"), true);
        operations[0x99] = new OpSubA8Reg(processor, processor.register("c"), true);
        operations[0x9a] = new OpSubA8Reg(processor, processor.register("d"), true);
        operations[0x9b] = new OpSubA8Reg(processor, processor.register("e"), true);
        operations[0x9c] = new OpSubA8Reg(processor, processor.register("h"), true);
        operations[0x9d] = new OpSubA8Reg(processor, processor.register("l"), true);
        operations[0x9e] = new OpSubAHlIndirect(processor, memory, true);
        operations[0x9f] = new OpSubA8Reg(processor, processor.register("a"), true);

        operations[0xa0] = new OpAndA8Reg(processor, processor.register("b"));
        operations[0xa1] = new OpAndA8Reg(processor, processor.register("c"));
        operations[0xa2] = new OpAndA8Reg(processor, processor.register("d"));
        operations[0xa3] = new OpAndA8Reg(processor, processor.register("e"));
        operations[0xa4] = new OpAndA8Reg(processor, processor.register("h"));
        operations[0xa5] = new OpAndA8Reg(processor, processor.register("l"));
        operations[0xa6] = new OpAndAHlIndirect(processor, memory);
        operations[0xa7] = new OpAndA8Reg(processor, processor.register("a"));
        operations[0xa8] = new OpXorA8Reg(processor, processor.register("b"));
        operations[0xa9] = new OpXorA8Reg(processor, processor.register("c"));
        operations[0xaa] = new OpXorA8Reg(processor, processor.register("d"));
        operations[0xab] = new OpXorA8Reg(processor, processor.register("e"));
        operations[0xac] = new OpXorA8Reg(processor, processor.register("h"));
        operations[0xad] = new OpXorA8Reg(processor, processor.register("l"));
        operations[0xae] = new OpXorAHlIndirect(processor, memory);
        operations[0xaf] = new OpXorA8Reg(processor, processor.register("a"));

        operations[0xb0] = new OpOrA8Reg(processor, processor.register("b"));
        operations[0xb1] = new OpOrA8Reg(processor, processor.register("c"));
        operations[0xb2] = new OpOrA8Reg(processor, processor.register("d"));
        operations[0xb3] = new OpOrA8Reg(processor, processor.register("e"));
        operations[0xb4] = new OpOrA8Reg(processor, processor.register("h"));
        operations[0xb5] = new OpOrA8Reg(processor, processor.register("l"));
        operations[0xb6] = new OpOrAHlIndirect(processor, memory);
        operations[0xb7] = new OpOrA8Reg(processor, processor.register("a"));
        operations[0xb8] = new OpCpA8Reg(processor, processor.register("b"));
        operations[0xb9] = new OpCpA8Reg(processor, processor.register("c"));
        operations[0xba] = new OpCpA8Reg(processor, processor.register("d"));
        operations[0xbb] = new OpCpA8Reg(processor, processor.register("e"));
        operations[0xbc] = new OpCpA8Reg(processor, processor.register("h"));
        operations[0xbd] = new OpCpA8Reg(processor, processor.register("l"));
        operations[0xbe] = new OpCpAHlIndirect(processor, memory);
        operations[0xbf] = new OpCpA8Reg(processor, processor.register("a"));

        operations[0xc0] = new OpRetConditional(processor, FlagsRegister.Flag.Z, false);
        operations[0xc1] = new OpPop16Reg(processor, processor.register("bc"));
        operations[0xc2] = new OpJpConditional(processor, FlagsRegister.Flag.Z, false);
        operations[0xc3] = new OpJp(processor);
        operations[0xc4] = new OpCallConditional(processor, FlagsRegister.Flag.Z, false);
        operations[0xc5] = new OpPush16Reg(processor, processor.register("bc"));
        operations[0xc6] = new OpAddAImmediate(processor, false);
        operations[0xc7] = new OpRst(processor, 0x00);
        operations[0xc8] = new OpRetConditional(processor, FlagsRegister.Flag.Z, true);
        operations[0xc9] = new OpRet(processor);
        operations[0xca] = new OpJpConditional(processor, FlagsRegister.Flag.Z, true);
        operations[0xcc] = new OpCallConditional(processor, FlagsRegister.Flag.Z, true);
        operations[0xcd] = new OpCall(processor);
        operations[0xce] = new OpAddAImmediate(processor, true);
        operations[0xcf] = new OpRst(processor, 0x08);

        operations[0xd0] = new OpRetConditional(processor, FlagsRegister.Flag.C, false);
        operations[0xd1] = new OpPop16Reg(processor, processor.register("de"));
        operations[0xd2] = new OpJpConditional(processor, FlagsRegister.Flag.C, false);
        operations[0xd3] = new OpOutA(processor, io);
        operations[0xd4] = new OpCallConditional(processor, FlagsRegister.Flag.C, false);
        operations[0xd5] = new OpPush16Reg(processor, processor.register("de"));
        operations[0xd6] = new OpSubAImmediate(processor, false);
        operations[0xd7] = new OpRst(processor, 0x10);
        operations[0xd8] = new OpRetConditional(processor, FlagsRegister.Flag.C, true);
        operations[0xd9] = new OpExx(processor);
        operations[0xda] = new OpJpConditional(processor, FlagsRegister.Flag.C, true);
        operations[0xdb] = new OpInA(processor, io);
        operations[0xdc] = new OpCallConditional(processor, FlagsRegister.Flag.C, true);
        operations[0xde] = new OpSubAImmediate(processor, true);
        operations[0xdf] = new OpRst(processor, 0x18);

        operations[0xe0] = new OpRetConditional(processor, FlagsRegister.Flag.P, false);
        operations[0xe1] = new OpPop16Reg(processor, processor.register("hl"));
        operations[0xe2] = new OpJpConditional(processor, FlagsRegister.Flag.P, false);
        operations[0xe3] = new OpExSpIndirectHl(processor, memory);
        operations[0xe4] = new OpCallConditional(processor, FlagsRegister.Flag.P, false);
        operations[0xe5] = new OpPush16Reg(processor, processor.register("hl"));
        operations[0xe6] = new OpAndAImmediate(processor);
        operations[0xe7] = new OpRst(processor, 0x20);
        operations[0xe8] = new OpRetConditional(processor, FlagsRegister.Flag.P, true);
        operations[0xe9] = new OpJpHlIndirect(processor);
        operations[0xea] = new OpJpConditional(processor, FlagsRegister.Flag.P, true);
        operations[0xeb] = new OpExRegister(processor.register("de"), processor.register("hl"));
        operations[0xec] = new OpCallConditional(processor, FlagsRegister.Flag.P, true);
        operations[0xee] = new OpXorAImmediate(processor);
        operations[0xef] = new OpRst(processor, 0x28);

        operations[0xf0] = new OpRetConditional(processor, FlagsRegister.Flag.S, false);
        operations[0xf1] = new OpPop16Reg(processor, processor.register("af"));
        operations[0xf2] = new OpJpConditional(processor, FlagsRegister.Flag.S, false);
        operations[0xf3] = new OpDi(processor);
        operations[0xf4] = new OpCallConditional(processor, FlagsRegister.Flag.S, false);
        operations[0xf5] = new OpPush16Reg(processor, processor.register("af"));
        operations[0xf6] = new OpOrAImmediate(processor);
        operations[0xf7] = new OpRst(processor, 0x30);
        operations[0xf8] = new OpRetConditional(processor, FlagsRegister.Flag.S, true);
        operations[0xf9] = new OpLdSpHl(processor);
        operations[0xfa] = new OpJpConditional(processor, FlagsRegister.Flag.S, true);
        operations[0xfb] = new OpEi(processor);
        operations[0xfc] = new OpCallConditional(processor, FlagsRegister.Flag.S, true);
        operations[0xfe] = new OpCpImmediate(processor);
        operations[0xff] = new OpRst(processor, 0x38);

        operations[0xcb] = new GroupCb(processor, memory);
        operations[0xed] = new GroupEd(processor, memory, io);
        operations[0xdd] = new GroupDdFd(processor, memory, IndexRegister.class.cast(processor.register("ix")));
        operations[0xfd] = new GroupDdFd(processor, memory, IndexRegister.class.cast(processor.register("iy")));
        
        return operations;
    }
}
