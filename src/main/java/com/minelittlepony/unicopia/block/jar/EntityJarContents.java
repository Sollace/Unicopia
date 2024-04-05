package com.minelittlepony.unicopia.block.jar;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.block.ItemJarBlock.FluidJarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public record EntityJarContents (
        TileData tile,
        @Nullable EntityType<?> entityType,
        Supplier<@Nullable Entity> entity
) implements FluidJarContents {
    public EntityJarContents(TileData tile, NbtCompound compound) {
        this(tile, Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(compound.getString("entity"))).orElse(null));
    }

    public EntityJarContents(TileData tile) {
        this(tile, (EntityType<?>)null);
    }

    public EntityJarContents(TileData tile, EntityType<?> entityType) {
        this(tile, entityType, Suppliers.memoize(() -> {
            return entityType == null ? null : entityType.create(tile.getWorld());
        }));
    }

    @Override
    public TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(Items.BUCKET)) {
            if (entity().get() instanceof Bucketable bucketable) {
                consumeAndSwap(player, hand, bucketable.getBucketItem());
                player.playSound(bucketable.getBucketFillSound(), 1, 1);
            }
            tile.markDirty();
            return TypedActionResult.success(new ItemsJarContents(tile));
        }
        return TypedActionResult.pass(this);
    }

    @Override
    public void onDestroyed() {
        tile.getWorld().setBlockState(tile.getPos(), Blocks.WATER.getDefaultState());
        Entity entity = entity().get();
        if (entity != null) {
            entity.refreshPositionAfterTeleport(tile.getPos().toCenterPos());
            tile.getWorld().spawnEntity(entity);
        }
    }

    @Override
    public NbtCompound toNBT(NbtCompound compound) {
        compound.putString("entity", EntityType.getId(entityType).toString());
        return compound;
    }

    @Override
    public FluidVariant fluid() {
        return FluidVariant.of(Fluids.WATER);
    }
}