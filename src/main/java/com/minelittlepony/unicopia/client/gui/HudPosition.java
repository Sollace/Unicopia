package com.minelittlepony.unicopia.client.gui;

import net.minecraft.util.math.MathHelper;

public enum HudPosition {
    MAIN_HAND(Alignment.UNSET, Alignment.UNSET),
    OFF_HAND(Alignment.UNSET, Alignment.UNSET),

    TOP_LEFT(Alignment.START, Alignment.START),
    TOP_CENTER(Alignment.MIDDLE, Alignment.START),
    TOP_RIGHT(Alignment.END, Alignment.START),

    CENTER_LEFT(Alignment.START, Alignment.MIDDLE),
    CENTER_CENTER(Alignment.MIDDLE, Alignment.MIDDLE),
    CENTER_RIGHT(Alignment.END, Alignment.MIDDLE),

    BOTTOM_LEFT(Alignment.START, Alignment.END),
    BOTTOM_CENTER(Alignment.MIDDLE, Alignment.END),
    BOTTOM_RIGHT(Alignment.END, Alignment.END);

    private final Alignment horizontal;
    private final Alignment vertical;

    HudPosition(Alignment horizontal, Alignment vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public Alignment getHorizontal() {
        return horizontal;
    }

    public Alignment getVertical() {
        return vertical;
    }

    public enum Alignment {
        UNSET,
        START,
        MIDDLE,
        END;

        public int getSignum() {
            return this == UNSET ? 0 : this == START ? -1 : 1;
        }

        public Alignment opposite() {
            return (this == UNSET || this == MIDDLE) ? this : this == START ? END : START;
        }

        public Alignment or(Alignment fallback) {
            return this == UNSET ? fallback : this;
        }

        public int pick(int start, int middle, int end, int unset) {
            return switch(this) {
                case UNSET -> unset;
                case START -> start;
                case MIDDLE -> middle;
                case END -> end;
            };
        }

        public int pick(int start, int end, int unset) {
            return switch(this) {
                case UNSET -> unset;
                case START -> start;
                case MIDDLE -> MathHelper.lerp(0.5F, start, end);
                case END -> end;
            };
        }
    }
}
