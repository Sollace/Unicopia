package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class ButterfingersStatusEffect extends StatusEffect {

    ButterfingersStatusEffect(int color) {
        super(StatusEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        amplifier = MathHelper.clamp(amplifier, 0, 5);
        final int scale = 500 + (int)(((5 - amplifier) / 5F) * 900);

        if (entity.getWorld().random.nextInt(scale / 4) == 0) {
            applyInstantEffect(null, null, entity, amplifier, entity.getWorld().random.nextInt(scale));
        }
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {

        if (target.getWorld().isClient) {
            return;
        }

        if (target instanceof ServerPlayerEntity player) {
            if (player.dropSelectedItem(proximity < 1)) {
                player.getWorld().playSound(null, player.getBlockPos(), USounds.ENTITY_GENERIC_BUTTER_FINGERS, player.getSoundCategory());
                PlayerInventory inventory = player.getInventory();
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, inventory.selectedSlot, inventory.getStack(inventory.selectedSlot)));
            }
        } else {
            ItemStack stack = target.getMainHandStack();
            if (!stack.isEmpty()) {
                target.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                target.dropStack(stack);
                target.getWorld().playSound(null, target.getBlockPos(), USounds.ENTITY_GENERIC_BUTTER_FINGERS, target.getSoundCategory());
            }
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration > 0;
    }
}
