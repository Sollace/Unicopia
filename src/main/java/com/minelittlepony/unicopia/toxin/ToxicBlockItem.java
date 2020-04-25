package com.minelittlepony.unicopia.toxin;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ToxicBlockItem extends BlockItem {

    private final Toxic toxic;

    public ToxicBlockItem(Block block, Settings settings, UseAction action, Toxicity toxicity, Toxin toxin) {
        super(block, settings);
        toxic = new Toxic(this, action, toxin, stack -> toxicity);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return toxic.getUseAction(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(toxic.getTooltip(stack));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        super.finishUsing(stack, world, entity);
        return toxic.finishUsing(stack, world, entity);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return toxic.use(world, player, hand, () -> super.use(world, player, hand));
    }
}
