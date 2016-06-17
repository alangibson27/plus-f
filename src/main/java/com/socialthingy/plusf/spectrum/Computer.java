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
import java.util.logging.Logger;

public class Computer implements InterruptingDevice {

    private static final Logger logger = Logger.getLogger(Computer.class.getName());
    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private final int[] memory;
    private final Timer processorExecuteTimer;
    private final Processor processor;
    private final MetricRegistry metricRegistry;
    private final int tstatesPerRefresh;
    private final Queue<InstructionRecord> recentInstructions = new LinkedList<>();
    private final ULA ula;

    private int currentCycleTstates;
    private boolean memoryProtectionEnabled;

    public Computer(
        final Processor processor,
        final ULA ula,
        final int[] memory,
        final Timings timings,
        final MetricRegistry metricRegistry
    ) {
        this.memory = memory;
        this.processor = processor;
        this.metricRegistry = metricRegistry;
        this.tstatesPerRefresh = timings.getTstatesPerRefresh();
        this.ula = ula;

        processorExecuteTimer = metricRegistry.timer(PROCESSOR_EXECUTE_TIMER_NAME);
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
        ula.newCycle();
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
                    logger.warning(String.format("Processor error encountered: %s\n", ex.getMessage()));
                    logger.fine("Recent operations:");
                    recentInstructions.forEach(ir ->
                        logger.fine(String.format("%04x - %s\n", ir.addr, ir.op.getClass().getName()))
                    );
                }

                currentCycleTstates += processor.lastTime();
                ula.advanceCycle(processor.lastTime());
            }
        } finally {
            timer.stop();
        }
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
