package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public class FoodPoisoningStatusEffect extends StatusEffect {

    FoodPoisoningStatusEffect(int color) {
        super(StatusEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {

        boolean showParticles = entity.getStatusEffect(this).shouldShowParticles();

        if (!entity.hasStatusEffect(StatusEffects.NAUSEA) && entity.getRandom().nextInt(12) == 0) {

            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 1, true, showParticles, false));
        }

        if (entity instanceof PlayerEntity) {
            ((PlayerEntity)entity).getHungerManager().addExhaustion(0.5F);
        }

        if (EffectUtils.isPoisoned(entity) && entity.getRandom().nextInt(12) == 0 && !entity.hasStatusEffect(StatusEffects.POISON)) {
            StatusEffects.POISON.applyUpdateEffect(entity, 1);
        }
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        applyUpdateEffect(target, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i = 40 >> amplifier;
        return i <= 0 || duration % i == 0;
    }

    public static TypedActionResult<ItemStack> apply(ItemConvertible sender, PlayerEntity user, Hand hand) {
        @Nullable
        FoodComponent food = sender.asItem().getFoodComponent();

        if (food == null || !user.canConsume(food.isAlwaysEdible()) || !user.hasStatusEffect(UEffects.FOOD_POISONING)) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        user.getWorld().playSound(null, user.getX(), user.getY(), user.getZ(), USounds.Vanilla.ENTITY_PLAYER_BURP, SoundCategory.NEUTRAL,
                1,
                1 + (user.getWorld().random.nextFloat() - user.getWorld().random.nextFloat()) * 0.4f);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 1, true, false, false));
        return TypedActionResult.fail(user.getStackInHand(hand));
    }
}
