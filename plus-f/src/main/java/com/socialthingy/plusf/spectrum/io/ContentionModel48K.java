package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;

public class ContentionModel48K extends ContentionModel {
    protected final int firstTickOfDisplay;
    protected final int lastTickOfDisplay;
    protected final int ticksPerScanline;
    protected final SpectrumMemory memory;

    public ContentionModel48K(final Clock clock, final Model model, final SpectrumMemory memory) {
        super(clock);
        this.firstTickOfDisplay = model.scanlinesBeforeDisplay * model.ticksPerScanline;
        this.lastTickOfDisplay = (model.scanlinesBeforeDisplay + 192) * model.ticksPerScanline;
        this.ticksPerScanline = model.ticksPerScanline;
        this.memory = memory;
    }

    @Override
    public void applyContention(final int address, final int baseLength) {
        if (memory.contendedAddress(address)) {
            handleContention(clock);
        }
        clock.tick(baseLength);
    }

    @Override
    public void applyIOContention(final int lowByte, final int highByte) {
        if (highByte >= 0x40 && highByte < 0x80) {
            applyHighByteContendedIO(lowByte);
        } else {
            applyHighByteUncontendedIO(lowByte);
        }
    }

    protected void applyHighByteContendedIO(int lowByte) {
        if ((lowByte & 0b1) == 0) {
            handleContention(clock);
            clock.tick(1);
            handleContention(clock);
            clock.tick(3);
        } else {
            handleContention(clock);
            clock.tick(1);
            handleContention(clock);
            clock.tick(1);
            handleContention(clock);
            clock.tick(1);
            handleContention(clock);
            clock.tick(1);
        }
    }

    private void applyHighByteUncontendedIO(int lowByte) {
        if ((lowByte & 0b1) == 0) {
            clock.tick(1);
            handleContention(clock);
            clock.tick(3);
        } else {
            clock.tick(4);
        }
    }

    protected void handleContention(final Clock clock) {
        if (clock.getTicks() >= firstTickOfDisplay &&
                clock.getTicks() < lastTickOfDisplay) {
            final int patternStart = clock.getTicks() - (firstTickOfDisplay - 1);
            if (patternStart % ticksPerScanline > 127) {
                return;
            }

            switch (patternStart % 8) {
                case 0:
                    clock.tick(6);
                    break;

                case 1:
                    clock.tick(5);
                    break;

                case 2:
                    clock.tick(4);
                    break;

                case 3:
                    clock.tick(3);
                    break;

                case 4:
                    clock.tick(2);
                    break;

                case 5:
                    clock.tick(1);
                    break;
            }
        }
    }
}
