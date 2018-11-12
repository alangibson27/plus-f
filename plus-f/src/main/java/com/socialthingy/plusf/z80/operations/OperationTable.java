package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OperationTable {
    private OperationTable() {}
    
    public static Operation[] build(
        final Processor processor,
        final Clock clock,
        final Memory memory,
        final IO io
    ) {
        final Operation[] operations = new Operation[0x100];
        operations[0x00] = new Nop(clock);

        operations[0x01] = new OpLd16RegImmediate(processor, clock, processor.register("bc"));
        operations[0x02] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("bc"), processor.register("a"));
        operations[0x03] = new OpInc16Reg(clock, processor.register("bc"));
        operations[0x04] = new OpInc8Reg(processor, clock, processor.register("b"));
        operations[0x05] = new OpDec8Reg(processor, clock, processor.register("b"));
        operations[0x06] = new OpLd8RegImmediate(processor, clock, processor.register("b"));
        operations[0x07] = new OpRlca(processor, clock);
        operations[0x08] = new OpExRegister(clock, processor.register("af"), processor.register("af'"));
        operations[0x09] = new OpAddHl16Reg(processor, clock, processor.register("bc"));
        operations[0x0a] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("a"), processor.register("bc"));
        operations[0x0b] = new OpDec16Reg(clock, processor.register("bc"));
        operations[0x0c] = new OpInc8Reg(processor, clock, processor.register("c"));
        operations[0x0d] = new OpDec8Reg(processor, clock, processor.register("c"));
        operations[0x0e] = new OpLd8RegImmediate(processor, clock, processor.register("c"));
        operations[0x0f] = new OpRrca(processor, clock);

        operations[0x10] = new OpDjnz(processor, clock);
        operations[0x11] = new OpLd16RegImmediate(processor, clock, processor.register("de"));
        operations[0x12] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("de"), processor.register("a"));
        operations[0x13] = new OpInc16Reg(clock, processor.register("de"));
        operations[0x14] = new OpInc8Reg(processor, clock, processor.register("d"));
        operations[0x15] = new OpDec8Reg(processor, clock, processor.register("d"));
        operations[0x16] = new OpLd8RegImmediate(processor, clock, processor.register("d"));
        operations[0x17] = new OpRla(processor, clock);
        operations[0x18] = new OpJr(processor, clock);
        operations[0x19] = new OpAddHl16Reg(processor, clock, processor.register("de"));
        operations[0x1a] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("a"), processor.register("de"));
        operations[0x1b] = new OpDec16Reg(clock, processor.register("de"));
        operations[0x1c] = new OpInc8Reg(processor, clock, processor.register("e"));
        operations[0x1d] = new OpDec8Reg(processor, clock, processor.register("e"));
        operations[0x1e] = new OpLd8RegImmediate(processor, clock, processor.register("e"));
        operations[0x1f] = new OpRra(processor, clock);

        operations[0x20] = new OpJrConditional(processor, clock, FlagsRegister.Flag.Z, false);
        operations[0x21] = new OpLd16RegImmediate(processor, clock, processor.register("hl"));
        operations[0x22] = new OpLdAddressHl(processor, clock, memory);
        operations[0x23] = new OpInc16Reg(clock, processor.register("hl"));
        operations[0x24] = new OpInc8Reg(processor, clock, processor.register("h"));
        operations[0x25] = new OpDec8Reg(processor, clock, processor.register("h"));
        operations[0x26] = new OpLd8RegImmediate(processor, clock, processor.register("h"));
        operations[0x27] = new OpDaa(processor, clock);
        operations[0x28] = new OpJrConditional(processor, clock, FlagsRegister.Flag.Z, true);
        operations[0x29] = new OpAddHl16Reg(processor, clock, processor.register("hl"));
        operations[0x2a] = new OpLdHlAddress(processor, clock, memory);
        operations[0x2b] = new OpDec16Reg(clock, processor.register("hl"));
        operations[0x2c] = new OpInc8Reg(processor, clock, processor.register("l"));
        operations[0x2d] = new OpDec8Reg(processor, clock, processor.register("l"));
        operations[0x2e] = new OpLd8RegImmediate(processor, clock, processor.register("l"));
        operations[0x2f] = new OpCpl(processor, clock);

        operations[0x30] = new OpJrConditional(processor, clock, FlagsRegister.Flag.C, false);
        operations[0x31] = new OpLd16RegImmediate(processor, clock, processor.register("sp"));
        operations[0x32] = new OpLdAddressA(processor, clock, memory);
        operations[0x33] = new OpInc16Reg(clock, processor.register("sp"));
        operations[0x34] = new OpIncHlIndirect(processor, clock, memory);
        operations[0x35] = new OpDecHlIndirect(processor, clock, memory);
        operations[0x36] = new OpLdHlIndirectImmediate(processor, clock, memory);
        operations[0x37] = new OpScf(processor, clock);
        operations[0x38] = new OpJrConditional(processor, clock, FlagsRegister.Flag.C, true);
        operations[0x39] = new OpAddHl16Reg(processor, clock, processor.register("sp"));
        operations[0x3a] = new OpLdAAddress(processor, clock, memory);
        operations[0x3b] = new OpDec16Reg(clock, processor.register("sp"));
        operations[0x3c] = new OpInc8Reg(processor, clock, processor.register("a"));
        operations[0x3d] = new OpDec8Reg(processor, clock, processor.register("a"));
        operations[0x3e] = new OpLd8RegImmediate(processor, clock, processor.register("a"));
        operations[0x3f] = new OpCcf(processor, clock);

        operations[0x40] = new OpLd8RegFrom8Reg(clock, processor.register("b"), processor.register("b"));
        operations[0x41] = new OpLd8RegFrom8Reg(clock, processor.register("b"), processor.register("c"));
        operations[0x42] = new OpLd8RegFrom8Reg(clock, processor.register("b"), processor.register("d"));
        operations[0x43] = new OpLd8RegFrom8Reg(clock, processor.register("b"), processor.register("e"));
        operations[0x44] = new OpLd8RegFrom8Reg(clock, processor.register("b"), processor.register("h"));
        operations[0x45] = new OpLd8RegFrom8Reg(clock, processor.register("b"), processor.register("l"));
        operations[0x46] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("b"), processor.register("hl"));
        operations[0x47] = new OpLd8RegFrom8Reg(clock, processor.register("b"), processor.register("a"));
        operations[0x48] = new OpLd8RegFrom8Reg(clock, processor.register("c"), processor.register("b"));
        operations[0x49] = new OpLd8RegFrom8Reg(clock, processor.register("c"), processor.register("c"));
        operations[0x4a] = new OpLd8RegFrom8Reg(clock, processor.register("c"), processor.register("d"));
        operations[0x4b] = new OpLd8RegFrom8Reg(clock, processor.register("c"), processor.register("e"));
        operations[0x4c] = new OpLd8RegFrom8Reg(clock, processor.register("c"), processor.register("h"));
        operations[0x4d] = new OpLd8RegFrom8Reg(clock, processor.register("c"), processor.register("l"));
        operations[0x4e] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("c"), processor.register("hl"));
        operations[0x4f] = new OpLd8RegFrom8Reg(clock, processor.register("c"), processor.register("a"));

        operations[0x50] = new OpLd8RegFrom8Reg(clock, processor.register("d"), processor.register("b"));
        operations[0x51] = new OpLd8RegFrom8Reg(clock, processor.register("d"), processor.register("c"));
        operations[0x52] = new OpLd8RegFrom8Reg(clock, processor.register("d"), processor.register("d"));
        operations[0x53] = new OpLd8RegFrom8Reg(clock, processor.register("d"), processor.register("e"));
        operations[0x54] = new OpLd8RegFrom8Reg(clock, processor.register("d"), processor.register("h"));
        operations[0x55] = new OpLd8RegFrom8Reg(clock, processor.register("d"), processor.register("l"));
        operations[0x56] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("d"), processor.register("hl"));
        operations[0x57] = new OpLd8RegFrom8Reg(clock, processor.register("d"), processor.register("a"));
        operations[0x58] = new OpLd8RegFrom8Reg(clock, processor.register("e"), processor.register("b"));
        operations[0x59] = new OpLd8RegFrom8Reg(clock, processor.register("e"), processor.register("c"));
        operations[0x5a] = new OpLd8RegFrom8Reg(clock, processor.register("e"), processor.register("d"));
        operations[0x5b] = new OpLd8RegFrom8Reg(clock, processor.register("e"), processor.register("e"));
        operations[0x5c] = new OpLd8RegFrom8Reg(clock, processor.register("e"), processor.register("h"));
        operations[0x5d] = new OpLd8RegFrom8Reg(clock, processor.register("e"), processor.register("l"));
        operations[0x5e] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("e"), processor.register("hl"));
        operations[0x5f] = new OpLd8RegFrom8Reg(clock, processor.register("e"), processor.register("a"));

        operations[0x60] = new OpLd8RegFrom8Reg(clock, processor.register("h"), processor.register("b"));
        operations[0x61] = new OpLd8RegFrom8Reg(clock, processor.register("h"), processor.register("c"));
        operations[0x62] = new OpLd8RegFrom8Reg(clock, processor.register("h"), processor.register("d"));
        operations[0x63] = new OpLd8RegFrom8Reg(clock, processor.register("h"), processor.register("e"));
        operations[0x64] = new OpLd8RegFrom8Reg(clock, processor.register("h"), processor.register("h"));
        operations[0x65] = new OpLd8RegFrom8Reg(clock, processor.register("h"), processor.register("l"));
        operations[0x66] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("h"), processor.register("hl"));
        operations[0x67] = new OpLd8RegFrom8Reg(clock, processor.register("h"), processor.register("a"));
        operations[0x68] = new OpLd8RegFrom8Reg(clock, processor.register("l"), processor.register("b"));
        operations[0x69] = new OpLd8RegFrom8Reg(clock, processor.register("l"), processor.register("c"));
        operations[0x6a] = new OpLd8RegFrom8Reg(clock, processor.register("l"), processor.register("d"));
        operations[0x6b] = new OpLd8RegFrom8Reg(clock, processor.register("l"), processor.register("e"));
        operations[0x6c] = new OpLd8RegFrom8Reg(clock, processor.register("l"), processor.register("h"));
        operations[0x6d] = new OpLd8RegFrom8Reg(clock, processor.register("l"), processor.register("l"));
        operations[0x6e] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("l"), processor.register("hl"));
        operations[0x6f] = new OpLd8RegFrom8Reg(clock, processor.register("l"), processor.register("a"));

        operations[0x70] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("hl"), processor.register("b"));
        operations[0x71] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("hl"), processor.register("c"));
        operations[0x72] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("hl"), processor.register("d"));
        operations[0x73] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("hl"), processor.register("e"));
        operations[0x74] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("hl"), processor.register("h"));
        operations[0x75] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("hl"), processor.register("l"));
        operations[0x76] = new OpHalt(processor, clock);
        operations[0x77] = new OpLd16RegIndirectFrom8Reg(clock, memory, processor.register("hl"), processor.register("a"));
        operations[0x78] = new OpLd8RegFrom8Reg(clock, processor.register("a"), processor.register("b"));
        operations[0x79] = new OpLd8RegFrom8Reg(clock, processor.register("a"), processor.register("c"));
        operations[0x7a] = new OpLd8RegFrom8Reg(clock, processor.register("a"), processor.register("d"));
        operations[0x7b] = new OpLd8RegFrom8Reg(clock, processor.register("a"), processor.register("e"));
        operations[0x7c] = new OpLd8RegFrom8Reg(clock, processor.register("a"), processor.register("h"));
        operations[0x7d] = new OpLd8RegFrom8Reg(clock, processor.register("a"), processor.register("l"));
        operations[0x7e] = new OpLd8RegFrom16RegIndirect(clock, memory, processor.register("a"), processor.register("hl"));
        operations[0x7f] = new OpLd8RegFrom8Reg(clock, processor.register("a"), processor.register("a"));

        operations[0x80] = new OpAddA8Reg(processor, clock, processor.register("b"), false);
        operations[0x81] = new OpAddA8Reg(processor, clock, processor.register("c"), false);
        operations[0x82] = new OpAddA8Reg(processor, clock, processor.register("d"), false);
        operations[0x83] = new OpAddA8Reg(processor, clock, processor.register("e"), false);
        operations[0x84] = new OpAddA8Reg(processor, clock, processor.register("h"), false);
        operations[0x85] = new OpAddA8Reg(processor, clock, processor.register("l"), false);
        operations[0x86] = new OpAddAHlIndirect(processor, clock, memory, false);
        operations[0x87] = new OpAddA8Reg(processor, clock, processor.register("a"), false);
        operations[0x88] = new OpAddA8Reg(processor, clock, processor.register("b"), true);
        operations[0x89] = new OpAddA8Reg(processor, clock, processor.register("c"), true);
        operations[0x8a] = new OpAddA8Reg(processor, clock, processor.register("d"), true);
        operations[0x8b] = new OpAddA8Reg(processor, clock, processor.register("e"), true);
        operations[0x8c] = new OpAddA8Reg(processor, clock, processor.register("h"), true);
        operations[0x8d] = new OpAddA8Reg(processor, clock, processor.register("l"), true);
        operations[0x8e] = new OpAddAHlIndirect(processor, clock, memory, true);
        operations[0x8f] = new OpAddA8Reg(processor, clock, processor.register("a"), true);

        operations[0x90] = new OpSubA8Reg(processor, clock, processor.register("b"), false);
        operations[0x91] = new OpSubA8Reg(processor, clock, processor.register("c"), false);
        operations[0x92] = new OpSubA8Reg(processor, clock, processor.register("d"), false);
        operations[0x93] = new OpSubA8Reg(processor, clock, processor.register("e"), false);
        operations[0x94] = new OpSubA8Reg(processor, clock, processor.register("h"), false);
        operations[0x95] = new OpSubA8Reg(processor, clock, processor.register("l"), false);
        operations[0x96] = new OpSubAHlIndirect(processor, clock, memory, false);
        operations[0x97] = new OpSubA8Reg(processor, clock, processor.register("a"), false);
        operations[0x98] = new OpSubA8Reg(processor, clock, processor.register("b"), true);
        operations[0x99] = new OpSubA8Reg(processor, clock, processor.register("c"), true);
        operations[0x9a] = new OpSubA8Reg(processor, clock, processor.register("d"), true);
        operations[0x9b] = new OpSubA8Reg(processor, clock, processor.register("e"), true);
        operations[0x9c] = new OpSubA8Reg(processor, clock, processor.register("h"), true);
        operations[0x9d] = new OpSubA8Reg(processor, clock, processor.register("l"), true);
        operations[0x9e] = new OpSubAHlIndirect(processor, clock, memory, true);
        operations[0x9f] = new OpSubA8Reg(processor, clock, processor.register("a"), true);

        operations[0xa0] = new OpAndA8Reg(processor, clock, processor.register("b"));
        operations[0xa1] = new OpAndA8Reg(processor, clock, processor.register("c"));
        operations[0xa2] = new OpAndA8Reg(processor, clock, processor.register("d"));
        operations[0xa3] = new OpAndA8Reg(processor, clock, processor.register("e"));
        operations[0xa4] = new OpAndA8Reg(processor, clock, processor.register("h"));
        operations[0xa5] = new OpAndA8Reg(processor, clock, processor.register("l"));
        operations[0xa6] = new OpAndAHlIndirect(processor, clock, memory);
        operations[0xa7] = new OpAndA8Reg(processor, clock, processor.register("a"));
        operations[0xa8] = new OpXorA8Reg(processor, clock, processor.register("b"));
        operations[0xa9] = new OpXorA8Reg(processor, clock, processor.register("c"));
        operations[0xaa] = new OpXorA8Reg(processor, clock, processor.register("d"));
        operations[0xab] = new OpXorA8Reg(processor, clock, processor.register("e"));
        operations[0xac] = new OpXorA8Reg(processor, clock, processor.register("h"));
        operations[0xad] = new OpXorA8Reg(processor, clock, processor.register("l"));
        operations[0xae] = new OpXorAHlIndirect(processor, clock, memory);
        operations[0xaf] = new OpXorA8Reg(processor, clock, processor.register("a"));

        operations[0xb0] = new OpOrA8Reg(processor, clock, processor.register("b"));
        operations[0xb1] = new OpOrA8Reg(processor, clock, processor.register("c"));
        operations[0xb2] = new OpOrA8Reg(processor, clock, processor.register("d"));
        operations[0xb3] = new OpOrA8Reg(processor, clock, processor.register("e"));
        operations[0xb4] = new OpOrA8Reg(processor, clock, processor.register("h"));
        operations[0xb5] = new OpOrA8Reg(processor, clock, processor.register("l"));
        operations[0xb6] = new OpOrAHlIndirect(processor, clock, memory);
        operations[0xb7] = new OpOrA8Reg(processor, clock, processor.register("a"));
        operations[0xb8] = new OpCpA8Reg(processor, clock, processor.register("b"));
        operations[0xb9] = new OpCpA8Reg(processor, clock, processor.register("c"));
        operations[0xba] = new OpCpA8Reg(processor, clock, processor.register("d"));
        operations[0xbb] = new OpCpA8Reg(processor, clock, processor.register("e"));
        operations[0xbc] = new OpCpA8Reg(processor, clock, processor.register("h"));
        operations[0xbd] = new OpCpA8Reg(processor, clock, processor.register("l"));
        operations[0xbe] = new OpCpAHlIndirect(processor, clock, memory);
        operations[0xbf] = new OpCpA8Reg(processor, clock, processor.register("a"));

        operations[0xc0] = new OpRetConditional(processor, clock, FlagsRegister.Flag.Z, false);
        operations[0xc1] = new OpPop16Reg(processor, clock, processor.register("bc"));
        operations[0xc2] = new OpJpConditional(processor, clock, FlagsRegister.Flag.Z, false);
        operations[0xc3] = new OpJp(processor, clock);
        operations[0xc4] = new OpCallConditional(processor, clock, FlagsRegister.Flag.Z, false);
        operations[0xc5] = new OpPush16Reg(processor, clock, processor.register("bc"));
        operations[0xc6] = new OpAddAImmediate(processor, clock, false);
        operations[0xc7] = new OpRst(processor, clock, 0x00);
        operations[0xc8] = new OpRetConditional(processor, clock, FlagsRegister.Flag.Z, true);
        operations[0xc9] = new OpRet(processor, clock);
        operations[0xca] = new OpJpConditional(processor, clock, FlagsRegister.Flag.Z, true);
        operations[0xcc] = new OpCallConditional(processor, clock, FlagsRegister.Flag.Z, true);
        operations[0xcd] = new OpCall(processor, clock);
        operations[0xce] = new OpAddAImmediate(processor, clock, true);
        operations[0xcf] = new OpRst(processor, clock, 0x08);

        operations[0xd0] = new OpRetConditional(processor, clock, FlagsRegister.Flag.C, false);
        operations[0xd1] = new OpPop16Reg(processor, clock, processor.register("de"));
        operations[0xd2] = new OpJpConditional(processor, clock, FlagsRegister.Flag.C, false);
        operations[0xd3] = new OpOutA(processor, io, clock);
        operations[0xd4] = new OpCallConditional(processor, clock, FlagsRegister.Flag.C, false);
        operations[0xd5] = new OpPush16Reg(processor, clock, processor.register("de"));
        operations[0xd6] = new OpSubAImmediate(processor, clock, false);
        operations[0xd7] = new OpRst(processor, clock, 0x10);
        operations[0xd8] = new OpRetConditional(processor, clock, FlagsRegister.Flag.C, true);
        operations[0xd9] = new OpExx(processor, clock);
        operations[0xda] = new OpJpConditional(processor, clock, FlagsRegister.Flag.C, true);
        operations[0xdb] = new OpInA(processor, clock, io);
        operations[0xdc] = new OpCallConditional(processor, clock, FlagsRegister.Flag.C, true);
        operations[0xde] = new OpSubAImmediate(processor, clock, true);
        operations[0xdf] = new OpRst(processor, clock, 0x18);

        operations[0xe0] = new OpRetConditional(processor, clock, FlagsRegister.Flag.P, false);
        operations[0xe1] = new OpPop16Reg(processor, clock, processor.register("hl"));
        operations[0xe2] = new OpJpConditional(processor, clock, FlagsRegister.Flag.P, false);
        operations[0xe3] = new OpExSpIndirectHl(processor, clock, memory);
        operations[0xe4] = new OpCallConditional(processor, clock, FlagsRegister.Flag.P, false);
        operations[0xe5] = new OpPush16Reg(processor, clock, processor.register("hl"));
        operations[0xe6] = new OpAndAImmediate(processor, clock);
        operations[0xe7] = new OpRst(processor, clock, 0x20);
        operations[0xe8] = new OpRetConditional(processor, clock, FlagsRegister.Flag.P, true);
        operations[0xe9] = new OpJpHlIndirect(processor, clock);
        operations[0xea] = new OpJpConditional(processor, clock, FlagsRegister.Flag.P, true);
        operations[0xeb] = new OpExRegister(clock, processor.register("de"), processor.register("hl"));
        operations[0xec] = new OpCallConditional(processor, clock, FlagsRegister.Flag.P, true);
        operations[0xee] = new OpXorAImmediate(processor, clock);
        operations[0xef] = new OpRst(processor, clock, 0x28);

        operations[0xf0] = new OpRetConditional(processor, clock, FlagsRegister.Flag.S, false);
        operations[0xf1] = new OpPop16Reg(processor, clock, processor.register("af"));
        operations[0xf2] = new OpJpConditional(processor, clock, FlagsRegister.Flag.S, false);
        operations[0xf3] = new OpDi(processor, clock);
        operations[0xf4] = new OpCallConditional(processor, clock, FlagsRegister.Flag.S, false);
        operations[0xf5] = new OpPush16Reg(processor, clock, processor.register("af"));
        operations[0xf6] = new OpOrAImmediate(processor, clock);
        operations[0xf7] = new OpRst(processor, clock, 0x30);
        operations[0xf8] = new OpRetConditional(processor, clock, FlagsRegister.Flag.S, true);
        operations[0xf9] = new OpLdSpHl(processor, clock);
        operations[0xfa] = new OpJpConditional(processor, clock, FlagsRegister.Flag.S, true);
        operations[0xfb] = new OpEi(processor, clock);
        operations[0xfc] = new OpCallConditional(processor, clock, FlagsRegister.Flag.S, true);
        operations[0xfe] = new OpCpImmediate(processor, clock);
        operations[0xff] = new OpRst(processor, clock, 0x38);

        return operations;
    }

    public static Operation[] buildEdGroup(
        final Processor processor,
        final Clock clock,
        final Memory memory,
        final IO io
    ) {
        final Operation[] operations = new Operation[0x100];

        final Operation opNeg = new OpNeg(processor, clock);
        final OpRetn opRetn = new OpRetn(processor, clock);
        final OpIm opIm1 = new OpIm(processor, clock, 1);
        final OpIm opIm2 = new OpIm(processor, clock, 2);
        final OpIm opIm0 = new OpIm(processor, clock, 0);
        final OpIn8RegC opInAC = new OpIn8RegC(processor, clock, io, processor.register("a"));

        operations[0x40] = new OpIn8RegC(processor, clock, io, processor.register("b"));
        operations[0x41] = new OpOutC8Reg(processor, io, clock, processor.register("b"));
        operations[0x42] = new OpSbcHl16Reg(processor, clock, processor.register("bc"));
        operations[0x43] = new OpLdAddress16Reg(processor, clock, memory, processor.register("bc"));
        operations[0x44] = opNeg;
        operations[0x45] = opRetn;
        operations[0x46] = opIm0;
        operations[0x47] = new OpLdIA(processor, clock);
        operations[0x48] = new OpIn8RegC(processor, clock, io, processor.register("c"));
        operations[0x49] = new OpOutC8Reg(processor, io, clock, processor.register("c"));
        operations[0x4a] = new OpAdcHl16Reg(processor, clock, processor.register("bc"));
        operations[0x4b] = new OpLd16RegAddress(processor, clock, memory, processor.register("bc"));
        operations[0x4c] = opNeg;
        operations[0x4d] = new OpReti(processor, clock);
        operations[0x4e] = opIm0;
        operations[0x4f] = new OpLdRA(processor, clock);

        operations[0x50] = new OpIn8RegC(processor, clock, io, processor.register("d"));
        operations[0x51] = new OpOutC8Reg(processor, io, clock, processor.register("d"));
        operations[0x52] = new OpSbcHl16Reg(processor, clock, processor.register("de"));
        operations[0x53] = new OpLdAddress16Reg(processor, clock, memory, processor.register("de"));
        operations[0x54] = opNeg;
        operations[0x55] = opRetn;
        operations[0x56] = opIm1;
        operations[0x57] = new OpLdAI(processor, clock);
        operations[0x58] = new OpIn8RegC(processor, clock, io, processor.register("e"));
        operations[0x59] = new OpOutC8Reg(processor, io, clock, processor.register("e"));
        operations[0x5a] = new OpAdcHl16Reg(processor, clock, processor.register("de"));
        operations[0x5b] = new OpLd16RegAddress(processor, clock, memory, processor.register("de"));
        operations[0x5c] = opNeg;
        operations[0x5d] = opRetn;
        operations[0x5e] = opIm2;
        operations[0x5f] = new OpLdAR(processor, clock);

        operations[0x60] = new OpIn8RegC(processor, clock, io, processor.register("h"));
        operations[0x61] = new OpOutC8Reg(processor, io, clock, processor.register("h"));
        operations[0x62] = new OpSbcHl16Reg(processor, clock, processor.register("hl"));
        operations[0x63] = new OpLdAddress16Reg(processor, clock, memory, processor.register("hl"));
        operations[0x64] = opNeg;
        operations[0x65] = opRetn;
        operations[0x66] = opIm0;
        operations[0x67] = new OpRrd(processor, clock, memory);
        operations[0x68] = new OpIn8RegC(processor, clock, io, processor.register("l"));
        operations[0x69] = new OpOutC8Reg(processor, io, clock, processor.register("l"));
        operations[0x6a] = new OpAdcHl16Reg(processor, clock, processor.register("hl"));
        operations[0x6b] = new OpLd16RegAddress(processor, clock, memory, processor.register("hl"));
        operations[0x6c] = opNeg;
        operations[0x6d] = opRetn;
        operations[0x6e] = opIm0;
        operations[0x6f] = new OpRld(processor, clock, memory);

        operations[0x70] = new OpInFlagsC(processor, clock, io);
        operations[0x71] = new OpOutCZero(processor, clock, io);
        operations[0x72] = new OpSbcHl16Reg(processor, clock, processor.register("sp"));
        operations[0x73] = new OpLdAddress16Reg(processor, clock, memory, processor.register("sp"));
        operations[0x74] = opNeg;
        operations[0x75] = opRetn;
        operations[0x76] = opIm1;
        operations[0x78] = opInAC;
        operations[0x79] = new OpOutC8Reg(processor, io, clock, processor.register("a"));
        operations[0x7a] = new OpAdcHl16Reg(processor, clock, processor.register("sp"));
        operations[0x7b] = new OpLd16RegAddress(processor, clock, memory, processor.register("sp"));
        operations[0x7c] = opNeg;
        operations[0x7d] = opRetn;
        operations[0x7e] = opIm2;

        operations[0xa0] = new OpLdi(processor, clock, memory);
        operations[0xa1] = new OpCpi(processor, clock, memory);
        operations[0xa2] = new OpIni(processor, clock, memory, io);
        operations[0xa3] = new OpOuti(processor, clock, memory, io);
        operations[0xa8] = new OpLdd(processor, clock, memory);
        operations[0xa9] = new OpCpd(processor, clock, memory);
        operations[0xaa] = new OpInd(processor, clock, memory, io);
        operations[0xab] = new OpOutd(processor, clock, memory, io);

        operations[0xb0] = new OpLdir(processor, clock, memory);
        operations[0xb1] = new OpCpir(processor, clock, memory);
        operations[0xb2] = new OpInir(processor, clock, memory, io);
        operations[0xb3] = new OpOtir(processor, clock, memory, io);
        operations[0xb8] = new OpLddr(processor, clock, memory);
        operations[0xb9] = new OpCpdr(processor, clock, memory);
        operations[0xba] = new OpIndr(processor, clock, memory, io);
        operations[0xbb] = new OpOtdr(processor, clock, memory, io);

        return operations;
    }

    public static Operation[] buildCbGroup(
        final Processor processor,
        final Clock clock,
        final Memory memory
    ) {
        final Operation[] operations = new Operation[0x100];

        operations[0x00] = new OpRlcReg(processor, clock, processor.register("b"));
        operations[0x01] = new OpRlcReg(processor, clock, processor.register("c"));
        operations[0x02] = new OpRlcReg(processor, clock, processor.register("d"));
        operations[0x03] = new OpRlcReg(processor, clock, processor.register("e"));
        operations[0x04] = new OpRlcReg(processor, clock, processor.register("h"));
        operations[0x05] = new OpRlcReg(processor, clock, processor.register("l"));
        operations[0x06] = new OpRlcHlIndirect(processor, clock, memory);
        operations[0x07] = new OpRlcReg(processor, clock, processor.register("a"));
        operations[0x08] = new OpRrcReg(processor, clock, processor.register("b"));
        operations[0x09] = new OpRrcReg(processor, clock, processor.register("c"));
        operations[0x0a] = new OpRrcReg(processor, clock, processor.register("d"));
        operations[0x0b] = new OpRrcReg(processor, clock, processor.register("e"));
        operations[0x0c] = new OpRrcReg(processor, clock, processor.register("h"));
        operations[0x0d] = new OpRrcReg(processor, clock, processor.register("l"));
        operations[0x0e] = new OpRrcHlIndirect(processor, clock, memory);
        operations[0x0f] = new OpRrcReg(processor, clock, processor.register("a"));

        operations[0x10] = new OpRlReg(processor, clock, processor.register("b"));
        operations[0x11] = new OpRlReg(processor, clock, processor.register("c"));
        operations[0x12] = new OpRlReg(processor, clock, processor.register("d"));
        operations[0x13] = new OpRlReg(processor, clock, processor.register("e"));
        operations[0x14] = new OpRlReg(processor, clock, processor.register("h"));
        operations[0x15] = new OpRlReg(processor, clock, processor.register("l"));
        operations[0x16] = new OpRlHlIndirect(processor, clock, memory);
        operations[0x17] = new OpRlReg(processor, clock, processor.register("a"));
        operations[0x18] = new OpRrReg(processor, clock, processor.register("b"));
        operations[0x19] = new OpRrReg(processor, clock, processor.register("c"));
        operations[0x1a] = new OpRrReg(processor, clock, processor.register("d"));
        operations[0x1b] = new OpRrReg(processor, clock, processor.register("e"));
        operations[0x1c] = new OpRrReg(processor, clock, processor.register("h"));
        operations[0x1d] = new OpRrReg(processor, clock, processor.register("l"));
        operations[0x1e] = new OpRrHlIndirect(processor, clock, memory);
        operations[0x1f] = new OpRrReg(processor, clock, processor.register("a"));

        operations[0x20] = new OpSlaReg(processor, clock, processor.register("b"));
        operations[0x21] = new OpSlaReg(processor, clock, processor.register("c"));
        operations[0x22] = new OpSlaReg(processor, clock, processor.register("d"));
        operations[0x23] = new OpSlaReg(processor, clock, processor.register("e"));
        operations[0x24] = new OpSlaReg(processor, clock, processor.register("h"));
        operations[0x25] = new OpSlaReg(processor, clock, processor.register("l"));
        operations[0x26] = new OpSlaHlIndirect(processor, clock, memory);
        operations[0x27] = new OpSlaReg(processor, clock, processor.register("a"));
        operations[0x28] = new OpSraReg(processor, clock, processor.register("b"));
        operations[0x29] = new OpSraReg(processor, clock, processor.register("c"));
        operations[0x2a] = new OpSraReg(processor, clock, processor.register("d"));
        operations[0x2b] = new OpSraReg(processor, clock, processor.register("e"));
        operations[0x2c] = new OpSraReg(processor, clock, processor.register("h"));
        operations[0x2d] = new OpSraReg(processor, clock, processor.register("l"));
        operations[0x2e] = new OpSraHlIndirect(processor, clock, memory);
        operations[0x2f] = new OpSraReg(processor, clock, processor.register("a"));

        operations[0x30] = new OpSllReg(processor, clock, processor.register("b"));
        operations[0x31] = new OpSllReg(processor, clock, processor.register("c"));
        operations[0x32] = new OpSllReg(processor, clock, processor.register("d"));
        operations[0x33] = new OpSllReg(processor, clock, processor.register("e"));
        operations[0x34] = new OpSllReg(processor, clock, processor.register("h"));
        operations[0x35] = new OpSllReg(processor, clock, processor.register("l"));
        operations[0x36] = new OpSllHlIndirect(processor, clock, memory);
        operations[0x37] = new OpSllReg(processor, clock, processor.register("a"));
        operations[0x38] = new OpSrlReg(processor, clock, processor.register("b"));
        operations[0x39] = new OpSrlReg(processor, clock, processor.register("c"));
        operations[0x3a] = new OpSrlReg(processor, clock, processor.register("d"));
        operations[0x3b] = new OpSrlReg(processor, clock, processor.register("e"));
        operations[0x3c] = new OpSrlReg(processor, clock, processor.register("h"));
        operations[0x3d] = new OpSrlReg(processor, clock, processor.register("l"));
        operations[0x3e] = new OpSrlHlIndirect(processor, clock, memory);
        operations[0x3f] = new OpSrlReg(processor, clock, processor.register("a"));

        operations[0x40] = new OpBitReg(processor, clock, processor.register("b"), 0);
        operations[0x41] = new OpBitReg(processor, clock, processor.register("c"), 0);
        operations[0x42] = new OpBitReg(processor, clock, processor.register("d"), 0);
        operations[0x43] = new OpBitReg(processor, clock, processor.register("e"), 0);
        operations[0x44] = new OpBitReg(processor, clock, processor.register("h"), 0);
        operations[0x45] = new OpBitReg(processor, clock, processor.register("l"), 0);
        operations[0x46] = new OpBitHlIndirect(processor, clock, memory, 0);
        operations[0x47] = new OpBitReg(processor, clock, processor.register("a"), 0);
        operations[0x48] = new OpBitReg(processor, clock, processor.register("b"), 1);
        operations[0x49] = new OpBitReg(processor, clock, processor.register("c"), 1);
        operations[0x4a] = new OpBitReg(processor, clock, processor.register("d"), 1);
        operations[0x4b] = new OpBitReg(processor, clock, processor.register("e"), 1);
        operations[0x4c] = new OpBitReg(processor, clock, processor.register("h"), 1);
        operations[0x4d] = new OpBitReg(processor, clock, processor.register("l"), 1);
        operations[0x4e] = new OpBitHlIndirect(processor, clock, memory, 1);
        operations[0x4f] = new OpBitReg(processor, clock, processor.register("a"), 1);

        operations[0x50] = new OpBitReg(processor, clock, processor.register("b"), 2);
        operations[0x51] = new OpBitReg(processor, clock, processor.register("c"), 2);
        operations[0x52] = new OpBitReg(processor, clock, processor.register("d"), 2);
        operations[0x53] = new OpBitReg(processor, clock, processor.register("e"), 2);
        operations[0x54] = new OpBitReg(processor, clock, processor.register("h"), 2);
        operations[0x55] = new OpBitReg(processor, clock, processor.register("l"), 2);
        operations[0x56] = new OpBitHlIndirect(processor, clock, memory, 2);
        operations[0x57] = new OpBitReg(processor, clock, processor.register("a"), 2);
        operations[0x58] = new OpBitReg(processor, clock, processor.register("b"), 3);
        operations[0x59] = new OpBitReg(processor, clock, processor.register("c"), 3);
        operations[0x5a] = new OpBitReg(processor, clock, processor.register("d"), 3);
        operations[0x5b] = new OpBitReg(processor, clock, processor.register("e"), 3);
        operations[0x5c] = new OpBitReg(processor, clock, processor.register("h"), 3);
        operations[0x5d] = new OpBitReg(processor, clock, processor.register("l"), 3);
        operations[0x5e] = new OpBitHlIndirect(processor, clock, memory, 3);
        operations[0x5f] = new OpBitReg(processor, clock, processor.register("a"), 3);

        operations[0x60] = new OpBitReg(processor, clock, processor.register("b"), 4);
        operations[0x61] = new OpBitReg(processor, clock, processor.register("c"), 4);
        operations[0x62] = new OpBitReg(processor, clock, processor.register("d"), 4);
        operations[0x63] = new OpBitReg(processor, clock, processor.register("e"), 4);
        operations[0x64] = new OpBitReg(processor, clock, processor.register("h"), 4);
        operations[0x65] = new OpBitReg(processor, clock, processor.register("l"), 4);
        operations[0x66] = new OpBitHlIndirect(processor, clock, memory, 4);
        operations[0x67] = new OpBitReg(processor, clock, processor.register("a"), 4);
        operations[0x68] = new OpBitReg(processor, clock, processor.register("b"), 5);
        operations[0x69] = new OpBitReg(processor, clock, processor.register("c"), 5);
        operations[0x6a] = new OpBitReg(processor, clock, processor.register("d"), 5);
        operations[0x6b] = new OpBitReg(processor, clock, processor.register("e"), 5);
        operations[0x6c] = new OpBitReg(processor, clock, processor.register("h"), 5);
        operations[0x6d] = new OpBitReg(processor, clock, processor.register("l"), 5);
        operations[0x6e] = new OpBitHlIndirect(processor, clock, memory, 5);
        operations[0x6f] = new OpBitReg(processor, clock, processor.register("a"), 5);

        operations[0x70] = new OpBitReg(processor, clock, processor.register("b"), 6);
        operations[0x71] = new OpBitReg(processor, clock, processor.register("c"), 6);
        operations[0x72] = new OpBitReg(processor, clock, processor.register("d"), 6);
        operations[0x73] = new OpBitReg(processor, clock, processor.register("e"), 6);
        operations[0x74] = new OpBitReg(processor, clock, processor.register("h"), 6);
        operations[0x75] = new OpBitReg(processor, clock, processor.register("l"), 6);
        operations[0x76] = new OpBitHlIndirect(processor, clock, memory, 6);
        operations[0x77] = new OpBitReg(processor, clock, processor.register("a"), 6);
        operations[0x78] = new OpBitReg(processor, clock, processor.register("b"), 7);
        operations[0x79] = new OpBitReg(processor, clock, processor.register("c"), 7);
        operations[0x7a] = new OpBitReg(processor, clock, processor.register("d"), 7);
        operations[0x7b] = new OpBitReg(processor, clock, processor.register("e"), 7);
        operations[0x7c] = new OpBitReg(processor, clock, processor.register("h"), 7);
        operations[0x7d] = new OpBitReg(processor, clock, processor.register("l"), 7);
        operations[0x7e] = new OpBitHlIndirect(processor, clock, memory, 7);
        operations[0x7f] = new OpBitReg(processor, clock, processor.register("a"), 7);

        operations[0x80] = new OpResReg(clock, processor.register("b"), 0);
        operations[0x81] = new OpResReg(clock, processor.register("c"), 0);
        operations[0x82] = new OpResReg(clock, processor.register("d"), 0);
        operations[0x83] = new OpResReg(clock, processor.register("e"), 0);
        operations[0x84] = new OpResReg(clock, processor.register("h"), 0);
        operations[0x85] = new OpResReg(clock, processor.register("l"), 0);
        operations[0x86] = new OpResHlIndirect(processor, clock, memory, 0);
        operations[0x87] = new OpResReg(clock, processor.register("a"), 0);
        operations[0x88] = new OpResReg(clock, processor.register("b"), 1);
        operations[0x89] = new OpResReg(clock, processor.register("c"), 1);
        operations[0x8a] = new OpResReg(clock, processor.register("d"), 1);
        operations[0x8b] = new OpResReg(clock, processor.register("e"), 1);
        operations[0x8c] = new OpResReg(clock, processor.register("h"), 1);
        operations[0x8d] = new OpResReg(clock, processor.register("l"), 1);
        operations[0x8e] = new OpResHlIndirect(processor, clock, memory, 1);
        operations[0x8f] = new OpResReg(clock, processor.register("a"), 1);

        operations[0x90] = new OpResReg(clock, processor.register("b"), 2);
        operations[0x91] = new OpResReg(clock, processor.register("c"), 2);
        operations[0x92] = new OpResReg(clock, processor.register("d"), 2);
        operations[0x93] = new OpResReg(clock, processor.register("e"), 2);
        operations[0x94] = new OpResReg(clock, processor.register("h"), 2);
        operations[0x95] = new OpResReg(clock, processor.register("l"), 2);
        operations[0x96] = new OpResHlIndirect(processor, clock, memory, 2);
        operations[0x97] = new OpResReg(clock, processor.register("a"), 2);
        operations[0x98] = new OpResReg(clock, processor.register("b"), 3);
        operations[0x99] = new OpResReg(clock, processor.register("c"), 3);
        operations[0x9a] = new OpResReg(clock, processor.register("d"), 3);
        operations[0x9b] = new OpResReg(clock, processor.register("e"), 3);
        operations[0x9c] = new OpResReg(clock, processor.register("h"), 3);
        operations[0x9d] = new OpResReg(clock, processor.register("l"), 3);
        operations[0x9e] = new OpResHlIndirect(processor, clock, memory, 3);
        operations[0x9f] = new OpResReg(clock, processor.register("a"), 3);

        operations[0xa0] = new OpResReg(clock, processor.register("b"), 4);
        operations[0xa1] = new OpResReg(clock, processor.register("c"), 4);
        operations[0xa2] = new OpResReg(clock, processor.register("d"), 4);
        operations[0xa3] = new OpResReg(clock, processor.register("e"), 4);
        operations[0xa4] = new OpResReg(clock, processor.register("h"), 4);
        operations[0xa5] = new OpResReg(clock, processor.register("l"), 4);
        operations[0xa6] = new OpResHlIndirect(processor, clock, memory, 4);
        operations[0xa7] = new OpResReg(clock, processor.register("a"), 4);
        operations[0xa8] = new OpResReg(clock, processor.register("b"), 5);
        operations[0xa9] = new OpResReg(clock, processor.register("c"), 5);
        operations[0xaa] = new OpResReg(clock, processor.register("d"), 5);
        operations[0xab] = new OpResReg(clock, processor.register("e"), 5);
        operations[0xac] = new OpResReg(clock, processor.register("h"), 5);
        operations[0xad] = new OpResReg(clock, processor.register("l"), 5);
        operations[0xae] = new OpResHlIndirect(processor, clock, memory, 5);
        operations[0xaf] = new OpResReg(clock, processor.register("a"), 5);

        operations[0xb0] = new OpResReg(clock, processor.register("b"), 6);
        operations[0xb1] = new OpResReg(clock, processor.register("c"), 6);
        operations[0xb2] = new OpResReg(clock, processor.register("d"), 6);
        operations[0xb3] = new OpResReg(clock, processor.register("e"), 6);
        operations[0xb4] = new OpResReg(clock, processor.register("h"), 6);
        operations[0xb5] = new OpResReg(clock, processor.register("l"), 6);
        operations[0xb6] = new OpResHlIndirect(processor, clock, memory, 6);
        operations[0xb7] = new OpResReg(clock, processor.register("a"), 6);
        operations[0xb8] = new OpResReg(clock, processor.register("b"), 7);
        operations[0xb9] = new OpResReg(clock, processor.register("c"), 7);
        operations[0xba] = new OpResReg(clock, processor.register("d"), 7);
        operations[0xbb] = new OpResReg(clock, processor.register("e"), 7);
        operations[0xbc] = new OpResReg(clock, processor.register("h"), 7);
        operations[0xbd] = new OpResReg(clock, processor.register("l"), 7);
        operations[0xbe] = new OpResHlIndirect(processor, clock, memory, 7);
        operations[0xbf] = new OpResReg(clock, processor.register("a"), 7);

        operations[0xc0] = new OpSetReg(clock, processor.register("b"), 0);
        operations[0xc1] = new OpSetReg(clock, processor.register("c"), 0);
        operations[0xc2] = new OpSetReg(clock, processor.register("d"), 0);
        operations[0xc3] = new OpSetReg(clock, processor.register("e"), 0);
        operations[0xc4] = new OpSetReg(clock, processor.register("h"), 0);
        operations[0xc5] = new OpSetReg(clock, processor.register("l"), 0);
        operations[0xc6] = new OpSetHlIndirect(processor, clock, memory, 0);
        operations[0xc7] = new OpSetReg(clock, processor.register("a"), 0);
        operations[0xc8] = new OpSetReg(clock, processor.register("b"), 1);
        operations[0xc9] = new OpSetReg(clock, processor.register("c"), 1);
        operations[0xca] = new OpSetReg(clock, processor.register("d"), 1);
        operations[0xcb] = new OpSetReg(clock, processor.register("e"), 1);
        operations[0xcc] = new OpSetReg(clock, processor.register("h"), 1);
        operations[0xcd] = new OpSetReg(clock, processor.register("l"), 1);
        operations[0xce] = new OpSetHlIndirect(processor, clock, memory, 1);
        operations[0xcf] = new OpSetReg(clock, processor.register("a"), 1);

        operations[0xd0] = new OpSetReg(clock, processor.register("b"), 2);
        operations[0xd1] = new OpSetReg(clock, processor.register("c"), 2);
        operations[0xd2] = new OpSetReg(clock, processor.register("d"), 2);
        operations[0xd3] = new OpSetReg(clock, processor.register("e"), 2);
        operations[0xd4] = new OpSetReg(clock, processor.register("h"), 2);
        operations[0xd5] = new OpSetReg(clock, processor.register("l"), 2);
        operations[0xd6] = new OpSetHlIndirect(processor, clock, memory, 2);
        operations[0xd7] = new OpSetReg(clock, processor.register("a"), 2);
        operations[0xd8] = new OpSetReg(clock, processor.register("b"), 3);
        operations[0xd9] = new OpSetReg(clock, processor.register("c"), 3);
        operations[0xda] = new OpSetReg(clock, processor.register("d"), 3);
        operations[0xdb] = new OpSetReg(clock, processor.register("e"), 3);
        operations[0xdc] = new OpSetReg(clock, processor.register("h"), 3);
        operations[0xdd] = new OpSetReg(clock, processor.register("l"), 3);
        operations[0xde] = new OpSetHlIndirect(processor, clock, memory, 3);
        operations[0xdf] = new OpSetReg(clock, processor.register("a"), 3);

        operations[0xe0] = new OpSetReg(clock, processor.register("b"), 4);
        operations[0xe1] = new OpSetReg(clock, processor.register("c"), 4);
        operations[0xe2] = new OpSetReg(clock, processor.register("d"), 4);
        operations[0xe3] = new OpSetReg(clock, processor.register("e"), 4);
        operations[0xe4] = new OpSetReg(clock, processor.register("h"), 4);
        operations[0xe5] = new OpSetReg(clock, processor.register("l"), 4);
        operations[0xe6] = new OpSetHlIndirect(processor, clock, memory, 4);
        operations[0xe7] = new OpSetReg(clock, processor.register("a"), 4);
        operations[0xe8] = new OpSetReg(clock, processor.register("b"), 5);
        operations[0xe9] = new OpSetReg(clock, processor.register("c"), 5);
        operations[0xea] = new OpSetReg(clock, processor.register("d"), 5);
        operations[0xeb] = new OpSetReg(clock, processor.register("e"), 5);
        operations[0xec] = new OpSetReg(clock, processor.register("h"), 5);
        operations[0xed] = new OpSetReg(clock, processor.register("l"), 5);
        operations[0xee] = new OpSetHlIndirect(processor, clock, memory, 5);
        operations[0xef] = new OpSetReg(clock, processor.register("a"), 5);

        operations[0xf0] = new OpSetReg(clock, processor.register("b"), 6);
        operations[0xf1] = new OpSetReg(clock, processor.register("c"), 6);
        operations[0xf2] = new OpSetReg(clock, processor.register("d"), 6);
        operations[0xf3] = new OpSetReg(clock, processor.register("e"), 6);
        operations[0xf4] = new OpSetReg(clock, processor.register("h"), 6);
        operations[0xf5] = new OpSetReg(clock, processor.register("l"), 6);
        operations[0xf6] = new OpSetHlIndirect(processor, clock, memory, 6);
        operations[0xf7] = new OpSetReg(clock, processor.register("a"), 6);
        operations[0xf8] = new OpSetReg(clock, processor.register("b"), 7);
        operations[0xf9] = new OpSetReg(clock, processor.register("c"), 7);
        operations[0xfa] = new OpSetReg(clock, processor.register("d"), 7);
        operations[0xfb] = new OpSetReg(clock, processor.register("e"), 7);
        operations[0xfc] = new OpSetReg(clock, processor.register("h"), 7);
        operations[0xfd] = new OpSetReg(clock, processor.register("l"), 7);
        operations[0xfe] = new OpSetHlIndirect(processor, clock, memory, 7);
        operations[0xff] = new OpSetReg(clock, processor.register("a"), 7);

        return operations;
    }

    public static Operation[] buildIndexedGroup(
        final Processor processor,
        final Clock clock,
        final Memory memory,
        final IndexRegister indexRegister
    ) {
        final Operation[] operations = new Operation[0x100];
        operations[0x09] = new OpAddIndexedReg(processor, clock, indexRegister, processor.register("bc"));
        operations[0x19] = new OpAddIndexedReg(processor, clock, indexRegister, processor.register("de"));

        operations[0x21] = new OpLdIndexedImmediate(processor, clock, indexRegister);
        operations[0x22] = new OpLdAddress16Reg(processor, clock, memory, indexRegister);
        operations[0x23] = new OpIncIndexed(clock, indexRegister);
        operations[0x24] = new OpIncIndexed8Reg(processor, clock, indexRegister.highReg());
        operations[0x25] = new OpDecIndexed8Reg(processor, clock, indexRegister.highReg());
        operations[0x26] = new OpLdIndexed8RegImmediate(processor, clock, indexRegister.highReg());
        operations[0x29] = new OpAddIndexedReg(processor, clock, indexRegister, indexRegister);
        operations[0x2a] = new OpLd16RegAddress(processor, clock, memory, indexRegister);
        operations[0x2b] = new OpDecIndexed(clock, indexRegister);
        operations[0x2c] = new OpIncIndexed8Reg(processor, clock, indexRegister.lowReg());
        operations[0x2d] = new OpDecIndexed8Reg(processor, clock, indexRegister.lowReg());
        operations[0x2e] = new OpLdIndexed8RegImmediate(processor, clock, indexRegister.lowReg());

        operations[0x34] = new OpIncIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x35] = new OpDecIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x36] = new OpLdIndexedIndirectImmediate(processor, clock, memory, indexRegister);
        operations[0x39] = new OpAddIndexedReg(processor, clock, indexRegister, processor.register("sp"));

        operations[0x44] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("b"), indexRegister.highReg());
        operations[0x45] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("b"), indexRegister.lowReg());
        operations[0x46] = new OpLd8RegIndexedIndirect(processor, clock, memory, processor.register("b"), indexRegister);
        operations[0x4c] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("c"), indexRegister.highReg());
        operations[0x4d] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("c"), indexRegister.lowReg());
        operations[0x4e] = new OpLd8RegIndexedIndirect(processor, clock, memory, processor.register("c"), indexRegister);

        operations[0x54] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("d"), indexRegister.highReg());
        operations[0x55] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("d"), indexRegister.lowReg());
        operations[0x56] = new OpLd8RegIndexedIndirect(processor, clock, memory, processor.register("d"), indexRegister);
        operations[0x5c] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("e"), indexRegister.highReg());
        operations[0x5d] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("e"), indexRegister.lowReg());
        operations[0x5e] = new OpLd8RegIndexedIndirect(processor, clock, memory, processor.register("e"), indexRegister);

        operations[0x60] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.highReg(), processor.register("b"));
        operations[0x61] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.highReg(), processor.register("c"));
        operations[0x62] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.highReg(), processor.register("d"));
        operations[0x63] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.highReg(), processor.register("e"));
        operations[0x64] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.highReg(), indexRegister.highReg());
        operations[0x65] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.highReg(), indexRegister.lowReg());
        operations[0x66] = new OpLd8RegIndexedIndirect(processor, clock, memory, processor.register("h"), indexRegister);
        operations[0x67] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.highReg(), processor.register("a"));
        operations[0x68] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.lowReg(), processor.register("b"));
        operations[0x69] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.lowReg(), processor.register("c"));
        operations[0x6a] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.lowReg(), processor.register("d"));
        operations[0x6b] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.lowReg(), processor.register("e"));
        operations[0x6c] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.lowReg(), indexRegister.highReg());
        operations[0x6d] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.lowReg(), indexRegister.lowReg());
        operations[0x6e] = new OpLd8RegIndexedIndirect(processor, clock, memory, processor.register("l"), indexRegister);
        operations[0x6f] = new OpLdIndexed8RegFrom8Reg(clock, indexRegister.lowReg(), processor.register("a"));

        operations[0x70] = new OpLdIndexedIndirect8Reg(processor, clock, memory, indexRegister, processor.register("b"));
        operations[0x71] = new OpLdIndexedIndirect8Reg(processor, clock, memory, indexRegister, processor.register("c"));
        operations[0x72] = new OpLdIndexedIndirect8Reg(processor, clock, memory, indexRegister, processor.register("d"));
        operations[0x73] = new OpLdIndexedIndirect8Reg(processor, clock, memory, indexRegister, processor.register("e"));
        operations[0x74] = new OpLdIndexedIndirect8Reg(processor, clock, memory, indexRegister, processor.register("h"));
        operations[0x75] = new OpLdIndexedIndirect8Reg(processor, clock, memory, indexRegister, processor.register("l"));
        operations[0x77] = new OpLdIndexedIndirect8Reg(processor, clock, memory, indexRegister, processor.register("a"));
        operations[0x7c] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("a"), indexRegister.highReg());
        operations[0x7d] = new OpLdIndexed8RegFrom8Reg(clock, processor.register("a"), indexRegister.lowReg());
        operations[0x7e] = new OpLd8RegIndexedIndirect(processor, clock, memory, processor.register("a"), indexRegister);

        operations[0x84] = new OpAddAIndexed8Reg(processor, clock, indexRegister.highReg(), false);
        operations[0x85] = new OpAddAIndexed8Reg(processor, clock, indexRegister.lowReg(), false);
        operations[0x86] = new OpAddAIndexedIndirect(processor, clock, memory, indexRegister, false);
        operations[0x8c] = new OpAddAIndexed8Reg(processor, clock, indexRegister.highReg(), true);
        operations[0x8d] = new OpAddAIndexed8Reg(processor, clock, indexRegister.lowReg(), true);
        operations[0x8e] = new OpAddAIndexedIndirect(processor, clock, memory, indexRegister, true);

        operations[0x95] = new OpSubAIndexed8Reg(processor, clock, indexRegister.lowReg(), false);
        operations[0x94] = new OpSubAIndexed8Reg(processor, clock, indexRegister.highReg(), false);
        operations[0x96] = new OpSubAIndexedIndirect(processor, clock, memory, indexRegister, false);
        operations[0x9c] = new OpSubAIndexed8Reg(processor, clock, indexRegister.highReg(), true);
        operations[0x9d] = new OpSubAIndexed8Reg(processor, clock, indexRegister.lowReg(), true);
        operations[0x9e] = new OpSubAIndexedIndirect(processor, clock, memory, indexRegister, true);

        operations[0xa4] = new OpAndAIndexed8Reg(processor, clock, indexRegister.highReg());
        operations[0xa5] = new OpAndAIndexed8Reg(processor, clock, indexRegister.lowReg());
        operations[0xa6] = new OpAndIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0xac] = new OpXorAIndexed8Reg(processor, clock, indexRegister.highReg());
        operations[0xad] = new OpXorAIndexed8Reg(processor, clock, indexRegister.lowReg());
        operations[0xae] = new OpXorIndexedIndirect(processor, clock, memory, indexRegister);

        operations[0xb4] = new OpOrAIndexed8Reg(processor, clock, indexRegister.highReg());
        operations[0xb5] = new OpOrAIndexed8Reg(processor, clock, indexRegister.lowReg());
        operations[0xb6] = new OpOrIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0xbc] = new OpCpAIndexed8Reg(processor, clock, indexRegister.highReg());
        operations[0xbd] = new OpCpAIndexed8Reg(processor, clock, indexRegister.lowReg());
        operations[0xbe] = new OpCpIndexedIndirect(processor, clock, memory, indexRegister);

        operations[0xe1] = new OpPopIndexed(processor, clock, indexRegister);
        operations[0xe3] = new OpExSpIndirectIndexed(processor, clock, indexRegister, memory);
        operations[0xe5] = new OpPushIndexed(processor, clock, indexRegister);
        operations[0xe9] = new OpJpIndexedIndirect(processor, clock, indexRegister);

        operations[0xf9] = new OpLdSpIndexed(processor, clock, indexRegister);

        return operations;
    }

    public static Operation[] buildIndexedBitwiseGroup(
        final Processor processor,
        final Clock clock,
        final Memory memory,
        final IndexRegister indexRegister
    ) {
        final Operation[] operations = new Operation[0x100];
        operations[0x06] = new OpRlcIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x0e] = new OpRrcIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x16] = new OpRlIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x1e] = new OpRrIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x26] = new OpSlaIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x2e] = new OpSraIndexedIndirect(processor, clock, memory, indexRegister);
        operations[0x3e] = new OpSrlIndexedIndirect(processor, clock, memory, indexRegister);

        operations[0x46] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 0);
        operations[0x4e] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 1);
        operations[0x56] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 2);
        operations[0x5e] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 3);
        operations[0x66] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 4);
        operations[0x6e] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 5);
        operations[0x76] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 6);
        operations[0x7e] = new OpBitIndexedIndirect(processor, clock, memory, indexRegister, 7);

        operations[0x86] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 0);
        operations[0x8e] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 1);
        operations[0x96] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 2);
        operations[0x9e] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 3);
        operations[0xa6] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 4);
        operations[0xae] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 5);
        operations[0xb6] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 6);
        operations[0xbe] = new OpResIndexedIndirect(processor, clock, memory, indexRegister, 7);

        operations[0xc6] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 0);
        operations[0xce] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 1);
        operations[0xd6] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 2);
        operations[0xde] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 3);
        operations[0xe6] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 4);
        operations[0xee] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 5);
        operations[0xf6] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 6);
        operations[0xfe] = new OpSetIndexedIndirect(processor, clock, memory, indexRegister, 7);

        return operations;
    }
}
