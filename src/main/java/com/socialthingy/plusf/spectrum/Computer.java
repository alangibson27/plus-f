package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.snapshot.SnapshotLoader;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.z80.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Computer implements InterruptingDevice {

    private static final Logger logger = Logger.getLogger(Computer.class.getName());
    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private final int[] memory;
    private final Timer processorExecuteTimer;
    private final Processor processor;
    private final MetricRegistry metricRegistry;
    private final int tstatesPerRefresh;
    private final ULA ula;

    private int currentCycleTstates;

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

    public void singleCycle() {
        currentCycleTstates = 0;
        final InterruptRequest interrupt = new InterruptRequest(this);
        processor.requestInterrupt(interrupt);

        final Timer.Context timer = processorExecuteTimer.time();
        ula.newCycle();
        try {
            while (currentCycleTstates < tstatesPerRefresh) {
                try {
                    processor.execute();
                } catch (ExecutionException ex) {
                    logger.warning(String.format("Processor error encountered: %s\n", ex.getCause().getMessage()));
                    logger.warning("Last operation:");
                    logger.warning(ex.getOperation().toString());
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Unrecoverable error encountered", ex);
                } finally {
                    if (currentCycleTstates == 0) {
                        processor.cancelInterrupt(interrupt);
                    }
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
}
