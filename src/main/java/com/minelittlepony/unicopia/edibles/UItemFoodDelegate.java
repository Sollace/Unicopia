package com.minelittlepony.unicopia.edibles;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
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
