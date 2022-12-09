package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.List;

import com.minelittlepony.common.client.gui.Tooltip;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.text.Text;

public class ProfileTooltip {
    public static Tooltip get(Pony pony) {
        return () -> {
            return List.of(
                    Text.literal(String.format("Level %d ", pony.getLevel().get() + 1)).append(pony.getActualSpecies().getDisplayName()).formatted(pony.getSpecies().getAffinity().getColor()),
                    Text.literal(String.format("Mana: %d%%", (int)(pony.getMagicalReserves().getMana().getPercentFill() * 100))),
                    Text.literal(String.format("Corruption: %d%%", (int)(pony.getCorruption().getScaled(100)))),
                    Text.literal(String.format("Experience: %d", (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100))),
                    Text.literal(String.format("Next level in: %dxp", 100 - (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100)))
            );
        };
    }
}
