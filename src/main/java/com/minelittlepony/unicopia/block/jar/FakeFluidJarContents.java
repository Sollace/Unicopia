package com.minelittlepony.unicopia.block.jar;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;
import com.minelittlepony.unicopia.util.FluidHelper;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public record FakeFluidJarContents (
        TileData tile,
        String fluid,
        int color,
        Item empty,
        Item filled
) implements JarContents {
    public FakeFluidJarContents(TileData tile, NbtCompound compound) {
        this(tile, compound.getString("fluid"), compound.getInt("color"),
                Registries.ITEM.get(Identifier.of(compound.getString("empty"))),
                Registries.ITEM.get(Identifier.of(compound.getString("filled"))));
    }

    @Override
    public TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        tile.markDirty();
        return getRealFluid().map(FluidVariant::of).<TypedActionResult<JarContents>>map(fluid -> {
            long remainder = FluidHelper.deposit(stack, player, hand, fluid, FluidConstants.BUCKET);
            fluid.getFluid().getBucketFillSound().ifPresent(sound -> player.playSound(sound, 1, 1));
            if (remainder > 0) {
                return TypedActionResult.success(new FluidOnlyJarContents(tile, remainder, fluid));
            }
            return TypedActionResult.success(new ItemsJarContents(tile));
        }).orElseGet(() -> {
            if (!stack.isOf(empty)) {
                return TypedActionResult.pass(this);
            }
            consumeAndSwap(player, hand, filled.getDefaultStack());
            player.playSound("powder_snow".equalsIgnoreCase(fluid) ? USounds.Vanilla.ITEM_BUCKET_FILL_POWDER_SNOW : USounds.Vanilla.ITEM_BUCKET_FILL, 1, 1);
            return TypedActionResult.success(new ItemsJarContents(tile));
        });
    }

    @Override
    public void onDestroyed() {
        getRealFluid().ifPresent(fluid -> {
            tile.getWorld().setBlockState(tile.getPos(), FluidHelper.getFullFluidState(FluidVariant.of(fluid)).getBlockState());
        });
    }

    @Override
    public NbtCompound toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.putString("fluid", fluid);
        compound.putInt("color", color);
        compound.putString("empty", Registries.ITEM.getId(empty).toString());
        compound.putString("filled", Registries.ITEM.getId(filled).toString());
        return compound;
    }

    private Optional<Fluid> getRealFluid() {
        return Registries.FLUID.getIds().stream()
                .filter(id -> id.getPath().equalsIgnoreCase(fluid))
                .findFirst()
                .map(Registries.FLUID::get);
    }
}