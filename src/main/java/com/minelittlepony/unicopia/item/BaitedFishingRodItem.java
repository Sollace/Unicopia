package com.minelittlepony.unicopia.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
                int lure = (EnchantmentHelper.getLure(stack) + 1) * 2;
                int luck = (EnchantmentHelper.getLuckOfTheSea(stack) + 1) * 2;
                world.spawnEntity(new FishingBobberEntity(user, world, luck, lure));
            }

            if (result.getValue().isOf(this)) {
                ItemStack stack = Items.FISHING_ROD.getDefaultStack();
                stack.setDamage(result.getValue().getDamage());
                return TypedActionResult.success(stack, world.isClient());
            }
        }
        return result;
    }
}
