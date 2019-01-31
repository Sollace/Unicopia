package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCereal extends ItemFood {

    private int sugarAmount;

    public ItemCereal(String domain, String name, int amount, float saturation) {
        super(amount, saturation, false);

        setTranslationKey(name);
        setRegistryName(domain, name);
        setMaxStackSize(1);
        setContainerItem(Items.BOWL);
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        super.onItemUseFinish(stack, worldIn, entityLiving);

        return getContainerItem(stack);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);

        if (sugarAmount != 0) {
            PlayerSpeciesList.instance().getPlayer(player).addEnergy(sugarAmount);
        }
    }

    public ItemCereal setSugarAmount(int sugar) {
        sugarAmount = sugar;
        return this;
    }
}
