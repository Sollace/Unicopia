package com.minelittlepony.unicopia.toxin;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ToxicItem extends Item {

    private final Toxic toxic;

    public ToxicItem(Item.Settings settings, UseAction action, Toxicity toxicity, Toxin toxin) {
        this(settings, action, stack -> toxicity, toxin);
    }

    public ToxicItem(Item.Settings settings, UseAction action, Function<ItemStack, Toxicity> toxicity, Toxin toxin) {
        super(settings.group(ItemGroup.FOOD));
        this.toxic = new Toxic(this, action, toxin, toxicity);
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
