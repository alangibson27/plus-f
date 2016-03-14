package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.qaopm.snapshot.SnapshotLoader;
import com.socialthingy.qaopm.z80.InterruptRequest;
import com.socialthingy.qaopm.z80.InterruptingDevice;
import com.socialthingy.qaopm.z80.Processor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Computer implements InterruptingDevice {

    public static final double SCREEN_REFRESHES_PER_SECOND = 50.0;
    private static final double CLOCK_CYCLES_PER_SECOND = 3500000.0;
    private static final double CLOCK_CYCLES_PER_T_STATE = 2.0;

    private static final int T_STATES_PER_REFRESH =
            (int) ((1 / SCREEN_REFRESHES_PER_SECOND) / (1 / (CLOCK_CYCLES_PER_SECOND / CLOCK_CYCLES_PER_T_STATE)));

    private static final String PROCESSOR_EXECUTE_TIMER_NAME = "processor.execute";

    private final Processor processor;
    private final ULA ula;
    private final int[] memory;
    private int originalRomHash;
    private final Timer processorExecuteTimer;

    public Computer(final int[] memory, final MetricRegistry metricRegistry) {
        this.memory = memory;
        ula = new ULA();
        processor = new Processor(memory, ula);
        processorExecuteTimer = metricRegistry.timer(PROCESSOR_EXECUTE_TIMER_NAME);
    }

    protected void dump(final PrintStream out) {
        processor.dump(out);

        out.printf(
                "Processor execute: count=%d avg=%f p99=%f max=%d rate=%f\n",
                processorExecuteTimer.getCount(),
                processorExecuteTimer.getSnapshot().getMean() / 1000000,
                processorExecuteTimer.getSnapshot().get99thPercentile() / 1000000,
                processorExecuteTimer.getSnapshot().getMax() / 1000000,
                processorExecuteTimer.getOneMinuteRate()
        );
    }

    public ULA getUla() {
        return ula;
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

    public void loadSnapshot(final String snapshotFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(snapshotFile)) {
            final SnapshotLoader sl = new SnapshotLoader(fis);
            sl.read(processor, memory);
        }
    }

    public void singleCycle() {
        int tStates = 0;
        processor.interrupt(new InterruptRequest(this));

        final Timer.Context timer = processorExecuteTimer.time();
        try {
            while (tStates < T_STATES_PER_REFRESH) {
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

                tStates += processor.lastTime();
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
