package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.Clock;

public class ContentionModelPlus2A extends ContentionModel48K {
    public ContentionModelPlus2A(final Clock clock, final SpectrumMemory memory) {
        super(clock, Model.PLUS_2A, memory);
    }

    @Override
    protected void handleContention(final Clock clock) {
        if (clock.getTicks() > firstTickOfDisplay &&
                clock.getTicks() < lastTickOfDisplay) {
            final int patternStart = clock.getTicks() - (firstTickOfDisplay + 1);
            if (patternStart % ticksPerScanline > 130) {
                return;
            }

            switch (patternStart % 8) {
                case 0:
                    clock.tick(1);
                    break;

                case 2:
                    clock.tick(7);
                    break;

                case 3:
                    clock.tick(6);
                    break;

                case 4:
                    clock.tick(5);
                    break;

                case 5:
                    clock.tick(4);
                    break;

                case 6:
                    clock.tick(3);
                    break;

                case 7:
                    clock.tick(2);
                    break;
            }
        }
    }

    @Override
    public void applyIOContention(int lowByte, int highByte) {
        clock.tick(4);
    }
}
