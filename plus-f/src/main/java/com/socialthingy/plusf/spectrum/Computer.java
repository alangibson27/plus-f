package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.spectrum.io.SpectrumMemory;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.z80.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.*;

public class Computer {
    private static final boolean DEBUG_ENABLED = true;//valueOf(System.getProperty("debugger", "false"));

    private static final Logger log = LoggerFactory.getLogger(Computer.class);
    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private final Timer processorExecuteTimer;
    private final Processor processor;
    private final MetricRegistry metricRegistry;
    private final ULA ula;
    private final SpectrumMemory memory;

    private int lastPc;
    private int lastTime;
    private Operation lastOp;
    private int cyclesUntilBreak = -1;
    private Set<Integer> breakpoints = new HashSet<>();
    private Set<Range> rangeBreakpoints = new HashSet<>();

    public Computer(
        final Processor processor,
        final SpectrumMemory memory,
        final ULA ula,
        final MetricRegistry metricRegistry
    ) {
        this.processor = processor;
        this.memory = memory;
        this.metricRegistry = metricRegistry;
        this.ula = ula;

        processorExecuteTimer = metricRegistry.timer(PROCESSOR_EXECUTE_TIMER_NAME);
    }

    protected void dump(final PrintStream out) {
        processor.dump(out);

        for (Map.Entry<String, Timer> timer: metricRegistry.getTimers().entrySet()) {
            out.printf(
                    "%s: count=%d avg=%f p99=%f max=%d rate=%f\n",
                    timer.getKey(),
                    timer.getValue().getCount(),
                    timer.getValue().getSnapshot().getMean() / 1000000,
                    timer.getValue().getSnapshot().get99thPercentile() / 1000000,
                    timer.getValue().getSnapshot().getMax() / 1000000,
                    timer.getValue().getOneMinuteRate()
            );
        }

        for (Map.Entry<String, Counter> counter: metricRegistry.getCounters().entrySet()) {
            out.printf("%s: count=%d\n", counter.getKey(), counter.getValue().getCount());
        }
    }

    public Processor getProcessor() {
        return processor;
    }

    public void singleCycle() {
        boolean newCycle = true;
        processor.requestInterrupt();

        final Timer.Context timer = processorExecuteTimer.time();
        ula.newCycle();
        try {
            while (ula.moreStatesUntilRefresh()) {
                final int ticksBefore = ula.getClock().getTicks();
                final int pc = processor.register("pc").get();
                final Operation op = processor.fetch();

                if (DEBUG_ENABLED) {
                    if (breakpoints.contains(pc)) {
                        cyclesUntilBreak = 0;
                    }

                    if (!rangeBreakpoints.isEmpty() && cyclesUntilBreak != 0) {
                        for (Range range : rangeBreakpoints) {
                            if (range.contains(pc)) {
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

                final int fetchSize = (processor.register("pc").get() - pc) & 0xffff;

                try {
                    processor.execute(op);
                } catch (ExecutionException ex) {
                    log.warn(String.format("Processor error encountered. Last operation: %s", ex.getOperation().toString()), ex);
                } catch (Exception ex) {
                    log.warn("Unrecoverable error encountered", ex);
                } finally {
                    if (newCycle) {
                        processor.cancelInterrupt();
                        newCycle = false;
                    }
                }

                final int expectedCyclesWithoutContention = processor.lastTime();
                final int expectedCyclesExcludingFetches = expectedCyclesWithoutContention - (4 * fetchSize);
                ula.getClock().tick(expectedCyclesExcludingFetches);
                lastTime = ula.getClock().getTicks() - ticksBefore;
                ula.advanceCycle(lastTime);
                lastOp = op;
                lastPc = pc;
            }
        } finally {
            timer.stop();
        }
    }

    public void reset() {
        cyclesUntilBreak = -1;
    }

    public boolean screenRedrawRequired() {
        return memory.screenChanged() || ula.flashStatusChanged();
    }

    public void markScreenDrawn() {
        memory.markScreenDrawn();
    }

    public int[] getDisplayMemory() {
        return memory.getDisplayMemory();
    }

    public boolean flashActive() {
        return ula.flashActive();
    }

    public boolean borderNeedsRedrawing() {
        return ula.borderNeedsRedrawing();
    }

    public int[] getBorderColours() {
        return ula.getBorderColours();
    }

    public int[] getScreenPixels() {
        return ula.getPixels();
    }

    public boolean ulaAccessed() {
        return ula.ulaAccessed();
    }

    public int peek(final int addr) {
        return memory.get(addr);
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

    private void debugConsole(final int pc, final Operation currentOp) {
        System.out.printf("Stopped at %04x\n", pc);
        final Scanner in = new Scanner(System.in);
        while (true) {
            System.out.printf("Last operation: [%04x] (at %d t-states) %s (took %d t-states)\n", lastPc, ula.getClock().getTicks(), lastOp != null ? lastOp.toString() : "", lastTime);
            System.out.printf("Next operation: [%04x] %s\n", pc, currentOp != null ? currentOp.toString() : "");
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

                processor.register(register).set(Integer.decode(value));
            }

            if ("last".equals(command)) {
                System.out.printf("Last operation: [%04x] %s (took %d t-states)\n", lastPc, lastOp.toString(), lastTime);
            }

            if ("skip".equals(command)) {
                cyclesUntilBreak = 1;
                break;
            }

//            if ("calcstack".equals(command)) {
//                printCalcStack();
//            }

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
                            memory.get(i),
                            memory.get(i + 1),
                            memory.get(i + 2),
                            memory.get(i + 3),
                            memory.get(i + 4),
                            memory.get(i + 5),
                            memory.get(i + 6),
                            memory.get(i + 7)
                    );
                }
            }

            if ("print".equals(command)) {
                final String reg = in.next();
                if (processor.register(reg) != null) {
                    System.out.printf("%04x\n", processor.register(reg).get());
                }
//                else if ("iff".equals(reg)) {
//                    System.out.printf("iff1: %d\n", processor.iffs[0] ? 0 : 1);
//                    System.out.printf("iff2: %d\n", iffs[1] ? 0 : 1);
//                }
            }

            if ("memsearch".equals(command)) {
                final int value = Integer.decode(in.next()) & 0xffff;
                final int v1 = value >> 8;
                final int v2 = value & 0xff;

                final List<Integer> results = new ArrayList<>();
                for (int i = 0; i < 0xffff; i++) {
                    if (memory.get(i) == v1 && memory.get(i + 1) == v2) {
                        results.add(i);
                    }
                }
                results.forEach(addr -> System.out.printf("%04x", addr));
            }
        }
    }

    public void startDebugging() {
        cyclesUntilBreak = 0;
    }
}
