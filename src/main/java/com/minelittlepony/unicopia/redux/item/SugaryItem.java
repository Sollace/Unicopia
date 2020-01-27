package com.minelittlepony.unicopia.redux.item;

import com.minelittlepony.unicopia.core.SpeciesList;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SugaryItem extends Item {

    private final int sugarAmount;

    public SugaryItem(Settings settings, int sugar) {
        super(settings);
        sugarAmount = sugar;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (sugarAmount != 0 && entity instanceof PlayerEntity) {
            SpeciesList.instance().getPlayer((PlayerEntity)entity).addEnergy(sugarAmount);
        }

        return super.finishUsing(stack, world, entity);
    }
}
