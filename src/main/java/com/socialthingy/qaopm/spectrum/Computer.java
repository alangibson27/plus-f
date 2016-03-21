package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.qaopm.snapshot.SnapshotLoader;
import com.socialthingy.qaopm.z80.InterruptRequest;
import com.socialthingy.qaopm.z80.InterruptingDevice;
import com.socialthingy.qaopm.z80.Processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class Computer implements InterruptingDevice {

    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private Processor processor;
    private final int[] memory;
    private int originalRomHash;
    private MetricRegistry metricRegistry;
    private final Timer processorExecuteTimer;
    private int tstatesPerRefresh;

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
        originalRomHash = romHash();
    }

    public void loadSnapshot(final File snapshotFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(snapshotFile)) {
            final SnapshotLoader sl = new SnapshotLoader(fis);
            sl.read(processor, memory);
        }
    }

    public void singleCycle() {
        int tstates = 0;
        processor.interrupt(new InterruptRequest(this));

        final Timer.Context timer = processorExecuteTimer.time();
        try {
            while (tstates < tstatesPerRefresh) {
                try {
                    processor.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if ("on".equals(System.getProperty("memoryprotection"))) {
                    if (romHash() != originalRomHash) {
                        processor.dump(System.out);
                        throw new IllegalStateException("ROM modification");
                    }
                }

                tstates += processor.lastTime();
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
}
