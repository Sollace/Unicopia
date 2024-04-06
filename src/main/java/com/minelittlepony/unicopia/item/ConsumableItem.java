package com.minelittlepony.unicopia.item;

import java.util.Optional;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ConsumableItem extends Item {

    private final UseAction action;

    public ConsumableItem(Item.Settings settings, UseAction action) {
        super(settings);
        this.action = action;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        super.finishUsing(stack, world, user);
        if (user instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)user;
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        if (stack.isEmpty()) {
            return stack.isEmpty() ? Optional.ofNullable(getRecipeRemainder()).map(Item::getDefaultStack).orElse(ItemStack.EMPTY) : stack;
        }

        if (user instanceof PlayerEntity player) {
            return Optional.ofNullable(getRecipeRemainder()).map(Item::getDefaultStack).map(remainder -> {
                return ItemUsage.exchangeStack(stack, player, remainder);
            }).orElse(stack);
        }
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return action;
    }
}
