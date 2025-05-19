package com.minelittlepony.unicopia.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class StagedFoodItem extends Item {

    private final ItemConvertible eatingRemainder;

    public StagedFoodItem(Settings settings, ItemConvertible eatingRemainder) {
        super(settings);
        this.eatingRemainder = eatingRemainder;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        FoodComponent food = stack.get(DataComponentTypes.FOOD);
        if (food != null) {
            user.eatFood(world, stack.copy(), food);
            if (user instanceof PlayerEntity player && player.getAbilities().creativeMode) {
                return stack;
            }

            if (stack.isDamageable() && stack.getDamage() < stack.getMaxDamage() - 1) {
                stack.damage(1, user, stack == user.getStackInHand(Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return stack;
            }
            return eatingRemainder.asItem().getDefaultStack();
        }
        return stack;
    }
}
