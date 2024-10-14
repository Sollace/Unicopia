package com.minelittlepony.unicopia.item.enchantment;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

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


    static boolean prefersEquipment(ItemStack newStack, ItemStack oldStack) {
        int newLevel = getWantItNeedItLevel(newStack);
        int oldLevel = getWantItNeedItLevel(oldStack);
        return newLevel > oldLevel;
    }

    static int getWantItNeedItLevel(Entity entity) {
        return entity instanceof LivingEntity l ? getWantItNeedItLevel(l)
             : entity instanceof ItemEntity i ? getWantItNeedItLevel(i)
             : entity instanceof FlyingItemEntity p ? getWantItNeedItLevel(p.getStack())
             : 0;
    }

    static int getWantItNeedItLevel(ItemEntity entity) {
        return getLevel(UEnchantments.WANT_IT_NEED_IT, entity.getStack());
    }

    static int getWantItNeedItLevel(LivingEntity entity) {
        return getLevel(UEnchantments.WANT_IT_NEED_IT, entity);
    }

    static int getWantItNeedItLevel(ItemStack stack) {
        return getLevel(UEnchantments.WANT_IT_NEED_IT, stack);
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

    private static boolean forEachEnchantment(ItemStack stack, EquipmentSlot slot, LivingEntity entity, Predicate<RegistryEntry<Enchantment>> consumer) {
        if (!stack.isEmpty()) {
            ItemEnchantmentsComponent component = stack.get(DataComponentTypes.ENCHANTMENTS);
            if (component != null && !component.isEmpty()) {
                for (var entry : component.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> enchantment = entry.getKey();
                    if (enchantment.value().slotMatches(slot)) {
                        if (consumer.test(enchantment)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean forEachEnchantment(LivingEntity entity, Predicate<RegistryEntry<Enchantment>> predicate) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (forEachEnchantment(entity.getEquippedStack(slot), slot, entity, predicate)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasAnyEnchantmentsIn(LivingEntity user, TagKey<Enchantment> tag) {
        return forEachEnchantment(user, enchantment -> enchantment.isIn(tag));
    }

    static boolean hasAnyEnchantmentsWith(LivingEntity user, ComponentType<?> componentType) {
        return forEachEnchantment(user, enchantment -> enchantment.value().effects().contains(componentType));
    }

    static int getLevel(LivingEntity user, TagKey<Enchantment> tag) {
        return RegistryUtils.entriesForTag(user.getWorld(), tag)
                .stream()
                .mapToInt(entry -> EnchantmentHelper.getEquipmentLevel(entry, user)).sum();
    }

    @Deprecated
    static int getLevel(RegistryKey<Enchantment> enchantment, LivingEntity entity) {
        return entity.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(enchantment.getValue())
                .map(entry -> EnchantmentHelper.getEquipmentLevel(entry, entity))
                .orElse(0);
    }

    private static int getTotalLevel(RegistryKey<Enchantment> enchantment, LivingEntity entity) {
        return entity.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(enchantment.getValue())
                .map(entry -> getTotalEquipmentLevel(entry, entity))
                .orElse(0);
    }

    static int getTotalEquipmentLevel(RegistryEntry<Enchantment> enchantment, LivingEntity entity) {
        int level = 0;
        for (ItemStack stack : enchantment.value().getEquipment(entity).values()) {
            level += EnchantmentHelper.getLevel(enchantment, stack);
        }
        return level;
    }

    static int getEffectAmplifier(LivingEntity entity, RegistryEntry<StatusEffect> effect) {
        return entity.hasStatusEffect(effect) ? entity.getStatusEffect(effect).getAmplifier() : 0;
    }

    static float getWeight(LivingEntity entity) {
        return 1 + getTotalLevel(UEnchantments.HEAVY, entity);
    }

    static float getWindBuffetResistance(LivingEntity entity) {
        return 1 + (getTotalLevel(UEnchantments.HEAVY, entity) * 0.8F) + (EquinePredicates.PLAYER_EARTH.test(entity) ? 1 : 0);
    }

    static float getImpactReduction(LivingEntity entity) {
        return 1 + (getTotalLevel(UEnchantments.PADDED, entity) / 6F);
    }

    static float getBouncyness(LivingEntity entity) {
        return getTotalLevel(UEnchantments.PADDED, entity) * 6;
    }

    static float getAirResistance(LivingEntity entity) {
        return 1 + getTotalLevel(UEnchantments.HEAVY, entity) * 0.009F;
    }

    @FunctionalInterface
    interface ContextAwareConsumer {
        boolean accept(RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context);
    }
}
