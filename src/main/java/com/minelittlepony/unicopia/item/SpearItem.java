package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.SpearEntity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.util.projectile.IAdvancedProjectile;
import com.minelittlepony.unicopia.util.projectile.ITossableItem;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class SpearItem extends Item implements ITossableItem {

    public SpearItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 440;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            ItemStack itemstack = player.getStackInHand(hand);

            if (canBeThrown(itemstack)) {
                player.swingHand(hand);

                return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
            }

        }

        return super.use(world, player, hand);
    }

    @Override
    public void onStoppedUsing(ItemStack itemstack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {

            int i = getMaxUseTime(itemstack) - timeLeft;

            if (i > 10) {
                if (canBeThrown(itemstack)) {
                    itemstack.damage(1, entity, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
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
        return new SpearEntity(world, player);
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
