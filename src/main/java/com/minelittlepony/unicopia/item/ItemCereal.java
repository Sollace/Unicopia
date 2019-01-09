package com.minelittlepony.unicopia.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCereal extends ItemFood {

    public ItemCereal(String domain, String name, int amount, float saturation) {
        super(amount, saturation, false);

        setTranslationKey(name);
        setRegistryName(domain, name);
        setMaxStackSize(1);
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        super.onItemUseFinish(stack, worldIn, entityLiving);

        this.setAlwaysEdible();

        return new ItemStack(Items.BOWL);
    }
}
