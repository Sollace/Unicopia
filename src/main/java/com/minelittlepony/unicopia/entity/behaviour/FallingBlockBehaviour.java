package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;
import com.minelittlepony.unicopia.mixin.MixinFallingBlock;
import com.minelittlepony.unicopia.mixin.MixinFallingBlockEntity;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class FallingBlockBehaviour extends EntityBehaviour<FallingBlockEntity> {
    private static final Optional<EntityDimensions> FULL_BLOCK = Optional.of(EntityDimensions.changing(0.6F, 0.9F));

    @Override
    public Optional<EntityDimensions> getDimensions(FallingBlockEntity entity, Optional<EntityDimensions> current) {
        return FULL_BLOCK;
    }

    @Override
    public void onImpact(Caster<?> source, FallingBlockEntity entity, float distance, float damageMultiplier, DamageSource cause) {
        if (source.asEntity().fallDistance > 3) {
            entity.fallDistance = source.asEntity().fallDistance;
            entity.handleFallDamage(distance, damageMultiplier, cause);

            BlockState state = entity.getBlockState();
            if (state.getBlock() instanceof FallingBlock fb) {
                fb.onLanding(entity.getWorld(), entity.getBlockPos(), state, state, entity);
            }
        }
    }

    @Override
    public boolean isEqual(FallingBlockEntity a, Entity b) {
        return b instanceof FallingBlockEntity f && f.getBlockState() == a.getBlockState();
    }

    private FallingBlockEntity configure(FallingBlockEntity entity, Block block) {
        if (block instanceof MixinFallingBlock) {
            ((MixinFallingBlock)block).invokeConfigureFallingBlockEntity(entity);
        }
        entity.dropItem = false;

        return entity;
    }

    @Override
    public FallingBlockEntity onCreate(FallingBlockEntity entity, EntityAppearance context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);

        BlockState state = entity.getBlockState()
                .withIfExists(Properties.CHEST_TYPE, ChestType.SINGLE)
                .withIfExists(Properties.BED_PART, BedPart.HEAD)
                .withIfExists(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        Block block = state.getBlock();
        context.setBlockEntity(block instanceof BlockEntityProvider bep ? bep.createBlockEntity(entity.getBlockPos(), state) : null);

        if (state.contains(Properties.BED_PART)) {
            Vec3i offset = BedBlock.getOppositePartDirection(state).getVector();
            BlockState foot = state.with(Properties.BED_PART, BedPart.FOOT);
            context.attachExtraEntity(Vec3d.of(offset), configure(MixinFallingBlockEntity.createInstance(entity.getWorld(), entity.getX() + offset.getX(), entity.getY() + offset.getY(), entity.getZ() + offset.getZ(), foot), block));
        }

        if (state.contains(Properties.DOUBLE_BLOCK_HALF)) {
            BlockState upperState = state.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            context.attachExtraEntity(new Vec3d(0, 1, 0), configure(MixinFallingBlockEntity.createInstance(entity.getWorld(), entity.getX(), entity.getY() + 1, entity.getZ(), upperState), block));
        }

        if (state != entity.getBlockState()) {
            entity = MixinFallingBlockEntity.createInstance(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), state);
        }
        return configure(entity, block);
    }

    @Override
    public void update(Living<?> source, FallingBlockEntity entity, Disguise spell) {

        BlockState state = entity.getBlockState();
        if (state.contains(Properties.WATERLOGGED)) {
            boolean logged = entity.getWorld().isWater(entity.getBlockPos());

            if (state.get(Properties.WATERLOGGED) != logged) {
                entity = MixinFallingBlockEntity.createInstance(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), state.with(Properties.WATERLOGGED, logged));
                spell.getDisguise().setAppearance(entity);
            }
        }

        EntityAppearance disguise = spell.getDisguise();

        BlockEntity be = disguise.getBlockEntity();

        if (source instanceof Pony player && be instanceof Tickable ceb && (be instanceof ChestBlockEntity || be instanceof EnderChestBlockEntity)) {
            if (player.sneakingChanged()) {
                be.onSyncedBlockEvent(1, isSneakingOnGround(source) ? 1 : 0);
            }

            be.setWorld(entity.getWorld());
            ((MixinBlockEntity)be).setPos(entity.getBlockPos());
            ceb.tick();
            be.setWorld(null);
        }

        for (var attachment : disguise.getAttachments()) {
            copyBaseAttributes(source.asEntity(), attachment.entity(), attachment.offset());
        }
    }
}
