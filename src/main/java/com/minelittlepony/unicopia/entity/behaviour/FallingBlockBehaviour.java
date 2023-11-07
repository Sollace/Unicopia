package com.minelittlepony.unicopia.entity.behaviour;

import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinFallingBlock;
import com.minelittlepony.unicopia.mixin.MixinFallingBlockEntity;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.state.property.Properties;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FallingBlockBehaviour extends EntityBehaviour<FallingBlockEntity> {

    private static final Vec3d UP = Vec3d.of(Direction.UP.getVector());

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

        BlockState state = entity.getBlockState();
        Block block = state.getBlock();

        if (state.isIn(BlockTags.DOORS) && block instanceof DoorBlock) {
            BlockState lowerState = state.with(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            BlockState upperState = state.with(DoorBlock.HALF, DoubleBlockHalf.UPPER);

            context.attachExtraEntity(configure(MixinFallingBlockEntity.createInstance(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), upperState), block));

            return configure(MixinFallingBlockEntity.createInstance(entity.getWorld(), entity.getX(), entity.getY() + 1, entity.getZ(), lowerState), block);
        }

        if (block instanceof BlockEntityProvider bep) {
            context.addBlockEntity(bep.createBlockEntity(entity.getBlockPos(), state));
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
                return;
            }
        }

        EntityAppearance disguise = spell.getDisguise();
        List<Entity> attachments = disguise.getAttachments();
        if (attachments.size() > 0) {
            copyBaseAttributes(source.asEntity(), attachments.get(0), UP);
        }

        BlockEntity be = disguise.getBlockEntity();

        if (source instanceof Pony player && be instanceof Tickable ceb && (be instanceof ChestBlockEntity || be instanceof EnderChestBlockEntity)) {
            if (player.sneakingChanged()) {
                be.onSyncedBlockEvent(1, isSneakingOnGround(source) ? 1 : 0);
            }

            be.setWorld(entity.getWorld());
            ((Positioned)be).setPos(entity.getBlockPos());
            ceb.tick();
            be.setWorld(null);
        }
    }

    public interface Positioned {
        void setPos(BlockPos pos);
    }
}
