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
import java.util.Map;

public class Computer {

    private static final Logger log = LoggerFactory.getLogger(Computer.class);
    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private final Timer processorExecuteTimer;
    private final Processor processor;
    private final MetricRegistry metricRegistry;
    private final int tstatesPerRefresh;
    private final ULA ula;
    private final SpectrumMemory memory;

    public Computer(
        final Processor processor,
        final SpectrumMemory memory,
        final ULA ula,
        final Model model,
        final MetricRegistry metricRegistry
    ) {
        this.processor = processor;
        this.memory = memory;
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

    public boolean ulaAccessed() {
        return ula.ulaAccessed();
    }

    public int peek(final int addr) {
        return memory.get(addr);
    }
}
