package com.minelittlepony.unicopia.container.inventory;

import java.util.List;

public interface SpellbookCraftingGrid {

    /**
     * Creates a hexagonal crafting grid.
     * @param grid   Output for normal slot positions.
     * @param gemPos Output for the gem slot position.
     */
    static void createGrid(List<int[]> grid, List<int[]> gemPos) {
        int cols = 4;
        int spacing = 23;

        int top = 34;
        int left = 65;

        for (int row = 0; row < 7; row++) {
            for (int i = 0; i < cols; i++) {

                int ring = 3;
                if (row == 0 || row == 6) {
                    ring = 1;
                } else if ((row == 1 || row == 5) && i > 0 && i < cols - 1) {
                    ring = 2;
                } else {
                    if (i == 0 || i == cols - 1) {
                        ring = 1;
                    } else if (i == 1 || i == cols - 2) {
                        ring = 2;
                    }
                }

                (row == 3 && i == 3 ? gemPos : grid).add(new int[] {
                        left + (i * spacing),
                        top,
                        row == 3 && i == 3 ? 4 : ring
                });
            }
            top += spacing * 0.9;
            left -= (spacing / 2) * (row > 2 ? -1 : 1);
            cols += row > 2 ? -1 : 1;
        }
    }

}
