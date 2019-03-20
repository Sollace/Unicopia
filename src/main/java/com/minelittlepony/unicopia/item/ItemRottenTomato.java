package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.tossable.ITossableItem;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRottenTomato extends ItemTomato implements ITossableItem {

    public ItemRottenTomato(String domain, String name, int heal, int sat) {
        super(domain, name, heal, sat);

        setDispenseable();
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

    protected boolean isSickening(ItemStack stack, EntityPlayer player) {
        return canBeThrown(stack)
                && !PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies().canUseEarth();
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (isSickening(stack, player)) {
            int duration = 7000;

            PotionEffect effect = player.getActivePotionEffect(MobEffects.NAUSEA);

            if (effect != null) {
                duration += Math.max(0, effect.getDuration());
            }

            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, duration, 4));
        }
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, IBlockState state) {
        if (caster.isLocal() && state.getMaterial() == Material.GLASS) {
            caster.getWorld().destroyBlock(pos, true);
        }
    }
}
