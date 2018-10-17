package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.io.SpectrumMemory;
import com.socialthingy.plusf.spectrum.io.ULA;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

import static com.socialthingy.plusf.spectrum.display.PixelMapper.*;
import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;

public class SwingDoubleSizeDisplay extends DisplayComponent {

    private static final double INNER_OUTER_RATIO = 1.125;
    private static final double WIDTH_HEIGHT_RATIO = 576.0 / 432.0;
    private static final int DEST_SCAN_WIDTH = SCREEN_WIDTH * SCALE * 2;
    private static final int SRC_SCAN_WIDTH = SCREEN_WIDTH + 2;

    private Dimension innerSize = new Dimension(SCREEN_WIDTH * SCALE, SCREEN_HEIGHT * SCALE);
    private Dimension outerSize = new Dimension(
        (int) (innerSize.width * INNER_OUTER_RATIO),
        (int) (innerSize.height * INNER_OUTER_RATIO)
    );

    private Point outerTopLeft = new Point(0, 0);
    private Point innerTopLeft = new Point((outerSize.width - innerSize.width) / 2, (outerSize.height - innerSize.height) / 2);

    private int roundTo2(final double value) {
        return ((int) (value / 2)) * 2;
    }

    public SwingDoubleSizeDisplay(final PixelMapper pixelMapper) {
        super(pixelMapper);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                triggerResize();
            }
        });
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

    @Override
    public void updateScreen(final SpectrumMemory memory, final ULA ula) {
        if (memory.screenChanged() || ula.flashStatusChanged()) {
            memory.markScreenDrawn();
            renderMemory(memory, ula.flashActive());
            System.arraycopy(targetPixels, 0, imageDataBuffer, 0, imageDataBuffer.length);
        }
    }

    @Override
    public void updateBorder(final ULA ula, final boolean force) {
        if (force || ula.borderNeedsRedrawing()) {
            final Iterator<Long> it = new ArrayList<>(ula.getBorderChanges()).iterator();
            long change = it.next();
            int colour = dullColour((int) change);
            int topLine = ((int) (change >> 32)) / TSTATES_PER_LINE;
            while (it.hasNext()) {
                change = it.next();
                int bottomLine = ((int) (change >> 32)) / TSTATES_PER_LINE;
                if (bottomLine > borderPixels.length) {
                    bottomLine = borderPixels.length;
                }
                for (int i = topLine; i < bottomLine; i++) {
                    borderPixels[i] = colour;
                }
                colour = dullColour((int) change);
                topLine = bottomLine;
            }
            for (int i = topLine; i < borderPixels.length; i++) {
                borderPixels[i] = colour;
            }
            System.arraycopy(borderPixels, TOP_BORDER_HEIGHT - BORDER, borderImageDataBuffer, 0, borderImageDataBuffer.length);
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
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

    @Override
    protected void scale(final int[] sourcePixels) {
        for (int x = 1; x < SCREEN_WIDTH + 1; x++) {
            int e0idx = (x - 1) * 2;
            int e1idx = e0idx + 1;
            int e2idx = e0idx + (SCREEN_WIDTH * SCALE);
            int e3idx = e2idx + 1;

            int pIdx = x + SCREEN_WIDTH + 2;
            int aIdx = x;
            int cIdx = pIdx - 1;
            int bIdx = pIdx + 1;
            int dIdx = pIdx + SCREEN_WIDTH + 2;

            for (int y = 1; y < SCREEN_HEIGHT + 1; y++) {
                final int a = sourcePixels[aIdx];
                final int c = sourcePixels[cIdx];
                final int p = sourcePixels[pIdx];
                final int b = sourcePixels[bIdx];
                final int d = sourcePixels[dIdx];
                aIdx += SRC_SCAN_WIDTH;
                cIdx += SRC_SCAN_WIDTH;
                pIdx += SRC_SCAN_WIDTH;
                bIdx += SRC_SCAN_WIDTH;
                dIdx += SRC_SCAN_WIDTH;

                targetPixels[e0idx] = (c == a && c != d && a != b) ? a : p;
                targetPixels[e1idx] = (a == b && a != c && b != d) ? b : p;
                targetPixels[e2idx] = (d == c && d != b && c != a) ? c : p;
                targetPixels[e3idx] = (b == d && b != a && d != c) ? d : p;

                e0idx += DEST_SCAN_WIDTH;
                e1idx += DEST_SCAN_WIDTH;
                e2idx += DEST_SCAN_WIDTH;
                e3idx += DEST_SCAN_WIDTH;
            }
        }
    }

    @Override
    public void setExtendBorder(final boolean extendBorder) {
        super.setExtendBorder(extendBorder);
        triggerResize();
    }
}
