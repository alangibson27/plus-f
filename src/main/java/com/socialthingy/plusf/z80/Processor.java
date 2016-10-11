package com.socialthingy.plusf.z80;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.operations.*;
import sun.misc.Unsafe;

import java.io.PrintStream;
import java.util.*;

import static java.lang.Boolean.valueOf;
import static sun.misc.Unsafe.ARRAY_OBJECT_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_OBJECT_INDEX_SCALE;

public class Processor {
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();
    private static final boolean DEBUG_ENABLED = valueOf(System.getProperty("debugger", "false"));

    private final Map<String, Register> registers = new HashMap<>();
    private final int[] memory;
    private final Operation[] operations;
    private final Operation[] edOperations;
    private final Operation[] cbOperations;
    private final Operation[] ddOperations;
    private final Operation[] ddCbOperations;
    private final Operation[] fdOperations;
    private final Operation[] fdCbOperations;

    private final Nop nop = new Nop();
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
    private Deque<InterruptRequest> interruptRequests = new LinkedList<>();
    private int lastTime;
    private Operation lastOp;
    private int lastPc;

    private int cyclesUntilBreak = -1;
    private Set<Integer> breakpoints = new HashSet<>();
    private Set<Range> rangeBreakpoints = new HashSet<>();

    public Processor(final int[] memory, final IO io) {
        this.memory = memory;

        prepareRegisters();
        operations = OperationTable.build(this, memory, io);
        edOperations = OperationTable.buildEdGroup(this, memory, io);
        cbOperations = OperationTable.buildCbGroup(this, memory);
        ddOperations = OperationTable.buildIndexedGroup(this, memory, (IndexRegister) registers.get("ix"));
        fdOperations = OperationTable.buildIndexedGroup(this, memory, (IndexRegister) registers.get("iy"));
        ddCbOperations = OperationTable.buildIndexedBitwiseGroup(this, memory, (IndexRegister) registers.get("ix"));
        fdCbOperations = OperationTable.buildIndexedBitwiseGroup(this, memory, (IndexRegister) registers.get("iy"));

        this.im1ResponseOp = new OpRst(this, 0x0038);
        this.nmiResponseOp = new OpRst(this, 0x0066);
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

    public int lastTime() {
        return lastTime;
    }

    public Operation execute() throws ExecutionException {
        final boolean enableIffAfterExecution = enableIff;
        final int pc = pcReg.get();
        final Operation op = fetch();
        if (op == null) {
            throw new IllegalStateException(String.format("Unimplemented operation at %d", pc));
        }

        if (DEBUG_ENABLED) {
            if (breakpoints.contains(pc)) {
                cyclesUntilBreak = 0;
            }

            if (!rangeBreakpoints.isEmpty() && cyclesUntilBreak != 0) {
                for (Range range : rangeBreakpoints) {
                    if (!range.contains(lastPc) && range.contains(pc)) {
                        cyclesUntilBreak = 0;
                        break;
                    }
                }
            }

            if (cyclesUntilBreak == 0) {
                debugConsole(pc, op);
            } else if (cyclesUntilBreak > 0) {
                cyclesUntilBreak--;
            }
        }

        this.lastOp = op;
        this.lastPc = pc;
        try {
            this.lastTime = op.execute();

            if (enableIffAfterExecution) {
                enableIff = false;
                setIffState(true);
            }

            return op;
        } catch (Exception ex) {
            throw new ExecutionException(op, ex);
        }
    }

    private void debugConsole(final int pc, final Operation currentOp) {
        // start at 0xeac0
        System.out.printf("Stopped at %04x\n", pc);
        final Scanner in = new Scanner(System.in);
        while (true) {
            System.out.printf("[%04x] %s\n", pc, lastOp != null ? currentOp.toString() : "");
            System.out.print("> ");
            final String command = in.next();
            if ("run".equals(command)) {
                cyclesUntilBreak = -1;
                break;
            }

            if (command.startsWith("n")) {
                if (in.hasNextInt()) {
                    cyclesUntilBreak = in.nextInt();
                }
                break;
            }

            if ("set".equals(command)) {
                final String register = in.next();
                final String value = in.next();

                register(register).set(Integer.decode(value));
            }

            if ("last".equals(command)) {
                System.out.printf("Last operation: [%04x] %s\n", lastPc, lastOp.toString());
            }

            if ("skip".equals(command)) {
                cyclesUntilBreak = 1;
                break;
            }

            if ("calcstack".equals(command)) {
                printCalcStack();
            }

            if ("break".equals(command)) {
                breakpoints.add(Integer.decode(in.next()));
            }

            if ("rangebreak".equals(command)) {
                rangeBreakpoints.add(new Range(Integer.decode(in.next()), Integer.decode(in.next())));
            }

            if ("clearbreak".equals(command)) {
                breakpoints.clear();
                rangeBreakpoints.clear();
            }

            if ("memory".equals(command)) {
                final int start = Integer.decode(in.next());
                final int end = Integer.decode(in.next());

                for (int i = start; i < end; i += 8) {
                    System.out.printf(
                        "%04x: %02x %02x %02x %02x %02x %02x %02x %02x\n",
                        i,
                        memory[i],
                        memory[(i + 1) & 0xffff],
                        memory[(i + 2) & 0xffff],
                        memory[(i + 3) & 0xffff],
                        memory[(i + 4) & 0xffff],
                        memory[(i + 5) & 0xffff],
                        memory[(i + 6) & 0xffff],
                        memory[(i + 7) & 0xffff]
                    );
                }
            }

            if ("print".equals(command)) {
                final String reg = in.next();
                if (register(reg) != null) {
                    System.out.printf("%04x\n", register(reg).get());
                } else if ("iff".equals(reg)) {
                    System.out.printf("iff1: %d\n", iffs[0] ? 0 : 1);
                    System.out.printf("iff2: %d\n", iffs[1] ? 0 : 1);
                }
            }

            if ("memsearch".equals(command)) {
                final int value = Integer.decode(in.next()) & 0xffff;
                final int v1 = value >> 8;
                final int v2 = value & 0xff;

                final List<Integer> results = new ArrayList<>();
                for (int i = 0; i < 0xffff; i++) {
                    if (memory[i] == v1 && memory[i + 1] == v2) {
                        results.add(i);
                    }
                }
                results.forEach(addr -> System.out.printf("%04x", addr));
            }
        }
    }

    private void printCalcStack() {
        final int stkBot = Word.from(memory[23651], memory[23652]);
        final int stkEnd = Word.from(memory[23653], memory[23654]);

        System.out.println("Calculator stack (from bottom):");
        for (int i = stkBot; i < stkEnd; i += 5) {
            System.out.printf(
                "[%04x] %02x %02x %02x %02x %02x\n",
                i,
                memory[i],
                memory[i + 1],
                memory[i + 2],
                memory[i + 3],
                memory[i + 4]
            );
        }
    }

    private Operation fetch() {
        final InterruptRequest interrupt = interruptRequests.peekFirst();

        if (iffs[0] && interrupt != null) {
            rReg.increment(1);
            if (halting) {
                pcReg.getAndInc();
                halting = false;
            }
            setIffState(false);
            interrupt.getDevice().acknowledge();

            if (interruptMode == 1) {
                return im1ResponseOp;
            } else {
                final int jumpBase = Word.from(0xff, iReg.get());
                final int jumpLow = UNSAFE.getInt(memory, 16L + ((jumpBase) * 4));
                final int jumpHigh = UNSAFE.getInt(memory, 16L + (((jumpBase + 1) & 0xffff) * 4));
                return new OpCallDirect(this, Word.from(jumpLow, jumpHigh));
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
        return UNSAFE.getInt(memory, 16L + addr * 4);
    }

    private Operation fromOpTable(final Operation[] opTable, final int index) {
        return (Operation) UNSAFE.getObject(opTable, (long) ARRAY_OBJECT_BASE_OFFSET + (ARRAY_OBJECT_INDEX_SCALE * index));
    }

    public int fetchNextByte() {
        return UNSAFE.getInt(memory, 16L + ((pcReg.getAndInc()) * 4));
    }

    public int getInterruptMode() {
        return interruptMode;
    }

    public int fetchNextWord() {
        return Word.from(fetchNextByte(), fetchNextByte());
    }

    public int fetchRelative(final int offset) {
        return UNSAFE.getInt(memory, 16L + (((pcReg.get() + offset) & 0xffff) * 4));
    }

    public void pushByte(final int value) {
        Memory.set(memory, spReg.decAndGet(), value);
    }

    public int popByte() {
        return UNSAFE.getInt(memory, 16L + ((spReg.getAndInc()) * 4));
    }

    public void setIff(final int iff, final boolean value) {
        this.iffs[iff] = value;
    }

    public boolean getIff(final int iff) {
        return this.iffs[iff];
    }

    public void requestInterrupt(final InterruptRequest request) {
        if (!interruptRequests.contains(request)) {
            interruptRequests.addLast(request);
        }
    }

    public void cancelInterrupt(final InterruptRequest request) {
        interruptRequests.remove(request);
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
        interruptRequests.clear();
        lastTime = 0;
        lastOp = null;
        lastPc = 0;
        cyclesUntilBreak = -1;
    }

    public void startDebugging() {
        cyclesUntilBreak = 0;
    }

    private class Range {
        private final int start;
        private final int end;

        Range(final int start, final int end) {
            this.start = start;
            this.end = end;
        }

        boolean contains(final int address) {
            return address >= start && address <= end;
        }
    }
}
