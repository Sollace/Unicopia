package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntitySpear;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.tossable.ITossableItem;
import com.minelittlepony.unicopia.tossable.ITossed;

import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSpear extends Item implements ITossableItem {

    public ItemSpear(String domain, String name) {
        setFull3D();
        setTranslationKey(name);
        setRegistryName(domain, name);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (canBeThrown(itemstack)) {
            toss(world, itemstack, player);

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
        switch (enchantment.type) {
            case WEAPON:
            case BOW:
                return true;
            default: return false;
        }
    }

    @Nullable
    @Override
    public ITossed createProjectile(World world, EntityPlayer player) {
        return new EntitySpear(world, player);
    }

    @Nullable
    @Override
    public ITossed createProjectile(World world, IPosition pos) {
        return null;
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, IBlockState state) {

    }
}
