package com.minelittlepony.unicopia.toxin;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.item.UEffects;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public enum Toxicity implements Toxin {
    SAFE(0, 0),
    MILD(1, 160),
    FAIR(1, 30),
    SEVERE(5, 160),
    LETHAL(10, 900);

    private final int level;
    private final int duration;

    private static final Map<String, Toxicity> REGISTRY = Arrays.stream(values()).collect(Collectors.toMap(Toxicity::name, Function.identity()));

    Toxicity(int level, int duration) {
        this.level = level;
        this.duration = duration;
    }

    public boolean isMild() {
        return this == MILD;
    }

    public boolean toxicWhenRaw() {
        return isLethal() || this != SAFE;
    }

    public boolean toxicWhenCooked() {
        return isLethal() || this == SEVERE;
    }

    public boolean isLethal() {
        return this == LETHAL;
    }

    public String getTranslationKey() {
        return String.format("toxicity.%s.name", name().toLowerCase());
    }

    public Text getTooltip() {
        Text text = new TranslatableText(getTranslationKey());
        text.getStyle().setColor(toxicWhenCooked() ? Formatting.RED : toxicWhenRaw() ? Formatting.DARK_PURPLE : Formatting.GRAY);
        return text;
    }

    public ItemStack ontoStack(ItemStack stack) {
        stack.getOrCreateTag().putString("toxicity", name());
        return stack;
    }

    @Override
    public void afflict(PlayerEntity player, Toxicity toxicity, ItemStack stack) {
        if (toxicWhenRaw()) {
            player.addStatusEffect(new StatusEffectInstance(isMild() ? StatusEffects.NAUSEA : StatusEffects.POISON, duration, level));
        }

        if (isLethal()) {
            player.addStatusEffect(new StatusEffectInstance(UEffects.FOOD_POISONING, 300, 7, false, false));
        } else if (toxicWhenCooked()) {
            WEAK_NAUSEA.afflict(player, toxicity, stack);
        }
    }

    public static Toxicity fromStack(ItemStack stack) {
        if (stack.hasTag()) {
            Tag tag = stack.getTag().get("toxicity");
            if (tag != null) {
                return REGISTRY.getOrDefault(tag.asString(), SAFE);
            }
        }
        return SAFE;
    }

    public static Toxicity byName(String name) {
        return REGISTRY.get(name.toUpperCase());
    }
}
