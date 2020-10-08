package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

@Mixin(BlockItem.class)
abstract class MixinBlockItem extends Item implements ToxicHolder {
    public MixinBlockItem() {super(null); }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (getToxic().isPresent()) {
            return getToxic().get().getUseAction(stack);
        }
        return super.getUseAction(stack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (getToxic().isPresent()) {
            return getToxic().get().use(world, player, hand, () -> super.use(world, player, hand));
        }
        return super.use(world, player, hand);
    }
}
