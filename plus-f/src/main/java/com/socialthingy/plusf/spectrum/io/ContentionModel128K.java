package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.z80.Clock;

public class ContentionModel128K extends ContentionModel48K {
    public ContentionModel128K(Clock clock, Model model, SpectrumMemory memory) {
        super(clock, model, memory);
    }

    @Override
    public void applyIOContention(int lowByte, int highByte) {
        if (highByte >= 0xc0 && memory.contendedAddress(0xc000)) {
            super.applyHighByteContendedIO(lowByte);
        } else {
            super.applyIOContention(lowByte, highByte);
        }
    }
}
