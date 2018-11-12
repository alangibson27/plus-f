package com.socialthingy.plusf.z80;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.operations.*;

import java.io.PrintStream;
import java.util.*;

public class Processor {

    private final Map<String, Register> registers = new HashMap<>();
    private final Memory memory;
    private final Clock clock;
    private final Operation[] operations;
    private final Operation[] edOperations;
    private final Operation[] cbOperations;
    private final Operation[] ddOperations;
    private final Operation[] ddCbOperations;
    private final Operation[] fdOperations;
    private final Operation[] fdCbOperations;

    private final Nop nop;
    private final RRegister rReg = new RRegister();
    private final ByteRegister iReg = new ByteRegister("i");
    private final WordRegister pcReg = new WordRegister("pc");
    private final WordRegister spReg = new WordRegister("sp");
    private final FlagsRegister fReg = new FlagsRegister();
    private final OpRst im1ResponseOp;
    private final OpRst nmiResponseOp;

    private boolean enableIff = false;
    private boolean halting = false;
    private boolean iffs[] = new boolean[2];
    private int interruptMode = 1;
    private boolean interruptRequested = false;
    private Operation lastOp;

    public Processor(final Memory memory, final IO io, final Clock clock) {
        this.clock = clock;
        this.memory = memory;
        this.nop = new Nop(clock);

        prepareRegisters();
        operations = OperationTable.build(this, clock, memory, io);
        edOperations = OperationTable.buildEdGroup(this, clock, memory, io);
        cbOperations = OperationTable.buildCbGroup(this, clock, memory);
        ddOperations = OperationTable.buildIndexedGroup(this, clock, memory, (IndexRegister) registers.get("ix"));
        fdOperations = OperationTable.buildIndexedGroup(this, clock, memory, (IndexRegister) registers.get("iy"));
        ddCbOperations = OperationTable.buildIndexedBitwiseGroup(this, clock, memory, (IndexRegister) registers.get("ix"));
        fdCbOperations = OperationTable.buildIndexedBitwiseGroup(this, clock, memory, (IndexRegister) registers.get("iy"));

        this.im1ResponseOp = new OpRst(this, clock, 0x0038);
        this.nmiResponseOp = new OpRst(this, clock, 0x0066);
    }

    private void prepareRegisters() {
        final ByteRegister aReg = new ByteRegister("a");
        final ByteRegister bReg = new ByteRegister("b");
        final ByteRegister cReg = new ByteRegister("c");
        final ByteRegister dReg = new ByteRegister("d");
        final ByteRegister eReg = new ByteRegister("e");
        final ByteRegister hReg = new ByteRegister("h");
        final ByteRegister lReg = new ByteRegister("l");

        final BytePairRegister afReg = new BytePairRegister(aReg, fReg);
        final BytePairRegister bcReg = new BytePairRegister(bReg, cReg);
        final BytePairRegister deReg = new BytePairRegister(dReg, eReg);
        final BytePairRegister hlReg = new BytePairRegister(hReg, lReg);

        final ByteRegister ixhReg = new ByteRegister("ixh");
        final ByteRegister ixlReg = new ByteRegister("ixl");
        final IndexRegister ixReg = new IndexRegister(ixhReg, ixlReg);

        final ByteRegister iyhReg = new ByteRegister("iyh");
        final ByteRegister iylReg = new ByteRegister("iyl");
        final IndexRegister iyReg = new IndexRegister(iyhReg, iylReg);

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

        registers.put("ixh", ixhReg);
        registers.put("ixl", ixlReg);
        registers.put("ix", ixReg);
        registers.put("iyh", iyhReg);
        registers.put("iyl", iylReg);
        registers.put("iy", iyReg);
        registers.put("pc", pcReg);
        registers.put("sp", spReg);

        final ByteRegister aPrimeReg = new ByteRegister("a'");
        final ByteRegister fPrimeReg = new ByteRegister("f'");
        final ByteRegister bPrimeReg = new ByteRegister("b'");
        final ByteRegister cPrimeReg = new ByteRegister("c'");
        final ByteRegister dPrimeReg = new ByteRegister("d'");
        final ByteRegister ePrimeReg = new ByteRegister("e'");
        final ByteRegister hPrimeReg = new ByteRegister("h'");
        final ByteRegister lPrimeReg = new ByteRegister("l'");

        registers.put("a'", aPrimeReg);
        registers.put("f'", fPrimeReg);
        registers.put("b'", bPrimeReg);
        registers.put("c'", cPrimeReg);
        registers.put("d'", dPrimeReg);
        registers.put("e'", ePrimeReg);
        registers.put("h'", hPrimeReg);
        registers.put("l'", lPrimeReg);

        final BytePairRegister afPrimeReg = new BytePairRegister(aPrimeReg, fPrimeReg);
        final BytePairRegister bcPrimeReg = new BytePairRegister(bPrimeReg, cPrimeReg);
        final BytePairRegister dePrimeReg = new BytePairRegister(dPrimeReg, ePrimeReg);
        final BytePairRegister hlPrimeReg = new BytePairRegister(hPrimeReg, lPrimeReg);

        registers.put("af'", afPrimeReg);
        registers.put("bc'", bcPrimeReg);
        registers.put("de'", dePrimeReg);
        registers.put("hl'", hlPrimeReg);
    }

    public Register register(final String name) {
        return this.registers.get(name);
    }

    public FlagsRegister flagsRegister() {
        return this.fReg;
    }

    public Operation execute() throws ExecutionException {
        final boolean enableIffAfterExecution = enableIff;
        final int pc = pcReg.get();
        final Operation op = fetch();
        if (op == null) {
            throw new IllegalStateException(String.format("Unimplemented operation at %d", pc));
        }

        this.lastOp = op;
        try {
            op.execute();

            if (enableIffAfterExecution) {
                enableIff = false;
                setIffState(true);
            }

            return op;
        } catch (Exception ex) {
            throw new ExecutionException(op, ex);
        }
    }

    private Operation fetch() {
        if (iffs[0] && interruptRequested) {
            rReg.increment(1);
            if (halting) {
                pcReg.getAndInc();
                halting = false;
            }
            setIffState(false);

            if (interruptMode == 1) {
                return im1ResponseOp;
            } else {
                final int jumpBase = Word.from(0xff, iReg.get());
                final int jumpLow = memory.get(jumpBase);
                final int jumpHigh = memory.get(jumpBase + 1);
                return new OpCallDirect(this, clock, Word.from(jumpLow, jumpHigh));
            }
        } else {
            return fetchFromMemory(pcReg.get());
        }
    }

    private Operation fetchFromMemory(final int startPc) {
        int pc = startPc;
        final int opCode1 = fromMemory(pc++);
        Operation op;
        int refreshes = 2;
        switch (opCode1) {
            case 0xcb:
                op = fromOpTable(cbOperations, fromMemory(pc++));
                break;

            case 0xed:
                op = fromOpTable(edOperations, fromMemory(pc++));
                if (op == null) {
                    op = nop;
                }
                break;

            case 0xdd:
                final int opCode2 = fromMemory(pc++);
                if (opCode2 == 0xcb) {
                    pc++;
                    op = fromOpTable(ddCbOperations, fromMemory(pc++));
                } else {
                    op = fromOpTable(ddOperations, opCode2);
                }
                break;

            case 0xfd:
                final int opCode3 = fromMemory(pc++);
                if (opCode3 == 0xcb) {
                    pc++;
                    op = fromOpTable(fdCbOperations, fromMemory(pc++));
                } else {
                    op = fromOpTable(fdOperations, opCode3);
                }
                break;

            default:
                op = fromOpTable(operations, opCode1);
                refreshes = 1;
                break;
        }

        rReg.increment(refreshes);
        pcReg.set(pc);
        return op;
    }

    private int fromMemory(final int addr) {
        final int val = memory.get(addr);
        clock.tick(1);
        return val;
    }

    private Operation fromOpTable(final Operation[] opTable, final int index) {
        return opTable[index];
    }

    public int fetchNextByte() {
        return memory.get(pcReg.getAndInc());
    }

    public int getInterruptMode() {
        return interruptMode;
    }

    public int fetchNextWord() {
        return Word.from(fetchNextByte(), fetchNextByte());
    }

    public int fetchRelative(final int offset) {
        return memory.get(pcReg.get() + offset);
    }

    public void pushByte(final int value) {
        memory.set(spReg.decAndGet(), value);
    }

    public int popByte() {
        return memory.get(spReg.getAndInc());
    }

    public void setIff(final int iff, final boolean value) {
        this.iffs[iff] = value;
    }

    public boolean getIff(final int iff) {
        return this.iffs[iff];
    }

    public void requestInterrupt() {
        interruptRequested = true;
    }

    public void cancelInterrupt() {
        interruptRequested = false;
    }

    public void nmi() {
        halting = false;
        iffs[0] = false;
        nmiResponseOp.execute();
    }

    public void enableInterrupts() {
        setIffState(false);
        enableIff = true;
    }

    private void setIffState(final boolean iffState) {
        iffs[0] = iffState;
        iffs[1] = iffState;
    }

    public void setInterruptMode(final int mode) {
        interruptMode = mode;
    }

    public void halt() {
        this.halting = true;
        pcReg.decAndGet();
    }

    public void dump(final PrintStream out) {
        if (lastOp != null) {
            out.println("Last operation: " + this.lastOp.toString());
        }
        out.println(String.format("af: %04x bc: %04x de: %04x hl: %04x",
                register("af").get(),
                register("bc").get(),
                register("de").get(),
                register("hl").get()));
        out.println(String.format("af':%04x bc':%04x de':%04x hl':%04x",
                register("af'").get(),
                register("bc'").get(),
                register("de'").get(),
                register("hl'").get()));
        out.println(String.format("ix: %04x iy: %04x pc: %04x sp: %04x ir:%02x%02x",
                register("ix").get(),
                register("iy").get(),
                register("pc").get(),
                register("sp").get(),
                register("i").get(),
                register("r").get()));
        out.println(String.format("iff1: %s  iff2: %s  im: %d",
                getIff(0) ? "True" : "False",
                getIff(1) ? "True" : "False",
                getInterruptMode()));
        out.println();
        out.flush();
    }

    public void reset() {
        for (Register reg: registers.values()) {
            reg.set(0);
        }

        enableIff = false;
        halting = false;
        iffs[0] = false;
        iffs[1] = false;
        interruptMode = 1;
        interruptRequested = false;
        lastOp = null;
    }

}
