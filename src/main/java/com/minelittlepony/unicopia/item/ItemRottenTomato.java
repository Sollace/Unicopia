package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.projectile.ITossableItem;
import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
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
    public TypedActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, EnumHand hand) {
        ItemStack itemstack = player.getStackInHand(hand);

        if (canBeThrown(itemstack) && !player.canEat(false)) {
            toss(world, itemstack, player);

            return new TypedActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    protected boolean isSickening(ItemStack stack, PlayerEntity player) {
        return canBeThrown(stack)
                && !SpeciesList.instance().getPlayer(player).getSpecies().canUseEarth();
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, PlayerEntity player) {
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
    public void onImpact(ICaster<?> caster, BlockPos pos, BlockState state) {
        if (caster.isLocal() && state.getMaterial() == Material.GLASS) {
            caster.getWorld().destroyBlock(pos, true);
        }
    }
}
