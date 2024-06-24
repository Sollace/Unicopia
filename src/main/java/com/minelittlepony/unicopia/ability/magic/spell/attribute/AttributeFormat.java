package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;

import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

public enum AttributeFormat {
    REGULAR {
        @Override
        public String formatValue(float value) {
            return ItemStack.MODIFIER_FORMAT.format(value);
        }
    },
    TIME {
        @Override
        public String formatValue(float value) {
            return StringHelper.formatTicks((int)Math.abs(value));
        }
    },
    PERCENTAGE {
        @Override
        public String formatValue(float value) {
            return ItemStack.MODIFIER_FORMAT.format((int)(Math.abs(value) * 100)) + "%";
        }
    };

    public abstract String formatValue(float value);

    public MutableText getBase(Text attributeName, float value, String comparison, Formatting color) {
        return formatAttributeLine(Text.translatable("attribute.modifier." + comparison + ".0", formatValue(value), attributeName).formatted(color));
    }

    public Text get(Text attributeName, float value) {
        return getBase(attributeName, value, "equals", Formatting.LIGHT_PURPLE);
    }

    public Text getRelative(Text attributeName, float baseValue, float currentValue, boolean detrimental) {
        float difference = currentValue - baseValue;
        return Text.literal(" (" + (difference > 0 ? "+" : "-") + formatValue(this == PERCENTAGE ? difference / baseValue : difference) + ")").formatted((detrimental ? difference : -difference) < 0 ? Formatting.DARK_GREEN : Formatting.RED);
    }

    static Text formatTraitDifference(Trait trait, float value) {
        boolean known = ItemTraitsTooltipRenderer.isKnown(trait);
        boolean canCast = UnicopiaClient.getClientPony() != null && UnicopiaClient.getClientPony().getObservedSpecies().canCast();
        Text name = canCast ? known
                ? trait.getName()
                : Text.translatable("spell_attribute.unicopia.added_trait.unknown").formatted(Formatting.YELLOW)
                : trait.getName().copy().formatted(Formatting.OBFUSCATED, Formatting.YELLOW);
        Text count = Text.literal(ItemStack.MODIFIER_FORMAT.format(value));
        return Text.translatable("spell_attribute.unicopia.added_trait." + ((value > 0) ? "plus" : "take"), name, count).formatted(Formatting.DARK_AQUA);
    }

    public static MutableText formatAttributeLine(Text attributeName) {
        return Text.literal(" ").append(attributeName).formatted(Formatting.LIGHT_PURPLE);
    }

}
