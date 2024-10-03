package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HayBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EdibleBlock extends HayBlock {
    private static final List<EdibleBlock> REGISTRY = new ArrayList<>();

    static final BooleanProperty TOP_NORTH_EAST = BooleanProperty.of("top_north_east");
    static final BooleanProperty TOP_NORTH_WEST = BooleanProperty.of("top_north_west");
    static final BooleanProperty TOP_SOUTH_EAST = BooleanProperty.of("top_south_east");
    static final BooleanProperty TOP_SOUTH_WEST = BooleanProperty.of("top_south_west");

    static final BooleanProperty BOTTOM_NORTH_EAST = BooleanProperty.of("bottom_north_east");
    static final BooleanProperty BOTTOM_NORTH_WEST = BooleanProperty.of("bottom_north_west");
    static final BooleanProperty BOTTOM_SOUTH_EAST = BooleanProperty.of("bottom_south_east");
    static final BooleanProperty BOTTOM_SOUTH_WEST = BooleanProperty.of("bottom_south_west");

    // [up/down][north/south][west/east]
    public static final BooleanProperty[] SEGMENTS = {
            BOTTOM_NORTH_WEST,
            BOTTOM_NORTH_EAST,
            BOTTOM_SOUTH_WEST,
            BOTTOM_SOUTH_EAST,
            TOP_NORTH_WEST,
            TOP_NORTH_EAST,
            TOP_SOUTH_WEST,
            TOP_SOUTH_EAST
    };
    private static final VoxelShape[] SHAPES = {
            Block.createCuboidShape(0, 0, 0, 8, 8, 8),
            Block.createCuboidShape(8, 0, 0, 16, 8, 8),
            Block.createCuboidShape(0, 0, 8, 8, 8, 16),
            Block.createCuboidShape(8, 0, 8, 16, 8, 16),
            Block.createCuboidShape(0, 8, 0, 8, 16, 8),
            Block.createCuboidShape(8, 8, 0, 16, 16, 8),
            Block.createCuboidShape(0, 8, 8, 8, 16, 16),
            Block.createCuboidShape(8, 8, 8, 16, 16, 16)
    };
    private static final Function<BlockState, VoxelShape> SHAPE_CACHE = Util.memoize(state -> {
        @Nullable
        VoxelShape shape = null;
        for (int i = 0; i < SEGMENTS.length; i++) {
            if (state.get(SEGMENTS[i])) {
                shape = shape == null ? SHAPES[i] : VoxelShapes.union(shape, SHAPES[i]);
            }
        }
        return shape == null ? VoxelShapes.fullCube() : shape.simplify();
    });

    static void bootstrap() {
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (!Pony.of(player).getSpecies().isEquine()
                    || (player.shouldCancelInteraction() && (!player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty()))) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            for (EdibleBlock edibleBlock : REGISTRY) {
                Block match = edibleBlock.getBaseBlock();
                if (match != Blocks.AIR && state.isOf(match)) {
                    BlockState copiedState = StateUtil.copyState(state, edibleBlock.getDefaultState());
                    ItemActionResult result = copiedState.onUseWithItem(player.getStackInHand(hand), world, player, hand, hitResult);

                    if (result.isAccepted()) {
                        return result.toActionResult();
                    }

                    if (result == ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == Hand.MAIN_HAND) {
                        ActionResult actionResult = copiedState.onUse(world, player, hitResult);
                        if (actionResult.isAccepted()) {
                            return actionResult;
                        }
                    }
                }
            }

            return ActionResult.PASS;
        });
    }

    private final Identifier baseBlock;
    private final Identifier material;

    public EdibleBlock(Identifier baseBlock, Identifier material, boolean register) {
        super(Settings.copy(Blocks.HAY_BLOCK));
        for (BooleanProperty segment : SEGMENTS) {
            setDefaultState(getDefaultState().with(segment, true));
        }
        this.baseBlock = baseBlock;
        this.material = material;
        if (register) {
            REGISTRY.add(this);
            FlammableBlockRegistry.getDefaultInstance().add(this, 60, 20);
        }
    }

    public Block getBaseBlock() {
        return Registries.BLOCK.get(baseBlock);
    }

    @Override
    public String getTranslationKey() {
        return getBaseBlock().getTranslationKey();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SEGMENTS);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE_CACHE.apply(state);
    }

    @Override
    @Deprecated
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSpectator()) {
            return ItemActionResult.FAIL;
        }

        if (!stack.isEmpty() && stack.isOf(Registries.ITEM.get(material))) {
            BooleanProperty segment = getHitCorner(hit, 1);

            if (!state.get(segment)) {
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                if (!world.isClient) {
                    state = state.with(segment, true);
                    if (SHAPE_CACHE.apply(state) == VoxelShapes.fullCube()) {
                        state = StateUtil.copyState(state, getBaseBlock().getDefaultState());
                    }
                    world.setBlockState(pos, state);
                }
                world.playSound(player, pos, getSoundGroup(state).getPlaceSound(), SoundCategory.BLOCKS);

                return ItemActionResult.SUCCESS;
            }

            return ItemActionResult.FAIL;
        }

        BooleanProperty corner = getHitCorner(hit, -1);

        if (!state.get(corner)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean usingHoe = stack.isIn(ItemTags.HOES);

        if (!usingHoe) {
            if (!(player.isCreative() || player.getHungerManager().isNotFull()) || !player.isSneaking()) {
                return ItemActionResult.FAIL;
            }
        }

        if (!world.isClient) {
            state = state.with(corner, false);
            if (SHAPE_CACHE.apply(state) == VoxelShapes.fullCube()) {
                world.removeBlock(pos, false);
            } else {
                world.setBlockState(pos, state);
            }
        }

        if (usingHoe) {
            stack.damage(1, player, LivingEntity.getSlotForHand(hand));
            dropStack(world, pos, Registries.ITEM.get(material).getDefaultStack());
            player.playSound(USounds.Vanilla.ITEM_HOE_TILL, 1, 1);
        } else {
            player.playSound(USounds.Vanilla.ENTITY_GENERIC_EAT, 1, 1);
            if (world.random.nextInt(10) == 0) {
                player.playSound(USounds.Vanilla.ENTITY_PLAYER_BURP, 1, player.getSoundPitch());
            }
            player.getHungerManager().add(2, 1.3F);
        }
        return ItemActionResult.SUCCESS;
    }

    static BooleanProperty getHitCorner(BlockHitResult hit, int direction) {
        Vec3d pos = hit.getPos().add(Vec3d.of(hit.getSide().getVector()).multiply(direction * 0.001F));

        BlockPos bPos = hit.getBlockPos();

        return SEGMENTS[
                  (4 * getIndex(pos.y, bPos.getY()))
                + (2 * getIndex(pos.z, bPos.getZ()))
                + (getIndex(pos.x, bPos.getX()))
        ];
    }

    static int getIndex(double axisHit, int tile) {
        axisHit -= tile;
        return Math.abs(axisHit) > 0.5 ? 1 : 0;
    }
}
