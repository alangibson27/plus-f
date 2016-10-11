package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.TapePlayer;

import javax.swing.*;
import java.awt.*;

public class TapeControls extends JPanel {
    public TapeControls(final TapePlayer tapePlayer) {
        final Dimension prefSize = new Dimension(32, 32);
        final JToggleButton playButton = new JToggleButton(Icons.play);
        playButton.setModel(tapePlayer.getPlayButtonModel());
        playButton.setPreferredSize(prefSize);
        playButton.setFocusable(false);

        final JButton rewindToStartButton = new JButton(Icons.rewindToStart);
        rewindToStartButton.setModel(tapePlayer.getRewindToStartButtonModel());
        rewindToStartButton.setPreferredSize(prefSize);
        rewindToStartButton.setFocusable(false);

        final JButton stopButton = new JButton(Icons.stop);
        stopButton.setModel(tapePlayer.getStopButtonModel());
        stopButton.setPreferredSize(prefSize);
        stopButton.setFocusable(false);

        final ButtonGroup playButtonGroup = new NoneSelectedButtonGroup();
        playButtonGroup.add(playButton);

        stopButton.setEnabled(false);
        rewindToStartButton.setEnabled(false);
        playButton.setEnabled(false);

        tapePlayer.getStopButtonModel().addActionListener(action -> playButtonGroup.clearSelection());
        tapePlayer.getRewindToStartButtonModel().addActionListener(action -> playButtonGroup.clearSelection());

        add(playButton);
        add(stopButton);
        add(rewindToStartButton);
    }

    public class NoneSelectedButtonGroup extends ButtonGroup {
        @Override
        public void setSelected(final ButtonModel model, final boolean selected) {
            if (selected) {
                super.setSelected(model, selected);
            } else {
                clearSelection();
            }
        }
    }
}