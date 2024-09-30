package com.minelittlepony.unicopia.item.enchantment;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public interface EnchantmentUtil {
    String HEART_BOUND_CONSUMED_FLAG = "unicopia:heart_bound_consumed";

    static boolean consumeEnchantment(RegistryEntry<Enchantment> enchantment, int levels, ItemStack stack) {
        return consumeEnchantment(enchantment, levels, stack, null, 0);
    }

    static boolean consumeEnchantment(RegistryEntry<Enchantment> enchantment, int levels, ItemStack stack, @Nullable Random random, int chance) {
        ItemEnchantmentsComponent.Builder enchantments = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(stack));
        int level = enchantments.getLevel(enchantment);
        if (level <= 0) {
            return false;
        }

        if (random == null || chance <= 1 || random.nextInt(chance) == 0) {
            enchantments.set(enchantment, Math.max(0, level - levels));
            EnchantmentHelper.set(stack, enchantments.build());
        }
        return true;
    }

    static int getLuck(int baseline, LivingEntity entity) {
        boolean naturallyLucky = Living.getOrEmpty(entity).filter(c -> c.getCompositeRace().canUseEarth()).isPresent();
        if (naturallyLucky) {
            baseline += 15;
        }
        float luckAmplifier = getEffectAmplifier(entity, StatusEffects.LUCK) * (naturallyLucky ? 2 : 1);
        float dolphinsGraceAmplifier = getEffectAmplifier(entity, StatusEffects.DOLPHINS_GRACE) * 0.5F;
        float unluckAmplifier = getEffectAmplifier(entity, StatusEffects.UNLUCK) * (naturallyLucky ? 0.5F : 1);
        float badOmenAmplifier = getEffectAmplifier(entity, StatusEffects.BAD_OMEN) * (naturallyLucky ? 1 : 2);
        return (int)MathHelper.clamp(baseline + luckAmplifier + dolphinsGraceAmplifier - unluckAmplifier - badOmenAmplifier, -10, 10);
    }

    static int getLevel(RegistryKey<Enchantment> enchantment, ItemStack stack) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        return enchantments.getEnchantments().stream()
                .filter(entry -> entry.matchesKey(enchantment)).map(enchantments::getLevel).findFirst().orElse(0);
    }

    @Deprecated
    static int getLevel(World world, RegistryKey<Enchantment> enchantment, ItemStack stack) {
        return world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LOOTING).map(entry -> {
            return EnchantmentHelper.getLevel(entry, stack);
        }).orElse(0);
    }

    @Deprecated
    static int getLevel(RegistryKey<Enchantment> enchantment, LivingEntity entity) {
        return entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LOOTING).map(entry -> {
            return EnchantmentHelper.getEquipmentLevel(entry, entity);
        }).orElse(0);
    }

    static int getEffectAmplifier(LivingEntity entity, RegistryEntry<StatusEffect> effect) {
        return entity.hasStatusEffect(effect) ? entity.getStatusEffect(effect).getAmplifier() : 0;
    }
}
