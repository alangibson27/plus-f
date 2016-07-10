package com.socialthingy.plusf.spectrum.display;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Icons {
    public static final Image tape = new Image("/icons/tape.png");
    public static final Image rewindToStart = new Image("/icons/rewind-to-start.png");
    public static final Image play = new Image("/icons/play.png");
    public static final Image stop = new Image("/icons/stop.png");
    public static final Image fastForward = new Image("/icons/fast-forward.png");
    public static final Image rewind = new Image("/icons/rewind.png");

    public static final ImageView iconFrom(final Image image) {
        final ImageView icon = new ImageView(image);
        return icon;
    }
}
