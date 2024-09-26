package com.minelittlepony.unicopia.block.jar;

import com.minelittlepony.unicopia.block.ItemJarBlock.FluidJarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;
import com.minelittlepony.unicopia.util.FluidHelper;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public record FluidOnlyJarContents (
        TileData tile,
        long amount,
        FluidVariant fluid
) implements FluidJarContents {

    public FluidOnlyJarContents(TileData tile, NbtCompound compound) {
        this(tile, compound.getLong("amount"), NbtSerialisable.decode(FluidVariant.CODEC, compound.getCompound("fluid")).orElse(FluidVariant.blank()));
    }

    @Override
    public TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(Items.BUCKET)) {
            long remainder = FluidHelper.deposit(stack, player, hand, fluid, amount);
            tile.markDirty();
            fluid.getFluid().getBucketFillSound().ifPresent(sound -> player.playSound(sound, 1, 1));
            if (remainder > 0) {
                return TypedActionResult.success(new FluidOnlyJarContents(tile, remainder, fluid));
            }
            return TypedActionResult.success(new ItemsJarContents(tile));
        }
        return TypedActionResult.pass(this);
    }

    @Override
    public void onDestroyed() {
        if (amount >= FluidConstants.BUCKET) {
            tile.getWorld().setBlockState(tile.getPos(), FluidHelper.getFullFluidState(fluid).getBlockState());
        }
    }

    @Override
    public NbtCompound toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("fluid", NbtSerialisable.encode(FluidVariant.CODEC, fluid));
        compound.putLong("amount", amount);
        return compound;
    }
}