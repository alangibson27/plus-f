package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.TapeListener;
import com.socialthingy.plusf.spectrum.UserPreferences;
import com.socialthingy.plusf.tape.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class TapeDialog extends JDialog implements TapeListener {
    private final TapeBlockModel blockModel = new TapeBlockModel();
    private final JList<PlayableBlock> blockList = new JList<>(blockModel);
    private final JButton jumpButton = new JButton("Jump");
    private final JButton stopBeforeButton = new JButton("Insert Stop");
    private final JButton removeAddedStopsButton = new JButton("Remove Stops");
    private final JButton rememberAddedStopsButton = new JButton("Save Stops");
    private Tape currentTape;

    public TapeDialog(final Window owner) {
        super(owner, "Jump to Block");

        blockList.setCellRenderer(new BlockListCellRenderer());
        blockList.addListSelectionListener(e -> {
            jumpButton.setEnabled(true);
            stopBeforeButton.setEnabled(true);
        });
        jumpButton.setEnabled(false);
        stopBeforeButton.setEnabled(false);
        stopBeforeButton.addActionListener(e -> {
            final PlayableBlock block = blockList.getSelectedValue();
            currentTape.addOverride(OverrideType.STOP_TAPE, block.getIndex());
            tapeChanged(currentTape);
        });
        removeAddedStopsButton.addActionListener(e -> {
            currentTape.getBlocks().removeIf(next -> next instanceof OverrideBlock);
            tapeChanged(currentTape);
        });
        rememberAddedStopsButton.addActionListener(e -> {
            final java.util.List<Integer> overrideBlockPositions = new ArrayList<>();
            for (int i = 0; i < currentTape.getBlocks().size(); i++) {
                if (currentTape.getBlocks().get(i) instanceof OverrideBlock) {
                    overrideBlockPositions.add(i);
                }
            }

            final UserPreferences prefs = new UserPreferences();
            prefs.saveOverridePositions(currentTape.getSignature(), overrideBlockPositions);
        });

        getContentPane().setLayout(new BorderLayout(4, 4));
        getContentPane().add(new JScrollPane(blockList), BorderLayout.CENTER);

        final JPanel controlPanel = new JPanel(new GridLayout(4, 1, 4, 4));
        controlPanel.add(jumpButton);
        controlPanel.add(stopBeforeButton);
        controlPanel.add(removeAddedStopsButton);
        controlPanel.add(rememberAddedStopsButton);

        final JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.add(controlPanel, BorderLayout.PAGE_START);
        getContentPane().add(sidePanel, BorderLayout.LINE_END);
    }

    public JButton getJumpButton() {
        return this.jumpButton;
    }

    public int getSelectedBlock() {
        if (blockList.getSelectedValue() != null) {
            return blockList.getSelectedValue().getIndex();
        }

        return -1;
    }

    @Override
    public void tapeChanged(Tape tape) {
        currentTape = tape;
        blockModel.clear();
        blockChanged(-1);
        if (tape != null) {
            blockModel.addAll(tape.getNavigableBlocks().stream().map(PlayableBlock::new).collect(Collectors.toList()));
            removeAddedStopsButton.setEnabled(true);
            rememberAddedStopsButton.setEnabled(true);
        } else {
            stopBeforeButton.setEnabled(false);
            jumpButton.setEnabled(false);
            removeAddedStopsButton.setEnabled(false);
            rememberAddedStopsButton.setEnabled(false);
        }
    }

    @Override
    public void blockChanged(int blockIndex) {
        for (int i = 0; i < blockModel.getSize(); i++) {
            final PlayableBlock block = blockModel.get(i);
            if (block.getIndex() == blockIndex) {
                blockModel.setCurrentBlock(blockIndex);
            }
        }
    }

    private static class BlockListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                final JList<?> list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus
        ) {
            final PlayableBlock selected = (PlayableBlock) list.getModel().getElementAt(index);
            final String text = selected.isOverrideBlock() ? String.format("(%s)", selected.getDisplayName()) : selected.getDisplayName();
            final Component rendered = super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            if (selected.isPlaying()) {
                rendered.setFont(rendered.getFont().deriveFont(Font.BOLD));
            }
            return rendered;
        }
    }
}

class TapeBlockModel extends DefaultListModel<PlayableBlock> {
    private int currentBlockIdx = -1;

    public void setCurrentBlock(final int blockIdx) {
        if (blockIdx != currentBlockIdx) {
            currentBlockIdx = blockIdx;

            for (int i = 0; i < super.getSize(); i++) {
                final PlayableBlock block = super.get(i);
                block.setPlaying(block.getIndex() == blockIdx);
                fireContentsChanged(this, 0, super.getSize() - 1);
            }
        }
    }
}

class PlayableBlock {
    private NavigableBlock block;
    private boolean playing;

    public PlayableBlock(final NavigableBlock block) {
        this.block = block;
    }

    public void setPlaying(final boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getDisplayName() {
        return block.getBlock().getDisplayName();
    }

    public int getIndex() {
        return block.getIndex();
    }

    public boolean isOverrideBlock() {
        return block.getBlock() instanceof OverrideBlock;
    }
}
