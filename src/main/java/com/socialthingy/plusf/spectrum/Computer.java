package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.snapshot.SnapshotLoader;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.tzx.TzxBlock;
import com.socialthingy.plusf.z80.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Computer implements InterruptingDevice {

    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private Processor processor;
    private final int[] memory;
    private MetricRegistry metricRegistry;
    private final Timer processorExecuteTimer;
    private int tstatesPerRefresh;
    private int currentCycleTstates;
    private Iterator<TzxBlock.Bit> tape;
    private ULA ula;
    private boolean memoryProtectionEnabled;
    private Queue<InstructionRecord> recentInstructions = new LinkedList<>();

    public Computer(
        final Processor processor,
        final int[] memory,
        final Timings timings,
        final MetricRegistry metricRegistry
    ) {
        this.memory = memory;
        this.processor = processor;
        this.metricRegistry = metricRegistry;
        this.tstatesPerRefresh = timings.getTstatesPerRefresh();

        processorExecuteTimer = metricRegistry.timer(PROCESSOR_EXECUTE_TIMER_NAME);
    }

    public void setTape(final Iterator<TzxBlock.Bit> tape) {
        this.tape = tape;
        ula.earIn(tape.next().getState());
    }

    public void setUla(final ULA ula) {
        this.ula = ula;
    }

    public boolean toggleMemoryProtectionEnabled() {
        memoryProtectionEnabled = !memoryProtectionEnabled;
        return memoryProtectionEnabled;
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

    public int getCurrentCycleTstates() {
        return currentCycleTstates;
    }

    public void loadRom(final String romFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(romFile)) {
            int addr = 0;
            for (int next = fis.read(); next != -1; next = fis.read()) {
                memory[addr++] = next;
            }
        }
    }

    public int loadSnapshot(final File snapshotFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(snapshotFile)) {
            final SnapshotLoader sl = new SnapshotLoader(fis);
            return sl.read(processor, memory);
        }
    }

    private List<Integer> breakPoints = Arrays.asList(Integer.parseInt("11ef", 16));

    public void setBreakPoints(final List<Integer> breakPoints) {
        this.breakPoints = breakPoints;
    }

    public void singleCycle() {
        currentCycleTstates = 0;
        processor.interrupt(new InterruptRequest(this));

        final Timer.Context timer = processorExecuteTimer.time();
        recentInstructions.clear();
        try {
            while (currentCycleTstates < tstatesPerRefresh) {
                if (breakPoints.contains(processor.register("pc").get())) {
                    dump(System.out);
                }

                try {
                    final int addr = processor.register("pc").get();
                    final Operation executed = processor.execute();
                    recentInstructions.add(new InstructionRecord(addr, executed));
                } catch (Exception ex) {
                    System.out.printf("Processor error encountered: %s\n", ex.getMessage());
//                    System.out.println("Recent operations");
//                    recentInstructions.forEach(ir ->
//                        System.out.printf("%04x - %s\n", ir.addr, ir.op.getClass().getName())
//                    );
//                    System.out.println();
                }

                currentCycleTstates += processor.lastTime();
                if (tape != null) {
                    for (int i = processor.lastTime(); i > 0; i--) {
                        if (tape.hasNext()) {
                            ula.earIn(tape.next().getState());
                        }
                    }
                }
            }
        } finally {
            timer.stop();
        }
    }

    private int romHash() {
        int result = 1;

        for (int i = 0x0000; i < 0x4000; i++) {
            result = 31 * result + memory[i];
        }

        return result;
    }

    @Override
    public void acknowledge() {
    }

    private class InstructionRecord {
        private final int addr;
        private final Operation op;

        private InstructionRecord(final int addr, final Operation op) {
            this.addr = addr;
            this.op = op;
        }
    }
}
