package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
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
        @Nullable
        FishingBobberEntity oldFishHook = user.fishHook;
        TypedActionResult<ItemStack> result = super.use(world, user, hand);
        if (!world.isClient) {
            if (user.fishHook != null) {
                user.fishHook.discard();
                ItemStack stack = user.getStackInHand(hand);
                int lure = (int)((EnchantmentHelper.getFishingTimeReduction((ServerWorld)world, stack, user) + 1) * 20F);
                int luck = (EnchantmentHelper.getFishingLuckBonus((ServerWorld)world, stack, user) + 1) * 2;
                FishingBobberEntity bobber = new FishingBobberEntity(user, world, luck, lure);
                ((BaitedFishingBobber)bobber).setRodType(this);
                world.spawnEntity(bobber);
            }

            if (!user.isCreative()) {
                if (oldFishHook != null
                        && oldFishHook.getHookedEntity() == null
                        && oldFishHook.getDataTracker().get(FishingBobberEntity.CAUGHT_FISH)
                        && result.getValue().isOf(this)) {
                    ItemStack stack = result.getValue().withItem(Items.FISHING_ROD);
                    return TypedActionResult.success(stack, world.isClient());
                }
            }
        }
        return result;
    }

    public interface BaitedFishingBobber {
        Item getRodType();

        void setRodType(Item rodType);
    }
}
