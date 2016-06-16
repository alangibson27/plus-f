package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;

import java.util.Iterator;

public interface TzxBlock {
    void write(RepeatingList<Bit> tape);

    class Bit {
        private final boolean state;
        private final String stage;

        public Bit(final boolean state, final String stage) {
            this.state = state;
            this.stage = stage;
        }

        public boolean getState() {
            return state;
        }

        public String getStage() {
            return stage;
        }

        public String toString() {
            return String.format("%d [%s]", state ? 1 : 0, stage);
        }
    }
}
