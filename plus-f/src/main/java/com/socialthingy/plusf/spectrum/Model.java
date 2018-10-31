package com.socialthingy.plusf.spectrum;

public enum Model {
    _48K("48K", 69888, 224, 64, 3500000, "/48.rom"),
    _128K("128K", 70908, 228, 63, 3546900, 8, 5, 2, 0, "/128-0.rom", "/128-1.rom"),
    _128K_SPANISH("128K Spanish", 70908, 228, 63, 3546900, 8, 5, 2, 0, "/128-spanish-0.rom", "/128-spanish-1.rom"),
    PLUS_2("+2", 70908, 228, 63, 3546900, 8, 5, 2, 0, "/plus2-0.rom", "/plus2-1.rom");

    public final String displayName;
    public final double tstateLengthMs;
    public final int tstatesPerRefresh;
    public final int ramPageCount;
    public final int screenPage;
    public final int midPage;
    public final int highPage;
    public final String[] romFileNames;
    public final int ticksPerScanline;
    public final int scanlinesBeforeDisplay;
    public final int clockFrequencyHz;

    Model(
        final String displayName,
        final int tstatesPerRefresh,
        final int ticksPerScanline,
        final int scanlinesBeforeDisplay,
        final int clockFrequencyHz,
        final int ramPageCount,
        final int screenPage,
        final int midPage,
        final int highPage,
        final String ... romFileNames
    ) {
        this.displayName = displayName;
        this.tstatesPerRefresh = tstatesPerRefresh;
        this.ticksPerScanline = ticksPerScanline;
        this.scanlinesBeforeDisplay = scanlinesBeforeDisplay;
        this.clockFrequencyHz = clockFrequencyHz;
        this.tstateLengthMs = 0.02 / tstatesPerRefresh;
        this.ramPageCount = ramPageCount;
        this.screenPage = screenPage;
        this.midPage = midPage;
        this.highPage = highPage;
        this.romFileNames = romFileNames;
    }

    Model(final String displayName, final int tstatesPerRefresh, final int ticksPerScanline, final int scanlinesBeforeDisplay, final int clockFrequencyHz, final String romFileName) {
        this.displayName = displayName;
        this.tstatesPerRefresh = tstatesPerRefresh;
        this.ticksPerScanline = ticksPerScanline;
        this.scanlinesBeforeDisplay = scanlinesBeforeDisplay;
        this.clockFrequencyHz = clockFrequencyHz;
        this.tstateLengthMs = 0.02 / tstatesPerRefresh;
        this.romFileNames = new String[] {romFileName};
        this.ramPageCount = 0;
        this.screenPage = 0;
        this.midPage = 0;
        this.highPage = 0;
    }
}
