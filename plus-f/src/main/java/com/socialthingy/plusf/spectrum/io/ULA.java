package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.sound.Beeper;
import com.socialthingy.plusf.spectrum.Clock;
import com.socialthingy.plusf.spectrum.TapePlayer;
import com.socialthingy.plusf.z80.IO;

public class ULA implements IO {
    private final TapePlayer tapePlayer;
    private final Keyboard keyboard;

    private int earBit;
    private int tapeCyclesAdvanced;
    private final Clock clock;
    private int cyclesSinceBeeperUpdate;
    protected boolean flashActive = false;
    private int cyclesUntilFlashChange = 16;
    private int currentBorderColour;
    private int[] borderColours = new int[64 + 192 + 52];
    private boolean borderColourChanged = true;
    private int unchangedBorderCycles = 0;
    private boolean ulaAccessed = false;
    private boolean beeperIsOn = false;
    private final Beeper beeper;

    public ULA(final Keyboard keyboard, final TapePlayer tapePlayer, final Beeper beeper, Clock clock) {
        this.clock = clock;
        this.keyboard = keyboard;
        this.tapePlayer = tapePlayer;
        this.beeper = beeper;
    }

    public boolean ulaAccessed() {
        return ulaAccessed;
    }

    @Override
    public boolean recognises(final int low, final int high) {
        return (low & 0b1) == 0;
    }

    @Override
    public int read(int low, int high) {
        ulaAccessed = true;
        if (tapeCyclesAdvanced > 0) {
            earBit = tapePlayer.skip(tapeCyclesAdvanced) ? 1 << 6 : 0;
            beeperIsOn = earBit == 0;
            tapeCyclesAdvanced = 0;
        }
        return keyboard.readKeyboard(high) | earBit;
    }

    @Override
    public void write(int low, int high, int value) {
        if (low == 0xfe) {
            final int newBorderColour = value & 0b111;
            if (currentBorderColour != newBorderColour) {
                unchangedBorderCycles = 0;
                borderColourChanged = true;
                currentBorderColour = newBorderColour;
            }

            beeperIsOn = (value & 0b10000) == 0;
        }
    }

    public void setBorderColour(final int borderColour) {
        this.currentBorderColour = borderColour & 0b111;
    }

    public void newCycle() {
        if (!borderColourChanged) {
            unchangedBorderCycles++;
        }
        cyclesUntilFlashChange--;
        if (cyclesUntilFlashChange == 0) {
            cyclesUntilFlashChange = 16;
            flashActive = !flashActive;
        }
        clock.reset();
        ulaAccessed = false;
    }

    public boolean borderNeedsRedrawing() {
        return unchangedBorderCycles < 2 && borderColourChanged;
    }

    public boolean flashStatusChanged() {
        return cyclesUntilFlashChange == 16;
    }

    public boolean flashActive() {
        return flashActive;
    }

    public int[] getBorderColours() {
        return borderColours;
    }

    public void advanceCycle(final int tstates) {
        cyclesSinceBeeperUpdate += tstates;
        if (cyclesSinceBeeperUpdate >= beeper.getUpdatePeriod()) {
            cyclesSinceBeeperUpdate = cyclesSinceBeeperUpdate - (int) beeper.getUpdatePeriod();
            beeper.update(beeperIsOn);
        }

        final int scanline = clock.getTicks() / 224;
        if (scanline < borderColours.length) {
            borderColours[scanline] = currentBorderColour;
        }

        clock.tick(tstates);
        if (tapePlayer.isPlaying()) {
            tapeCyclesAdvanced += tstates;
        }
    }

    public void reset() {
        clock.reset();
        unchangedBorderCycles = 0;
        earBit = 0;
        tapeCyclesAdvanced = 0;
        flashActive = false;
        cyclesUntilFlashChange = 16;
    }
}
