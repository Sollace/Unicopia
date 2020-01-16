package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntitySpear;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.projectile.IAdvancedProjectile;
import com.minelittlepony.unicopia.projectile.ITossableItem;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class ItemSpear extends Item implements ITossableItem {

    public ItemSpear(String domain, String name) {
        setFull3D();
        setTranslationKey(name);
        setRegistryName(domain, name);
        setMaxStackSize(1);
        setMaxDamage(500);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 440;
    }

    @Override
    public TypedActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, EnumHand hand) {
        if (!world.isClient) {
            ItemStack itemstack = player.getStackInHand(hand);

            if (canBeThrown(itemstack)) {
                player.setActiveHand(hand);

                return new TypedActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
            }

        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {

            int i = getMaxItemUseDuration(itemstack) - timeLeft;

            if (i > 10) {
                if (canBeThrown(itemstack)) {
                    itemstack.damageItem(1, entity);
                    toss(world, itemstack, (PlayerEntity)entity);
                }
            }
        }
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
    public IAdvancedProjectile createProjectile(World world, PlayerEntity player) {
        return new EntitySpear(world, player);
    }

    @Nullable
    @Override
    public IAdvancedProjectile createProjectile(World world, Position pos) {
        return null;
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, BlockState state) {

    }
}
