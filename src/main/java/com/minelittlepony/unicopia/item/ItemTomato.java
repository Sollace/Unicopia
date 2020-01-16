package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.UItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class ItemTomato extends ItemFood {

    public ItemTomato(String domain, String name, int heal, int sat) {
        super(heal, sat, false);

        setTranslationKey(name);
        setRegistryName(domain, name);
    }


    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this == UItems.tomato && isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            items.add(new ItemStack(UItems.rotten_tomato));
            items.add(new ItemStack(UItems.cloudsdale_tomato));
            items.add(new ItemStack(UItems.rotten_cloudsdale_tomato));
        }
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {

        PotionEffect effect = player.getActivePotionEffect(MobEffects.NAUSEA);

        if (effect != null) {
            player.removePotionEffect(MobEffects.NAUSEA);
        }
    }

}
