package com.minelittlepony.unicopia.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BaitedFishingRodItem extends FishingRodItem {

    public BaitedFishingRodItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        TypedActionResult<ItemStack> result = super.use(world, user, hand);
        if (!world.isClient) {
            if (user.fishHook != null) {
                user.fishHook.discard();
                ItemStack stack = user.getStackInHand(hand);
                int lure = (int)((EnchantmentHelper.getFishingTimeReduction((ServerWorld)world, stack, user) + 1) * 20F);
                int luck = (EnchantmentHelper.getFishingLuckBonus((ServerWorld)world, stack, user) + 1) * 2;
                world.spawnEntity(new FishingBobberEntity(user, world, luck, lure));
            }

            if (result.getValue().isOf(this)) {
                ItemStack stack = result.getValue().withItem(Items.FISHING_ROD);
                return TypedActionResult.success(stack, world.isClient());
            }
        }
        return result;
    }
}
