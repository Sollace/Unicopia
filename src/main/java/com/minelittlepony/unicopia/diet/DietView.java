package com.minelittlepony.unicopia.diet;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public interface DietView {
    TypedActionResult<ItemStack> startUsing(ItemStack stack, World world, PlayerEntity user, Hand hand);

    void finishUsing(ItemStack stack, World world, LivingEntity entity);

    void appendTooltip(ItemStack stack, @Nullable PlayerEntity user, List<Text> tooltip, TooltipContext context);

    interface Holder {
        default DietView getDiets(ItemStack stack) {
            return PonyDiets.getInstance();
        }
    }
}
