package com.minelittlepony.unicopia.item.consumables;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class UItemFoodDelegate extends ItemMultiTexture implements IEdible {

    @Nonnull
    private ItemFood foodItem = new ItemFood(0, 0, false);

    public UItemFoodDelegate(Block block, ItemMultiTexture.Mapper mapper) {
        super(block, block, mapper);
    }

    public UItemFoodDelegate setFoodDelegate(@Nonnull ItemFood foodItem) {
        this.foodItem = foodItem;
        return this;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        foodItem.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        return foodItem.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return foodItem.getMaxItemUseDuration(stack);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return foodItem.getItemUseAction(stack);
    }

    @Override
    public TypedActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, EnumHand handIn) {
        return foodItem.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        if (foodItem instanceof IEdible) {
            return ((IEdible)foodItem).getToxicityLevel(stack);
        }
        return null;
    }
}
