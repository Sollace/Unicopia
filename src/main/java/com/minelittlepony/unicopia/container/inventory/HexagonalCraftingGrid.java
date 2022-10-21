package com.minelittlepony.unicopia.container.inventory;

import java.util.List;

public interface HexagonalCraftingGrid {
    int SPACING = 23;

    /**
     * Creates a hexagonal crafting grid.
     * @param grid   Output for normal slot positions.
     * @param gemPos Output for the gem slot position.
     */
    static void create(int top, int left, int rings, List<Slot> grid, List<Slot> gemPos) {
        rings++;

        final int ROWS = (rings * 2) - 1;
        final int INFLECTION_POINT = ROWS / 2;

        int cols = rings;

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < cols; x++) {
                (y == 3 && x == 3 ? gemPos : grid).add(new Slot(
                        left + (x * SPACING),
                        top,
                        getWeight(x, y, cols, ROWS)
                ));
            }

            int change = y >= INFLECTION_POINT ? -1 : 1;

            top += SPACING * 0.9;
            left -= (SPACING / 2) * change;
            cols += change;
        }
    }

    static float getWeight(int x, int y, int cols, int rows) {
        if (y == 3 && x == 3) {
            return SpellbookSlot.CENTER_FACTOR;
        }

        if (y == 0 || y == (rows - 1) || x == 0 || x == (cols - 1)) {
            return SpellbookSlot.FAR_FACTOR;
        }
        if (y == 1 || y == (rows - 2) || x == 1 || x == (cols - 2)) {
            return SpellbookSlot.MIDDLE_FACTOR;
        }
        return SpellbookSlot.NEAR_FACTOR;
    }

    record Slot (
        int left,
        int top,
        float weight
    ) {}
}
