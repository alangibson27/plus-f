package com.socialthingy.plusf.spectrum.display;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static com.socialthingy.plusf.spectrum.display.PixelMapper.*;
import static com.socialthingy.plusf.spectrum.display.Scaler2X.SCALE;
import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

public class DisplayComponent extends JComponent {
    public static final int TOP_BORDER_HEIGHT = 64;
    public static final int BOTTOM_BORDER_HEIGHT = 56;

    private static final int BORDER = 16;
    private static final int DISPLAY_WIDTH = (BORDER * SCALE) + (SCREEN_WIDTH * SCALE) + (BORDER * SCALE);
    private static final int DISPLAY_HEIGHT = (BORDER * SCALE) + (SCREEN_HEIGHT * SCALE) + (BORDER * SCALE);
    private static final Dimension DISPLAY_DIMENSIONS = new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT);
    private static final int SCALED_WIDTH = SCREEN_WIDTH * SCALE;
    private static final int SCALED_HEIGHT = SCREEN_HEIGHT * SCALE;
    private static final double INNER_OUTER_RATIO = 1.125;
    private static final double WIDTH_HEIGHT_RATIO = 576.0 / 432.0;

    private Dimension innerSize = new Dimension(SCREEN_WIDTH * SCALE, SCREEN_HEIGHT * SCALE);
    private Dimension outerSize = new Dimension(
        (int) (innerSize.width * INNER_OUTER_RATIO),
        (int) (innerSize.height * INNER_OUTER_RATIO)
    );
    private Point outerTopLeft = new Point(0, 0);
    private Point innerTopLeft = new Point((outerSize.width - innerSize.width) / 2, (outerSize.height - innerSize.height) / 2);
    protected final int[] imageDataBuffer;
    protected final int[] borderImageDataBuffer;
    private Object renderingHint = VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    private final BufferedImage borderImage;
    private final BufferedImage image;
    private boolean extendBorder;

    public DisplayComponent() {
        this.image = new BufferedImage(SCALED_WIDTH, SCALED_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.imageDataBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.borderImage = new BufferedImage(1, (BORDER * 2) + SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.borderImageDataBuffer = ((DataBufferInt) borderImage.getRaster().getDataBuffer()).getData();

        setPreferredSize(DISPLAY_DIMENSIONS);
        setMinimumSize(DISPLAY_DIMENSIONS);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                triggerResize();
            }
        });
    }

    public void setSmoothRendering(final boolean smoothRendering) {
        renderingHint = smoothRendering ? VALUE_INTERPOLATION_BILINEAR : VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    }

    private int roundTo2(final double value) {
        return ((int) value >> 1) << 1;
    }

    private void triggerResize() {
        final Dimension newSize = new Dimension(getSize().width, getSize().height - 16);
        outerSize = new Dimension(roundTo2(newSize.height * WIDTH_HEIGHT_RATIO), roundTo2(newSize.height));
        innerSize = new Dimension(
            roundTo2(outerSize.width / INNER_OUTER_RATIO),
            roundTo2(outerSize.height/ INNER_OUTER_RATIO)
        );
        outerTopLeft = new Point((newSize.width - outerSize.width) / 2, (newSize.height - outerSize.height) / 2);
        innerTopLeft = new Point(
            outerTopLeft.x + ((outerSize.width - innerSize.width) / 2),
            outerTopLeft.y + ((outerSize.height - innerSize.height) / 2)
        );
        if (extendBorder) {
            outerSize = new Dimension(newSize.width, newSize.height);
            outerTopLeft = new Point(0, 0);
        }
    }

    public void updateScreen(final int[] pixels) {
        Scaler2X.scale(pixels, imageDataBuffer);
    }

    public void updateBorder(final int[] borderColours) {
        final int[] fullBorder = new int[borderColours.length];
        int dest = 0;
        for (int colourIdx: borderColours) {
            fullBorder[dest++] = dullColour(colourIdx);
        }
        System.arraycopy(fullBorder, TOP_BORDER_HEIGHT - 16, borderImageDataBuffer, 0, borderImageDataBuffer.length);
    }

    public void setExtendBorder(final boolean extendBorder) {
        this.extendBorder = extendBorder;
        triggerResize();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(borderImage, outerTopLeft.x, outerTopLeft.y, outerSize.width, outerSize.height, null);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHint);
        g.drawImage(
                image,
                innerTopLeft.x,
                innerTopLeft.y,
                innerSize.width,
                innerSize.height,
                null
        );
    }
}
