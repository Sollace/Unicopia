package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.List;

import com.minelittlepony.common.client.gui.Tooltip;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.text.LiteralText;

public class ProfileTooltip {
    public static Tooltip get(Pony pony) {
        return () -> {
            return List.of(
                    new LiteralText(String.format("Level %d ", pony.getLevel().get() + 1)).append(pony.getSpecies().getDisplayName()).formatted(pony.getSpecies().getAffinity().getColor()),
                    new LiteralText(String.format("Mana: %d%%", (int)(pony.getMagicalReserves().getMana().getPercentFill() * 100))),
                    new LiteralText(String.format("Experience: %d", (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100))),
                    new LiteralText(String.format("Next level in: %d experience points", 100 - (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100)))
            );
        };
    }
}
