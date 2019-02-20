package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.spectrum.io.SpectrumMemory;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.z80.*;
import com.socialthingy.plusf.z80.operations.OpAndAImmediate;
import com.socialthingy.plusf.z80.operations.OpInA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Computer {
    private static final Logger log = LoggerFactory.getLogger(Computer.class);
    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private final Timer processorExecuteTimer;
    private final Processor processor;
    private final ULA ula;
    private final SpectrumMemory memory;
    private boolean dumping;
    private List<ExecutedOperation> executedOperations = new ArrayList<>();

    private int instructionsSinceLastUlaIn;
    private boolean probablyLoadingTape;

    public Computer(
        final Processor processor,
        final SpectrumMemory memory,
        final ULA ula,
        final MetricRegistry metricRegistry
    ) {
        this.processor = processor;
        this.memory = memory;
        this.ula = ula;

        processorExecuteTimer = metricRegistry.timer(PROCESSOR_EXECUTE_TIMER_NAME);
    }

    public Processor getProcessor() {
        return processor;
    }

    public void singleCycle() {
        boolean newCycle = true;
        probablyLoadingTape = false;
        processor.requestInterrupt();

        final Timer.Context timer = processorExecuteTimer.time();
        executedOperations.clear();

        ula.newCycle();
        try {
            while (ula.moreStatesUntilRefresh()) {
                final int ticksBefore = ula.getClock().getTicks();
                final ExecutedOperation executed = new ExecutedOperation();
                executed.startTime = ticksBefore;
                executed.address = processor.register("pc").get();
                try {
                    executed.operation = processor.execute();
                    executed.fromInterrupt = processor.fetchedFromInterrupt();
                } catch (ExecutionException ex) {
                    log.warn(String.format("Processor error encountered. Last operation: %s", ex.getOperation().toString()), ex);
                } catch (Exception ex) {
                    log.warn("Unrecoverable error encountered, cycle will be dumped", ex);
                    processor.register("pc").set(0);
                    dumping = true;
                    break;
                } finally {
                    if (newCycle) {
                        processor.cancelInterrupt();
                        newCycle = false;
                    }
                }

                if (executed.operation instanceof OpInA &&
                        processor.fetchRelative(-1) == 0xfe) {
                    instructionsSinceLastUlaIn = 0;
                } else {
                    instructionsSinceLastUlaIn++;
                }

                if (executed.operation instanceof OpAndAImmediate &&
                        (processor.fetchRelative(-1) == 0x20 || processor.fetchRelative(-1) == 0x40) &&
                        instructionsSinceLastUlaIn > 0 && instructionsSinceLastUlaIn <= 5) {
                    probablyLoadingTape = true;
                    instructionsSinceLastUlaIn = 0;
                }

                final int duration = ula.getClock().getTicks() - ticksBefore;
                executed.duration = duration;
                ula.advanceCycle(duration);

                executedOperations.add(executed);
            }
        } finally {
            timer.stop();
        }

        if (dumping) {
            final List<String> lines = executedOperations.stream().map(e -> String.format(
                    "[T+%d] 0x%04X (%d) %s %s",
                    e.startTime,
                    e.address,
                    e.duration,
                    e.fromInterrupt ? "I" : "N",
                    e.operation.toString()
            )).collect(Collectors.toList());
            try {
                Files.write(new File(String.format("/var/tmp/plusf-%d.dump", System.currentTimeMillis())).toPath(), lines);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            dumping = false;
        }
    }

    public void startDumping() {
        this.dumping = true;
    }

    public int[] getDisplayMemory() {
        return ula.getRenderedDisplayMemory();
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

    public boolean probablyLoadingTape() {
        return probablyLoadingTape;
    }

    public int peek(final int addr) {
        return memory.get(addr);
    }

    private class ExecutedOperation {
        private Operation operation;
        private int address;
        private int startTime;
        private int duration;
        private boolean fromInterrupt;
    }
}
