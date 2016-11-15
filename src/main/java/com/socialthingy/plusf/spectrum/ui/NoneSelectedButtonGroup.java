package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;

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
