package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.forgebullshit.IMultiItem;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.tossable.ITossableItem;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTomato extends ItemFood implements ITossableItem, IMultiItem {

    private final String name;

    public ItemTomato(String domain, String name, int heal, int sat) {
        super(heal, sat, false);

        this.name = name;

        setTranslationKey(name);
        setRegistryName(domain, name);

        setDispenseable();
        setHasSubtypes(true);
    }

    @Override
    public String[] getVariants() {
        return new String[] {name, "rotten_" + name};
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + (canBeThrown(stack) ? ".rotten" : "");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (canBeThrown(itemstack) && !player.canEat(false)) {
            toss(world, itemstack, player);

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    public boolean canBeThrown(ItemStack stack) {
        return stack.getMetadata() > 0;
    }

    protected boolean isSickening(ItemStack stack, EntityPlayer player) {
        return canBeThrown(stack)
                && !PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies().canUseEarth();
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {

        PotionEffect effect = player.getActivePotionEffect(MobEffects.NAUSEA);

        if (isSickening(stack, player)) {
            int duration = 7000;

            if (effect != null) {
                duration += Math.max(0, effect.getDuration());
            }

            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, duration, 4));
        } else if (effect != null) {
            player.removePotionEffect(MobEffects.NAUSEA);
        }

        super.onFoodEaten(stack, worldIn, player);
    }

    @Override
    public void onImpact(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote && state.getMaterial() == Material.GLASS) {
            world.destroyBlock(pos, true);
        }
    }
}
