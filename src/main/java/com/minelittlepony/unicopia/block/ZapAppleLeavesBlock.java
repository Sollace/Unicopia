package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.block.data.ZapAppleStageStore;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ZapAppleLeavesBlock extends LeavesBlock {
    public static final EnumProperty<ZapAppleStageStore.Stage> STAGE = EnumProperty.of("stage", ZapAppleStageStore.Stage.class);

    ZapAppleLeavesBlock() {
        super(Settings.of(Material.LEAVES)
                .strength(500, 1200)
                .ticksRandomly()
                .sounds(BlockSoundGroup.AZALEA_LEAVES)
                .nonOpaque()
                .allowsSpawning(UBlocks::canSpawnOnLeaves)
                .suffocates(UBlocks::never)
                .blockVision(UBlocks::never)
        );
        setDefaultState(getDefaultState().with(STAGE, ZapAppleStageStore.Stage.HIBERNATING));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STAGE);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        ZapAppleStageStore store = ZapAppleStageStore.get(world);
        ZapAppleStageStore.Stage newStage = store.getStage();
        if (!world.isDay() && state.get(STAGE).mustChangeInto(newStage)) {
            world.setBlockState(pos, state.with(STAGE, newStage));
            onStageChanged(store, newStage, world, state, pos, random);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);

        ZapAppleStageStore store = ZapAppleStageStore.get(world);
        ZapAppleStageStore.Stage newStage = store.getStage();
        if (!world.isDay() && state.get(STAGE).mustChangeIntoInstantly(newStage)) {
            world.setBlockState(pos, state.with(STAGE, newStage));
            onStageChanged(store, newStage, world, state, pos, random);
        }

        world.createAndScheduleBlockTick(pos, this, 1);
    }

    @Override
    protected boolean shouldDecay(BlockState state) {
        return false;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (!ctx.getWorld().isClient) {
            ctx.getWorld().createAndScheduleBlockTick(ctx.getBlockPos(), this, 1);
            return super.getPlacementState(ctx).with(STAGE, ZapAppleStageStore.get(ctx.getWorld()).getStage());
        }
        return super.getPlacementState(ctx);
    }

    private void onStageChanged(ZapAppleStageStore store, ZapAppleStageStore.Stage stage, ServerWorld world, BlockState state, BlockPos pos, Random random) {
        boolean mustFruit = Random.create(state.getRenderingSeed(pos)).nextInt(5) < 2;
        BlockState below = world.getBlockState(pos.down());

        if (world.isAir(pos.down())) {
            if (stage == ZapAppleStageStore.Stage.FRUITING && mustFruit) {
                world.setBlockState(pos.down(), UBlocks.ZAP_BULB.getDefaultState(), Block.NOTIFY_ALL);
                store.triggerLightningStrike(pos);
            }
        }

        if (stage != ZapAppleStageStore.Stage.HIBERNATING && world.getRandom().nextInt(10) == 0) {
            store.triggerLightningStrike(pos);
        }

        if (stage == ZapAppleStageStore.Stage.RIPE) {
            store.playMoonEffect(pos);

            if (below.isOf(UBlocks.ZAP_BULB)) {
                world.setBlockState(pos.down(), UBlocks.ZAP_APPLE.getDefaultState(), Block.NOTIFY_ALL);
                store.triggerLightningStrike(pos);
            }
        }

        if (mustFruit && stage == ZapAppleStageStore.Stage.HIBERNATING) {
            if (below.isOf(UBlocks.ZAP_APPLE) || below.isOf(UBlocks.ZAP_BULB)) {
                world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
            }
        }
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        triggerLightning(state, world, pos, player);
    }

    @Deprecated
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return isAir(state) ? BlockRenderType.INVISIBLE : super.getRenderType(state);
    }

    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return isAir(state) ? VoxelShapes.empty() : super.getOutlineShape(state, world, pos, context);
    }

    @Deprecated
    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return isAir(state) || super.canReplace(state, context);
    }

    @Deprecated
    @Override
    public boolean canBucketPlace(BlockState state, Fluid fluid) {
        return isAir(state) || super.canBucketPlace(state, fluid);
    }

    protected boolean isAir(BlockState state) {
        return state.get(STAGE) == ZapAppleStageStore.Stage.HIBERNATING;
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float delta = super.calcBlockBreakingDelta(state, player, world, pos);

        if (Pony.of(player).getSpecies().canUseEarth()) {
            delta *= 50;
        }

        if (state.get(STAGE) == ZapAppleStageStore.Stage.RIPE) {
            delta *= 5;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }

    public static void triggerLightning(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Vec3d center = Vec3d.ofCenter(pos);
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            world.getOtherEntities(null, Box.from(center).expand(7)).forEach(other -> {
                float dist = (float)other.getPos().distanceTo(center);
                if (dist < 4) {
                    other.onStruckByLightning(serverWorld, lightning);
                } else {
                    float damage = 3 / dist;
                    if (damage > 1) {
                        other.damage(DamageSource.LIGHTNING_BOLT, damage);
                    }
                }
            });
        }
        world.emitGameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Emitter.of(state));
        ParticleUtils.spawnParticle(world, UParticles.LIGHTNING_BOLT, Vec3d.ofCenter(pos), Vec3d.ZERO);
    }

}
