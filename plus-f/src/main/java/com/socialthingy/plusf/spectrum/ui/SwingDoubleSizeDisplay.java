package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.Memory;
import sun.misc.Unsafe;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

import static com.socialthingy.plusf.spectrum.display.PixelMapper.*;
import static com.socialthingy.plusf.spectrum.display.SpectrumColour.dullColour;
import static com.socialthingy.plusf.util.UnsafeUtil.BASE;

public class SwingDoubleSizeDisplay extends DisplayComponent {
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    private static final double INNER_OUTER_RATIO = 1.125;
    private static final double WIDTH_HEIGHT_RATIO = 576.0 / 432.0;
    private static final int SCAN_WIDTH = SCREEN_WIDTH * SCALE * 2;

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
    public void updateScreen(final int[] memory, final ULA ula) {
        if (Memory.screenChanged() || ula.flashStatusChanged()) {
            Memory.markScreenDrawn();
            renderMemory(Memory.getScreenBytes(memory), ula.flashActive());
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
            System.arraycopy(borderPixels, 0, borderImageDataBuffer, 0, borderImageDataBuffer.length);
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
            for (int y = 1; y < SCREEN_HEIGHT + 1; y++) {
                final int a = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x, y - 1) * UnsafeUtil.SCALE));
                final int c = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x - 1, y) * UnsafeUtil.SCALE));
                final int p = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x, y) * UnsafeUtil.SCALE));
                final int b = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x + 1, y) * UnsafeUtil.SCALE));
                final int d = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x, y + 1) * UnsafeUtil.SCALE));

                final int e0 = (c == a && c != d && a != b) ? a : p;
                final int e1 = (a == b && a != c && b != d) ? b : p;
                final int e2 = (d == c && d != b && c != a) ? c : p;
                final int e3 = (b == d && b != a && d != c) ? d : p;

                unsafe.putInt(targetPixels, BASE + (e0idx * UnsafeUtil.SCALE), e0);
                unsafe.putInt(targetPixels, BASE + (e1idx * UnsafeUtil.SCALE), e1);
                unsafe.putInt(targetPixels, BASE + (e2idx * UnsafeUtil.SCALE), e2);
                unsafe.putInt(targetPixels, BASE + (e3idx * UnsafeUtil.SCALE), e3);

                e0idx += SCAN_WIDTH;
                e1idx += SCAN_WIDTH;
                e2idx += SCAN_WIDTH;
                e3idx += SCAN_WIDTH;
            }
        }
    }

    @Override
    public void setExtendBorder(final boolean extendBorder) {
        super.setExtendBorder(extendBorder);
        triggerResize();
    }
}
