package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;

import net.minecraft.text.Text;

public interface TooltipFactory {
    void appendTooltip(CustomisedSpellType<?> type, List<Text> tooltip);

    static TooltipFactory of(TooltipFactory...lines) {
        return (type, tooltip) -> {
            for (var line : lines) {
                line.appendTooltip(type, tooltip);
            }
        };
    }
}
