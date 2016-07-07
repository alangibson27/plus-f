package com.socialthingy.plusf.z80;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.operations.*;

import java.io.PrintStream;
import java.util.*;

public class Processor {
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
    private boolean enableIff = false;
    private boolean halting = false;
    private boolean iffs[] = new boolean[2];
    private int interruptMode = 1;
    private Deque<InterruptRequest> interruptRequests = new LinkedList<>();
    private final ByteRegister rReg = new ByteRegister("r");
    private final ByteRegister iReg = new ByteRegister("i");
    private final WordRegister pcReg = new WordRegister("pc");
    private final WordRegister spReg = new WordRegister("sp");
    private final FlagsRegister fReg = new FlagsRegister();
    private final OpRst im1ResponseOp;
    private final OpRst nmiResponseOp;
    private int lastTime;
    private Operation lastOp;
    private int lastPc;

    private int cyclesUntilBreak = -1;
    private Set<Integer> breakpoints = new HashSet<>();

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
            throw new IllegalStateException("Unimplemented operation");
        }

        if (breakpoints.contains(pc)) {
            cyclesUntilBreak = 0;
        }

        if (cyclesUntilBreak == 0) {
            debugConsole(pc, op);
        } else if (cyclesUntilBreak > 0) {
            cyclesUntilBreak--;
        }

        this.lastOp = op;
        this.lastPc = pc;
        try {
            this.lastTime = op.execute();

            if (enableIffAfterExecution) {
                enableIff = false;
                iffs[0] = true;
                iffs[1] = true;
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

            if ("clearbreak".equals(command)) {
                breakpoints.clear();
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
                }
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
        if (iffs[0] && !interruptRequests.isEmpty()) {
            incrementR();
            halting = false;
            final InterruptRequest request = interruptRequests.removeFirst();
            request.getDevice().acknowledge();

            if (interruptMode == 1) {
                return im1ResponseOp;
            } else {
                final int jumpBase = Word.from(rReg.get() & 0xfe, iReg.get());
                final int jumpLow = memory[jumpBase];
                final int jumpHigh = memory[(jumpBase + 1) & 0xffff];
                return new OpCallDirect(this, Word.from(jumpLow, jumpHigh));
            }
        } else {
            if (halting) {
                return operations[0x00];
            } else {
                int opCode = fetchNextOpcode();
                switch (opCode) {
                    case 0xcb:
                        return cbOperations[fetchNextOpcode()];

                    case 0xed:
                        final Operation op = edOperations[fetchNextOpcode()];
                        if (op == null) {
                            return nop;
                        } else {
                            return op;
                        }

                    case 0xdd:
                        opCode = fetchNextOpcode();
                        if (opCode == 0xcb) {
                            fetchNextByte();
                            return ddCbOperations[fetchNextByte()];
                        } else {
                            return ddOperations[opCode];
                        }

                    case 0xfd:
                        opCode = fetchNextOpcode();
                        if (opCode == 0xcb) {
                            fetchNextByte();
                            return fdCbOperations[fetchNextByte()];
                        } else {
                            return fdOperations[opCode];
                        }

                    default:
                        return operations[opCode];
                }
            }
        }
    }

    public int fetchNextOpcode() {
        incrementR();
        return memory[pcReg.getAndInc()];
    }

    private void incrementR() {
        final int rValue = rReg.get();
        rReg.set((rValue & 0b10000000) | ((rValue + 1) & 0b01111111));
    }

    public int fetchNextByte() {
        return memory[pcReg.getAndInc()];
    }

    public int getInterruptMode() {
        return interruptMode;
    }

    public int fetchNextWord() {
        return Word.from(fetchNextByte(), fetchNextByte());
    }

    public int fetchRelative(final int offset) {
        return memory[(pcReg.get() + offset) & 0xffff];
    }

    public void pushByte(final int value) {
        Memory.set(memory, spReg.decAndGet(), value);
    }

    public int popByte() {
        return memory[spReg.getAndInc()];
    }

    public void setIff(final int iff, final boolean value) {
        this.iffs[iff] = value;
    }

    public boolean getIff(final int iff) {
        return this.iffs[iff];
    }

    public void interrupt(final InterruptRequest request) {
        if (!interruptRequests.contains(request)) {
            interruptRequests.addLast(request);
        }
    }

    public void nmi() {
        halting = false;
        iffs[0] = false;
        nmiResponseOp.execute();
    }

    public void enableInterrupts() {
        enableIff = true;
    }

    public void setInterruptMode(final int mode) {
        interruptMode = mode;
    }

    public void halt() {
        this.halting = true;
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
    }

    public void startDebugging() {
        cyclesUntilBreak = 0;
    }
}
