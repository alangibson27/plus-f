package com.socialthingy.qaopm.z80;

import com.socialthingy.qaopm.util.Word;
import com.socialthingy.qaopm.z80.operations.*;

import com.socialthingy.qaopm.z80.FlagsRegister.Flag;

import java.util.HashMap;
import java.util.Map;

public class Processor {

    private final Map<String, Register> registers = new HashMap<>();
    private final int[] memory;
    private final IO io;
    private final Operation[] operations = new Operation[0x100];
    private boolean enableIffAfterOp = false;
    private boolean iffs[] = new boolean[2];
    private final WordRegister pcReg = new WordRegister();
    private final WordRegister spReg = new WordRegister();
    private final FlagsRegister fReg = new FlagsRegister();

    public Processor(final int[] memory, final IO io) {
        this.memory = memory;
        this.io = io;

        prepareRegisters();
        prepareOperations();
    }

    private void prepareOperations() {
                operations[0x00] = new Nop();

                operations[0x01] = new OpLd16RegImmediate(this, registers.get("bc"));
                operations[0x02] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("bc"), registers.get("a"));
//                operations[0x03] = new OpInc16Reg(this, "bc");
                operations[0x04] = new OpInc8Reg(this, registers.get("b"));
                operations[0x05] = new OpDec8Reg(this, registers.get("b"));
                operations[0x06] = new OpLd8RegImmediate(this, registers.get("b"));
                operations[0x07] = new OpRlca(this);
//                operations[0x08] = new OpExAfAfPrime(this);
//                operations[0x09] = new OpAddHl16Reg(this, "bc");
                operations[0x0a] = new OpLd8RegFrom16RegIndirect(memory, registers.get("a"), registers.get("bc"));
//                operations[0x0b] = new OpDec16Reg(this, "bc");
                operations[0x0c] = new OpInc8Reg(this, registers.get("c"));
                operations[0x0d] = new OpDec8Reg(this, registers.get("c"));
                operations[0x0e] = new OpLd8RegImmediate(this, registers.get("c"));
                operations[0x0f] = new OpRrca(this);
//
                operations[0x10] = new OpDjnz(this);
                operations[0x11] = new OpLd16RegImmediate(this, registers.get("de"));
                operations[0x12] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("de"), registers.get("a"));
//                operations[0x13] = new OpInc16Reg(this, "de");
                operations[0x14] = new OpInc8Reg(this, registers.get("d"));
                operations[0x15] = new OpDec8Reg(this, registers.get("d"));
                operations[0x16] = new OpLd8RegImmediate(this, registers.get("d"));
                operations[0x17] = new OpRla(this);
                operations[0x18] = new OpJr(this);
//                operations[0x19] = new OpAddHl16Reg(this, "de");
                operations[0x1a] = new OpLd8RegFrom16RegIndirect(memory, registers.get("a"), registers.get("de"));
//                operations[0x1b] = new OpDec16Reg(this, "de");
                operations[0x1c] = new OpInc8Reg(this, registers.get("e"));
                operations[0x1d] = new OpDec8Reg(this, registers.get("e"));
                operations[0x1e] = new OpLd8RegImmediate(this, registers.get("e"));
                operations[0x1f] = new OpRra(this);
//
                operations[0x20] = new OpJrConditional(this, Flag.Z, false);
                operations[0x21] = new OpLd16RegImmediate(this, registers.get("hl"));
                operations[0x22] = new OpLdAddressHl(this, memory);
//                operations[0x23] = new OpInc16Reg(this, "hl");
                operations[0x24] = new OpInc8Reg(this, registers.get("h"));
                operations[0x25] = new OpDec8Reg(this, registers.get("h"));
                operations[0x26] = new OpLd8RegImmediate(this, registers.get("h"));
                operations[0x27] = new OpDaa(this);
                operations[0x28] = new OpJrConditional(this, Flag.Z, true);
//                operations[0x29] = new OpAddHl16Reg(this, "hl");
                operations[0x2a] = new OpLdHlAddress(this, memory);
//                operations[0x2b] = new OpDec16Reg(this, "hl");
                operations[0x2c] = new OpInc8Reg(this, registers.get("l"));
                operations[0x2d] = new OpDec8Reg(this, registers.get("l"));
                operations[0x2e] = new OpLd8RegImmediate(this, registers.get("l"));
                operations[0x2f] = new OpCpl(this);

                operations[0x30] = new OpJrConditional(this, Flag.C, false);
                operations[0x31] = new OpLd16RegImmediate(this, registers.get("sp"));
                operations[0x32] = new OpLdAddressA(this, memory);
//                operations[0x33] = new OpInc16Reg(this, "sp");
                operations[0x34] = new OpIncHlIndirect(this, memory);
                operations[0x35] = new OpDecHlIndirect(this, memory);
                operations[0x36] = new OpLdHlIndirectImmediate(this, memory);
                operations[0x37] = new OpScf(this);
                operations[0x38] = new OpJrConditional(this, Flag.C, true);
//                operations[0x39] = new OpAddHl16Reg(this, "sp");
                operations[0x3a] = new OpLdAAddress(this, memory);
//                operations[0x3b] = new OpDec16Reg(this, "sp");
                operations[0x3c] = new OpInc8Reg(this, registers.get("a"));
                operations[0x3d] = new OpDec8Reg(this, registers.get("a"));
                operations[0x3e] = new OpLd8RegImmediate(this, registers.get("a"));
                operations[0x3f] = new OpCcf(this);

                operations[0x40] = new OpLd8RegFrom8Reg(registers.get("b"), registers.get("b"));
                operations[0x41] = new OpLd8RegFrom8Reg(registers.get("b"), registers.get("c"));
                operations[0x42] = new OpLd8RegFrom8Reg(registers.get("b"), registers.get("d"));
                operations[0x43] = new OpLd8RegFrom8Reg(registers.get("b"), registers.get("e"));
                operations[0x44] = new OpLd8RegFrom8Reg(registers.get("b"), registers.get("h"));
                operations[0x45] = new OpLd8RegFrom8Reg(registers.get("b"), registers.get("l"));
                operations[0x46] = new OpLd8RegFrom16RegIndirect(memory, registers.get("b"), registers.get("hl"));
                operations[0x47] = new OpLd8RegFrom8Reg(registers.get("b"), registers.get("a"));
                operations[0x48] = new OpLd8RegFrom8Reg(registers.get("c"), registers.get("b"));
                operations[0x49] = new OpLd8RegFrom8Reg(registers.get("c"), registers.get("c"));
                operations[0x4a] = new OpLd8RegFrom8Reg(registers.get("c"), registers.get("d"));
                operations[0x4b] = new OpLd8RegFrom8Reg(registers.get("c"), registers.get("e"));
                operations[0x4c] = new OpLd8RegFrom8Reg(registers.get("c"), registers.get("h"));
                operations[0x4d] = new OpLd8RegFrom8Reg(registers.get("c"), registers.get("l"));
                operations[0x4e] = new OpLd8RegFrom16RegIndirect(memory, registers.get("c"), registers.get("hl"));
                operations[0x4f] = new OpLd8RegFrom8Reg(registers.get("c"), registers.get("a"));
//
                operations[0x50] = new OpLd8RegFrom8Reg(registers.get("d"), registers.get("b"));
                operations[0x51] = new OpLd8RegFrom8Reg(registers.get("d"), registers.get("c"));
                operations[0x52] = new OpLd8RegFrom8Reg(registers.get("d"), registers.get("d"));
                operations[0x53] = new OpLd8RegFrom8Reg(registers.get("d"), registers.get("e"));
                operations[0x54] = new OpLd8RegFrom8Reg(registers.get("d"), registers.get("h"));
                operations[0x55] = new OpLd8RegFrom8Reg(registers.get("d"), registers.get("l"));
                operations[0x56] = new OpLd8RegFrom16RegIndirect(memory, registers.get("d"), registers.get("hl"));
                operations[0x57] = new OpLd8RegFrom8Reg(registers.get("d"), registers.get("a"));
                operations[0x58] = new OpLd8RegFrom8Reg(registers.get("e"), registers.get("b"));
                operations[0x59] = new OpLd8RegFrom8Reg(registers.get("e"), registers.get("c"));
                operations[0x5a] = new OpLd8RegFrom8Reg(registers.get("e"), registers.get("d"));
                operations[0x5b] = new OpLd8RegFrom8Reg(registers.get("e"), registers.get("e"));
                operations[0x5c] = new OpLd8RegFrom8Reg(registers.get("e"), registers.get("h"));
                operations[0x5d] = new OpLd8RegFrom8Reg(registers.get("e"), registers.get("l"));
                operations[0x5e] = new OpLd8RegFrom16RegIndirect(memory, registers.get("e"), registers.get("hl"));
                operations[0x5f] = new OpLd8RegFrom8Reg(registers.get("e"), registers.get("a"));
//
                operations[0x60] = new OpLd8RegFrom8Reg(registers.get("h"), registers.get("b"));
                operations[0x61] = new OpLd8RegFrom8Reg(registers.get("h"), registers.get("c"));
                operations[0x62] = new OpLd8RegFrom8Reg(registers.get("h"), registers.get("d"));
                operations[0x63] = new OpLd8RegFrom8Reg(registers.get("h"), registers.get("e"));
                operations[0x64] = new OpLd8RegFrom8Reg(registers.get("h"), registers.get("h"));
                operations[0x65] = new OpLd8RegFrom8Reg(registers.get("h"), registers.get("l"));
                operations[0x66] = new OpLd8RegFrom16RegIndirect(memory, registers.get("h"), registers.get("hl"));
                operations[0x67] = new OpLd8RegFrom8Reg(registers.get("h"), registers.get("a"));
                operations[0x68] = new OpLd8RegFrom8Reg(registers.get("l"), registers.get("b"));
                operations[0x69] = new OpLd8RegFrom8Reg(registers.get("l"), registers.get("c"));
                operations[0x6a] = new OpLd8RegFrom8Reg(registers.get("l"), registers.get("d"));
                operations[0x6b] = new OpLd8RegFrom8Reg(registers.get("l"), registers.get("e"));
                operations[0x6c] = new OpLd8RegFrom8Reg(registers.get("l"), registers.get("h"));
                operations[0x6d] = new OpLd8RegFrom8Reg(registers.get("l"), registers.get("l"));
                operations[0x6e] = new OpLd8RegFrom16RegIndirect(memory, registers.get("l"), registers.get("hl"));
                operations[0x6f] = new OpLd8RegFrom8Reg(registers.get("l"), registers.get("a"));
//
                operations[0x70] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("hl"), registers.get("b"));
                operations[0x71] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("hl"), registers.get("c"));
                operations[0x72] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("hl"), registers.get("d"));
                operations[0x73] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("hl"), registers.get("e"));
                operations[0x74] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("hl"), registers.get("h"));
                operations[0x75] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("hl"), registers.get("l"));
//                operations[0x76] = new OpHalt(this);
                operations[0x77] = new OpLd16RegIndirectFrom8Reg(memory, registers.get("hl"), registers.get("a"));
                operations[0x78] = new OpLd8RegFrom8Reg(registers.get("a"), registers.get("b"));
                operations[0x79] = new OpLd8RegFrom8Reg(registers.get("a"), registers.get("c"));
                operations[0x7a] = new OpLd8RegFrom8Reg(registers.get("a"), registers.get("d"));
                operations[0x7b] = new OpLd8RegFrom8Reg(registers.get("a"), registers.get("e"));
                operations[0x7c] = new OpLd8RegFrom8Reg(registers.get("a"), registers.get("h"));
                operations[0x7d] = new OpLd8RegFrom8Reg(registers.get("a"), registers.get("l"));
                operations[0x7e] = new OpLd8RegFrom16RegIndirect(memory, registers.get("a"), registers.get("hl"));
                operations[0x7f] = new OpLd8RegFrom8Reg(registers.get("a"), registers.get("a"));

                operations[0x80] = new OpAddA8Reg(this, registers.get("b"), false);
                operations[0x81] = new OpAddA8Reg(this, registers.get("c"), false);
                operations[0x82] = new OpAddA8Reg(this, registers.get("d"), false);
                operations[0x83] = new OpAddA8Reg(this, registers.get("e"), false);
                operations[0x84] = new OpAddA8Reg(this, registers.get("h"), false);
                operations[0x85] = new OpAddA8Reg(this, registers.get("l"), false);
                operations[0x86] = new OpAddAHlIndirect(this, memory, false);
                operations[0x87] = new OpAddA8Reg(this, registers.get("a"), false);
                operations[0x88] = new OpAddA8Reg(this, registers.get("b"), true);
                operations[0x89] = new OpAddA8Reg(this, registers.get("c"), true);
                operations[0x8a] = new OpAddA8Reg(this, registers.get("d"), true);
                operations[0x8b] = new OpAddA8Reg(this, registers.get("e"), true);
                operations[0x8c] = new OpAddA8Reg(this, registers.get("h"), true);
                operations[0x8d] = new OpAddA8Reg(this, registers.get("l"), true);
                operations[0x8e] = new OpAddAHlIndirect(this, memory, true);
                operations[0x8f] = new OpAddA8Reg(this, registers.get("a"), true);
//
                operations[0x90] = new OpSubA8Reg(this, registers.get("b"), false);
                operations[0x91] = new OpSubA8Reg(this, registers.get("c"), false);
                operations[0x92] = new OpSubA8Reg(this, registers.get("d"), false);
                operations[0x93] = new OpSubA8Reg(this, registers.get("e"), false);
                operations[0x94] = new OpSubA8Reg(this, registers.get("h"), false);
                operations[0x95] = new OpSubA8Reg(this, registers.get("l"), false);
                operations[0x96] = new OpSubAHlIndirect(this, memory, false);
                operations[0x97] = new OpSubA8Reg(this, registers.get("a"), false);
                operations[0x98] = new OpSubA8Reg(this, registers.get("b"), true);
                operations[0x99] = new OpSubA8Reg(this, registers.get("c"), true);
                operations[0x9a] = new OpSubA8Reg(this, registers.get("d"), true);
                operations[0x9b] = new OpSubA8Reg(this, registers.get("e"), true);
                operations[0x9c] = new OpSubA8Reg(this, registers.get("h"), true);
                operations[0x9d] = new OpSubA8Reg(this, registers.get("l"), true);
                operations[0x9e] = new OpSubAHlIndirect(this, memory, true);
                operations[0x9f] = new OpSubA8Reg(this, registers.get("a"), true);
//
                operations[0xa0] = new OpAndA8Reg(this, registers.get("b"));
                operations[0xa1] = new OpAndA8Reg(this, registers.get("c"));
                operations[0xa2] = new OpAndA8Reg(this, registers.get("d"));
                operations[0xa3] = new OpAndA8Reg(this, registers.get("e"));
                operations[0xa4] = new OpAndA8Reg(this, registers.get("h"));
                operations[0xa5] = new OpAndA8Reg(this, registers.get("l"));
                operations[0xa6] = new OpAndAHlIndirect(this, memory);
                operations[0xa7] = new OpAndA8Reg(this, registers.get("a"));
                operations[0xa8] = new OpXorA8Reg(this, registers.get("b"));
                operations[0xa9] = new OpXorA8Reg(this, registers.get("c"));
                operations[0xaa] = new OpXorA8Reg(this, registers.get("d"));
                operations[0xab] = new OpXorA8Reg(this, registers.get("e"));
                operations[0xac] = new OpXorA8Reg(this, registers.get("h"));
                operations[0xad] = new OpXorA8Reg(this, registers.get("l"));
                operations[0xae] = new OpXorAHlIndirect(this, memory);
                operations[0xaf] = new OpXorA8Reg(this, registers.get("a"));

                operations[0xb0] = new OpOrA8Reg(this, registers.get("b"));
                operations[0xb1] = new OpOrA8Reg(this, registers.get("c"));
                operations[0xb2] = new OpOrA8Reg(this, registers.get("d"));
                operations[0xb3] = new OpOrA8Reg(this, registers.get("e"));
                operations[0xb4] = new OpOrA8Reg(this, registers.get("h"));
                operations[0xb5] = new OpOrA8Reg(this, registers.get("l"));
                operations[0xb6] = new OpOrAHlIndirect(this, memory);
                operations[0xb7] = new OpOrA8Reg(this, registers.get("a"));
                operations[0xb8] = new OpCpA8Reg(this, register("b"));
                operations[0xb9] = new OpCpA8Reg(this, register("c"));
                operations[0xba] = new OpCpA8Reg(this, register("d"));
                operations[0xbb] = new OpCpA8Reg(this, register("e"));
                operations[0xbc] = new OpCpA8Reg(this, register("h"));
                operations[0xbd] = new OpCpA8Reg(this, register("l"));
                operations[0xbe] = new OpCpAHlIndirect(this, memory);
                operations[0xbf] = new OpCpA8Reg(this, register("a"));
//
//                operations[0xc0] = new OpRetNz(this);
                operations[0xc1] = new OpPop16Reg(this, registers.get("bc"));
                operations[0xc2] = new OpJpConditional(this, Flag.Z, false);
                operations[0xc3] = new OpJp(this);
//                operations[0xc4] = new OpCallNz(this);
                operations[0xc5] = new OpPush16Reg(this, registers.get("bc"));
                operations[0xc6] = new OpAddAImmediate(this, false);
//                operations[0xc7] = new OpRst(this, 0x00);
//                operations[0xc8] = new OpRetZ(this);
//                operations[0xc9] = new OpRet(this);
                operations[0xca] = new OpJpConditional(this, Flag.Z, true);
//                operations[0xcc] = new OpCallZ(this);
//                operations[0xcd] = new OpCall(this);
                operations[0xce] = new OpAddAImmediate(this, true);
//                operations[0xcf] = new OpRst(this, 0x08);
//
//                operations[0xd0] = new OpRetNc(this);
                operations[0xd1] = new OpPop16Reg(this, registers.get("de"));
                operations[0xd2] = new OpJpConditional(this, Flag.C, false);
//                operations[0xd3] = new OpOutA(this, this.io);
//                operations[0xd4] = new OpCallNc(this);
                operations[0xd5] = new OpPush16Reg(this, registers.get("de"));
                operations[0xd6] = new OpSubAImmediate(this, false);
//                operations[0xd7] = new OpRst(this, 0x10);
//                operations[0xd8] = new OpRetC(this);
//                operations[0xd9] = new OpExx(this);
                operations[0xda] = new OpJpConditional(this, Flag.C, true);
//                operations[0xdb] = new OpInA(this, this.io);
//                operations[0xdc] = new OpCallC(this);
                operations[0xde] = new OpSubAImmediate(this, true);
//                operations[0xdf] = new OpRst(this, 0x18);
//
//                operations[0xe0] = new OpRetPo(this);
                operations[0xe1] = new OpPop16Reg(this, registers.get("hl"));
                operations[0xe2] = new OpJpConditional(this, Flag.P, false);
//                operations[0xe3] = new OpExSpIndirectHl(this, this.memory);
//                operations[0xe4] = new OpCallPo(this);
                operations[0xe5] = new OpPush16Reg(this, registers.get("hl"));
                operations[0xe6] = new OpAndAImmediate(this);
//                operations[0xe7] = new OpRst(this, 0x20);
//                operations[0xe8] = new OpRetPe(this);
                operations[0xe9] = new OpJpHlIndirect(this);
                operations[0xea] = new OpJpConditional(this, Flag.P, true);
//                operations[0xeb] = new OpExDeHl(this);
//                operations[0xec] = new OpCallPe(this);
                operations[0xee] = new OpXorAImmediate(this);
//                operations[0xef] = new OpRst(this, 0x28);
//
//                operations[0xf0] = new OpRetP(this);
                operations[0xf1] = new OpPop16Reg(this, registers.get("af"));
                operations[0xf2] = new OpJpConditional(this, Flag.S, false);
//                operations[0xf3] = new OpDi(this);
//                operations[0xf4] = new OpCallP(this);
                operations[0xf5] = new OpPush16Reg(this, registers.get("af"));
                operations[0xf6] = new OpOrAImmediate(this);
//                operations[0xf7] = new OpRst(this, 0x30);
//                operations[0xf8] = new OpRetM(this);
                operations[0xf9] = new OpLdSpHl(this);
                operations[0xfa] = new OpJpConditional(this, Flag.S, true);
//                operations[0xfb] = new OpEi(this);
//                operations[0xfc] = new OpCallM(this);
                operations[0xfe] = new OpCpImmediate(this);
//                operations[0xff] = new OpRst(this, 0x38);
//
                operations[0xcb] = new OpCbGroup(this, memory);
                operations[0xed] = new OpEdGroup(this, memory, io);
                operations[0xdd] = new OpDdFdGroup(this, memory, IndexRegister.class.cast(registers.get("ix")));
                operations[0xfd] = new OpDdFdGroup(this, memory, IndexRegister.class.cast(registers.get("iy")));
    }

    private void prepareRegisters() {
        final ByteRegister aReg = new ByteRegister();
        final ByteRegister bReg = new ByteRegister();
        final ByteRegister cReg = new ByteRegister();
        final ByteRegister dReg = new ByteRegister();
        final ByteRegister eReg = new ByteRegister();
        final ByteRegister hReg = new ByteRegister();
        final ByteRegister lReg = new ByteRegister();
        final ByteRegister iReg = new ByteRegister();
        final ByteRegister rReg = new ByteRegister();

        final BytePairRegister afReg = new BytePairRegister(aReg, fReg);
        final BytePairRegister bcReg = new BytePairRegister(bReg, cReg);
        final BytePairRegister deReg = new BytePairRegister(dReg, eReg);
        final BytePairRegister hlReg = new BytePairRegister(hReg, lReg);

        final IndexRegister ixReg = new IndexRegister();
        final IndexRegister iyReg = new IndexRegister();

        registers.put("a", aReg);
        registers.put("f", fReg);
        registers.put("b", bReg);
        registers.put("c", cReg);
        registers.put("d", dReg);
        registers.put("e", eReg);
        registers.put("h", hReg);
        registers.put("l", lReg);
        registers.put("i", iReg);
        registers.put("r", rReg);

        registers.put("af", afReg);
        registers.put("bc", bcReg);
        registers.put("de", deReg);
        registers.put("hl", hlReg);

        registers.put("ix", ixReg);
        registers.put("iy", iyReg);
        registers.put("pc", pcReg);
        registers.put("sp", spReg);
    }

    public Register register(final String name) {
        return this.registers.get(name);
    }

    public FlagsRegister flagsRegister() {
        return this.fReg;
    }

    public void execute() {
        final Operation op = fetch();
        if (op == null) {
            throw new IllegalStateException("Unimplemented operation");
        }

        op.execute();
    }

    private Operation fetch() {
        return operations[fetchNextPC()];
    }

    public int fetchNextPC() {
        return memory[pcReg.getAndInc()];
    }

    public int fetchNextWord() {
        return Word.from(fetchNextPC(), fetchNextPC());
    }

    public int fetchRelative(final int offset) {
        return memory[(pcReg.get() + offset) & 0xffff];
    }

    public void pushByte(final int value) {
        memory[spReg.decAndGet()] = value;
    }

    public int popByte() {
        return memory[spReg.getAndInc()];
    }
}
