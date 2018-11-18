package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.sound.Beeper;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.spectrum.TapePlayer;
import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.z80.IO;

import static com.socialthingy.plusf.spectrum.display.DisplayComponent.BOTTOM_BORDER_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.DisplayComponent.TOP_BORDER_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.PixelMapper.SCREEN_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.PixelMapper.SCREEN_WIDTH;

public class ULA implements IO {
    private final TapePlayer tapePlayer;
    private final Keyboard keyboard;
    private final Clock clock;
    private final Beeper beeper;
    private final int ticksPerScanline;
    private final int scanlinesBeforeDisplay;
    private final int ticksPerCycle;

    private int earBit;
    private int tapeCyclesAdvanced;
    private int cyclesSinceBeeperUpdate;
    protected boolean flashActive = false;
    private int cyclesUntilFlashChange = 16;

    private int currentBorderColour;
    protected int[] borderColours = new int[TOP_BORDER_HEIGHT + PixelMapper.SCREEN_HEIGHT + BOTTOM_BORDER_HEIGHT];
    protected boolean borderColourChanged = true;
    private int unchangedBorderCycles = 0;

    private int lastScanlineRendered;
    private final PixelMapper pixelMapper;
    private final int[] pixels = new int[(SCREEN_WIDTH + 2) * (SCREEN_HEIGHT + 2)];
    private final SpectrumMemory memory;

    private boolean ulaAccessed = false;
    private boolean beeperIsOn = false;

    public ULA(final SpectrumMemory memory, final Keyboard keyboard, final TapePlayer tapePlayer, final Beeper beeper, final Clock clock, final Model model) {
        this.memory = memory;
        this.clock = clock;
        this.keyboard = keyboard;
        this.tapePlayer = tapePlayer;
        this.beeper = beeper;
        this.ticksPerScanline = model.ticksPerScanline;
        this.scanlinesBeforeDisplay = model.scanlinesBeforeDisplay;
        this.lastScanlineRendered = scanlinesBeforeDisplay;
        this.ticksPerCycle = model.tstatesPerRefresh;
        this.pixelMapper = new PixelMapper(scanlinesBeforeDisplay);
    }

    public Clock getClock() {
        return clock;
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
        memory.resetDisplayMemory();
        ulaAccessed = false;
        lastScanlineRendered = scanlinesBeforeDisplay - 1;
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
        final int scanline = clock.getTicks() / ticksPerScanline;
        if (scanline < borderColours.length) {
            borderColours[scanline] = currentBorderColour;
        }

        if (scanline != lastScanlineRendered &&
                scanline > scanlinesBeforeDisplay && scanline <= scanlinesBeforeDisplay + 192) {
            for (int sl = lastScanlineRendered; sl < scanline; sl++) {
                pixelMapper.renderScanline(memory.getDisplayMemory(), pixels, sl, flashActive);
            }
        }
        lastScanlineRendered = scanline;

        cyclesSinceBeeperUpdate += tstates;
        if (cyclesSinceBeeperUpdate >= beeper.getUpdatePeriod()) {
            cyclesSinceBeeperUpdate = cyclesSinceBeeperUpdate - (int) beeper.getUpdatePeriod();
            beeper.update(beeperIsOn);
        }

        if (tapePlayer.isPlaying()) {
            tapeCyclesAdvanced += tstates;
        }
    }

    public int[] getPixels() {
        return pixels;
    }

    public void reset() {
        clock.reset();
        unchangedBorderCycles = 0;
        earBit = 0;
        tapeCyclesAdvanced = 0;
        flashActive = false;
        cyclesUntilFlashChange = 16;
        lastScanlineRendered = scanlinesBeforeDisplay;
    }

    public boolean moreStatesUntilRefresh() {
        return clock.getTicks() < ticksPerCycle;
    }
}
