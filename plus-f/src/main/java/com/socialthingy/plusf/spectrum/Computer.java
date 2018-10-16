package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.snapshot.SnapshotLoader;
import com.socialthingy.plusf.spectrum.io.SpectrumMemory;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.z80.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class Computer {

    private static final Logger log = LoggerFactory.getLogger(Computer.class);
    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private final SpectrumMemory memory;
    private final Timer processorExecuteTimer;
    private final Processor processor;
    private final MetricRegistry metricRegistry;
    private final int tstatesPerRefresh;
    private final ULA ula;

    public Computer(
        final Processor processor,
        final ULA ula,
        final SpectrumMemory memory,
        final Model model,
        final MetricRegistry metricRegistry
    ) {
        this.memory = memory;
        this.processor = processor;
        this.metricRegistry = metricRegistry;
        this.tstatesPerRefresh = model.tstatesPerRefresh;
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

    public int loadSnapshot(final File snapshotFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(snapshotFile)) {
            final SnapshotLoader sl = new SnapshotLoader(fis);
            return sl.read(processor, memory);
        }
    }

    public void singleCycle() {
        int currentCycleTstates = 0;
        processor.requestInterrupt();

        final Timer.Context timer = processorExecuteTimer.time();
        ula.newCycle();
        try {
            while (currentCycleTstates < tstatesPerRefresh) {
                try {
                    processor.execute();
                } catch (ExecutionException ex) {
                    log.warn(String.format("Processor error encountered. Last operation: %s", ex.getOperation().toString()), ex);
                } catch (Exception ex) {
                    log.warn("Unrecoverable error encountered", ex);
                } finally {
                    if (currentCycleTstates == 0) {
                        processor.cancelInterrupt();
                    }
                }

                final int executedCycles = processor.lastTime();
                currentCycleTstates += executedCycles;
                ula.advanceCycle(executedCycles);
            }
        } finally {
            timer.stop();
        }
    }

    public void reset() {
    }
}
