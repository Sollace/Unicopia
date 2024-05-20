package com.minelittlepony.unicopia.client.gui;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;

public class HudEffects {

    private static boolean addedHunger;
    private static Set<TagKey<Fluid>> originalTags = null;

    public static void tryApply(@Nullable PlayerEntity player, float tickDelta, boolean on) {
        if (player != null) {
            apply(Pony.of(player), tickDelta, on);
        }
    }

    private static void apply(Pony pony, float tickDelta, boolean on) {
        if (on) {
            if (!pony.asEntity().hasStatusEffect(StatusEffects.HUNGER) && pony.asEntity().hasStatusEffect(UEffects.FOOD_POISONING)) {
                addedHunger = true;
                pony.asEntity().addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 1, 1, false, false));
            }
        } else {
            if (addedHunger) {
                addedHunger = false;
                pony.asEntity().removeStatusEffect(StatusEffects.HUNGER);
            }
        }

        if (pony.getCompositeRace().includes(Race.SEAPONY)) {
            Set<TagKey<Fluid>> fluidTags = ((EntityDuck)pony.asEntity()).getSubmergedFluidTags();
            if (on) {
                originalTags = new HashSet<>(fluidTags);
                if (fluidTags.contains(FluidTags.WATER)) {
                    fluidTags.clear();
                } else {
                    fluidTags.add(FluidTags.WATER);
                }
            } else if (originalTags != null) {
                fluidTags.clear();
                fluidTags.addAll(originalTags);
                originalTags = null;
            }
        }
    }
}
