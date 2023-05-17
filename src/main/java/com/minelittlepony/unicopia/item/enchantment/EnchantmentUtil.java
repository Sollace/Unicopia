package com.minelittlepony.unicopia.item.enchantment;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public interface EnchantmentUtil {

    static boolean consumeEnchantment(Enchantment enchantment, int levels, ItemStack stack) {
        return consumeEnchantment(enchantment, levels, stack, null, 0);
    }

    static boolean consumeEnchantment(Enchantment enchantment, int levels, ItemStack stack, @Nullable Random random, int chance) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        int level = enchantments.getOrDefault(enchantment, 0);
        if (level <= 0) {
            return false;
        }

        if (random == null || chance <= 1 || random.nextInt(chance) == 0) {
            level = Math.max(0, level - levels);
            if (level == 0) {
                enchantments.remove(enchantment);
            } else {
                enchantments.put(enchantment, level - 1);
            }
            EnchantmentHelper.set(enchantments, stack);
        }
        return true;
    }

    static int getLuck(int baseline, LivingEntity entity) {
        boolean naturallyLucky = Living.getOrEmpty(entity).filter(c -> c.getSpecies().canUseEarth()).isPresent();
        if (naturallyLucky) {
            baseline += 15;
        }
        float luckAmplifier = getEffectAmplifier(entity, StatusEffects.LUCK) * (naturallyLucky ? 2 : 1);
        float dolphinsGraceAmplifier = getEffectAmplifier(entity, StatusEffects.DOLPHINS_GRACE) * 0.5F;
        float unluckAmplifier = getEffectAmplifier(entity, StatusEffects.UNLUCK) * (naturallyLucky ? 0.5F : 1);
        float badOmenAmplifier = getEffectAmplifier(entity, StatusEffects.BAD_OMEN) * (naturallyLucky ? 1 : 2);
        return (int)MathHelper.clamp(baseline + luckAmplifier + dolphinsGraceAmplifier - unluckAmplifier - badOmenAmplifier, -10, 10);
    }

    static int getEffectAmplifier(LivingEntity entity, StatusEffect effect) {
        if (!entity.hasStatusEffect(effect)) {
            return 0;
        }
        return entity.getStatusEffect(effect).getAmplifier();
    }
}
