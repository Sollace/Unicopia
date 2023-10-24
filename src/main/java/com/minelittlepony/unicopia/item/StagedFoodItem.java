package com.minelittlepony.unicopia.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class StagedFoodItem extends Item {

    private final ItemConvertible eatingRemainder;

    public StagedFoodItem(Settings settings, ItemConvertible eatingRemainder) {
        super(settings);
        this.eatingRemainder = eatingRemainder;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (isFood()) {
            user.eatFood(world, stack.copy());
            if (user instanceof PlayerEntity player && player.getAbilities().creativeMode) {
                return stack;
            }

            if (stack.isDamageable() && stack.getDamage() < stack.getMaxDamage() - 1) {
                stack.damage(1, user, ply -> {
                    // noop
                });
                return stack;
            }
            return eatingRemainder.asItem().getDefaultStack();
        }
        return stack;
    }
}
