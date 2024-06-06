package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import java.util.List;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

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

    static TooltipFactory of(Text line) {
        return (type, tooltip) -> tooltip.add(line);
    }

    default TooltipFactory conditionally(Predicate<SpellTraits> condition) {
        TooltipFactory self = this;
        return (type, tooltip) -> {
            if (condition.test(type.traits())) {
                self.appendTooltip(type, tooltip);
            }
        };
    }
}
