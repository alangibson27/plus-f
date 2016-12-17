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

public class SwingDoubleSizeDisplay extends DisplayComponent {
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    private static final double INNER_OUTER_RATIO = 1.125;
    private static final double WIDTH_HEIGHT_RATIO = 576.0 / 432.0;

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
            }
        });
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
            final Iterator<Long> it = ula.getBorderChanges().iterator();
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
        g.drawImage(borderImage, outerTopLeft.x, outerTopLeft.y, outerSize.width, outerSize.height, null);
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
        for (int x = 0; x < SCREEN_WIDTH; x++) {
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                final int b = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x, y - 1) * 4));
                final int d = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x - 1, y) * 4));
                final int e = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x, y) * 4));
                final int f = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x + 1, y) * 4));
                final int h = unsafe.getInt(sourcePixels, 16L + (sourcePixelAt(x, y + 1) * 4));

                final int e0;
                final int e1;
                final int e2;
                final int e3;
                if (b != h && d != f) {
                    e0 = d == b ? d : e;
                    e1 = b == f ? f : e;
                    e2 = d == h ? d : e;
                    e3 = h == f ? f : e;
                } else {
                    e0 = e;
                    e1 = e;
                    e2 = e;
                    e3 = e;
                }

                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 0, 0) * 4), e0);
                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 1, 0) * 4), e1);
                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 0, 1) * 4), e2);
                unsafe.putInt(targetPixels, 16L + (targetPixelAt(x, y, 1, 1) * 4), e3);
            }
        }
    }
}
