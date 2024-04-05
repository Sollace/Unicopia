package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.CodecUtils;
import com.minelittlepony.unicopia.util.SoundEmitter;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;

public class PieBlock extends Block implements Waterloggable {
    public static final MapCodec<PieBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtils.ITEM.fieldOf("slice_item").forGetter(b -> b.sliceItem),
            CodecUtils.ITEM.fieldOf("normal_item").forGetter(b -> b.normalItem),
            CodecUtils.ITEM.fieldOf("stomped_item").forGetter(b -> b.stompedItem),
            BedBlock.createSettingsCodec()
    ).apply(instance, PieBlock::new));
    public static final int MAX_BITES = 3;
    public static final IntProperty BITES = IntProperty.of("bites", 0, MAX_BITES);
    public static final BooleanProperty STOMPED = BooleanProperty.of("stomped");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape[] SHAPES;
    static {
        final int PIE_HEIGHT = 4;
        final VoxelShape WEDGE = Block.createCuboidShape(2, 0, 2, 8, PIE_HEIGHT, 8);
        final float OFFSET_AMOUNT = 6F/16F;
        SHAPES = new VoxelShape[] {
                Block.createCuboidShape(2, 0, 2, 14, PIE_HEIGHT, 14),
                VoxelShapes.union(WEDGE, WEDGE.offset(OFFSET_AMOUNT, 0, 0), WEDGE.offset(OFFSET_AMOUNT, 0, OFFSET_AMOUNT)),
                VoxelShapes.union(WEDGE, WEDGE.offset(OFFSET_AMOUNT, 0, 0)),
                WEDGE
            };
    }

    private final ItemConvertible sliceItem;
    private final ItemConvertible normalItem;
    private final ItemConvertible stompedItem;

    public PieBlock(ItemConvertible sliceItem, ItemConvertible normalItem, ItemConvertible stompedItem, Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(STOMPED, false).with(WATERLOGGED, false));
        this.sliceItem = sliceItem;
        this.normalItem = normalItem;
        this.stompedItem = stompedItem;
    }

    @Override
    protected MapCodec<? extends PieBlock> getCodec() {
        return CODEC;
    }

    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[state.get(BITES)];
    }

    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (world.isClient) {

            if (itemStack.isIn(UTags.Items.CAN_CUT_PIE)) {
                return ActionResult.SUCCESS;
            }

            if (tryEat(world, pos, state, player).isAccepted()) {
                return ActionResult.SUCCESS;
            }

            if (itemStack.isEmpty()) {
                return ActionResult.CONSUME;
            }
        }

        if (itemStack.isIn(UTags.Items.CAN_CUT_PIE)) {
            SoundEmitter.playSoundAt(player, USounds.BLOCK_PIE_SLICE, SoundCategory.NEUTRAL, 1, 1);
            removeSlice(world, pos, state, player);
            itemStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            SoundEmitter.playSoundAt(player, USounds.BLOCK_PIE_SLICE_POP, SoundCategory.NEUTRAL, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
            Block.dropStack(world, pos, sliceItem.asItem().getDefaultStack());
            return ActionResult.SUCCESS;
        }

        return tryEat(world, pos, state, player);
    }

    protected ActionResult tryEat(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!player.canConsume(false)) {
            return ActionResult.PASS;
        }
        player.incrementStat(Stats.EAT_CAKE_SLICE);
        player.getHungerManager().add(state.get(STOMPED) ? 1 : 2, 0.1f);

        world.emitGameEvent(player, GameEvent.EAT, pos);
        SoundEmitter.playSoundAt(player, USounds.Vanilla.ENTITY_GENERIC_EAT, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
        if (world instanceof World ww && (!player.canConsume(false) || world.getRandom().nextInt(10) == 0)) {
            AwaitTickQueue.scheduleTask(ww, w -> {
                SoundEmitter.playSoundAt(player, USounds.Vanilla.ENTITY_PLAYER_BURP, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
            }, 5);
        }

        removeSlice(world, pos, state, player);
        return ActionResult.SUCCESS;
    }

    protected void removeSlice(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
        int bites = state.get(BITES);

        if (bites < MAX_BITES) {
            world.setBlockState(pos, state.with(BITES, bites + 1), Block.NOTIFY_ALL);
        } else {
            world.removeBlock(pos, false);
            world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return (state.get(STOMPED) ? stompedItem : normalItem).asItem().getDefaultStack();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(STOMPED)) {
            Vec3d center = Vec3d.ofCenter(pos);
            world.addParticle(ParticleTypes.SNEEZE,
                    random.nextTriangular(center.getX(), 0.9),
                    random.nextTriangular(center.getY(), 0.9),
                    random.nextTriangular(center.getZ(), 0.9),
                    0,
                    0,
                    0
            );
        } else {
            if (world.random.nextInt(10) == 0) {
                Vec3d center = Vec3d.ofCenter(pos);
                world.addParticle(ParticleTypes.SNEEZE,
                        random.nextTriangular(center.getX(), 0.2),
                        random.nextTriangular(center.getY(), 0.2),
                        random.nextTriangular(center.getZ(), 0.2),
                        0,
                        0.01F,
                        0
                );
            }
        }
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!state.get(STOMPED) && entity instanceof LivingEntity && !entity.bypassesSteppingEffects()) {
            world.setBlockState(pos, state.cycle(STOMPED));
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
            world.emitGameEvent(UGameEvents.PIE_STOMP, pos, GameEvent.Emitter.of(entity, state));
        }
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Deprecated
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos, Direction.UP);
    }

    @Deprecated
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getDefaultState()
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER)
                .with(STOMPED, ctx.getStack().isOf(UItems.APPLE_PIE_HOOF));
    }

    @Deprecated
    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BITES, STOMPED, WATERLOGGED);
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (5 - state.get(BITES)) * 2;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
