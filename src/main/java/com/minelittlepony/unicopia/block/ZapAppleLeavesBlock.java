package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.data.ZapAppleStageStore;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;

public class ZapAppleLeavesBlock extends LeavesBlock implements TintedBlock {
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
    public boolean hasRandomTicks(BlockState state) {
        return true;
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

        world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    protected boolean shouldDecay(BlockState state) {
        return false;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (!ctx.getWorld().isClient) {
            ctx.getWorld().scheduleBlockTick(ctx.getBlockPos(), this, 1);
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
            if (below.isOf(UBlocks.ZAP_BULB)) {
                world.setBlockState(pos.down(), UBlocks.ZAP_APPLE.getDefaultState(), Block.NOTIFY_ALL);
                store.playMoonEffect(pos);
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
        ZapBlock.triggerLightning(state, world, pos, player);
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

    @Override
    public int getTint(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int foliageColor) {

        if (pos == null) {
            return 0x4C7EFA;
        }

        return TintedBlock.blend(TintedBlock.rotate(foliageColor, 2), 0x0000FF, 0.3F);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (state.get(PERSISTENT)) {
            return Blocks.OAK_LEAVES.calcBlockBreakingDelta(Blocks.OAK_LEAVES.getDefaultState(), player, world, pos);
        }

        float delta = super.calcBlockBreakingDelta(state, player, world, pos);

        if (Pony.of(player).getSpecies().canUseEarth()) {
            delta *= 50;
        }

        if (state.get(STAGE) == ZapAppleStageStore.Stage.RIPE) {
            delta *= 5;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }
}
