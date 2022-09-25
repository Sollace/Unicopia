package com.minelittlepony.unicopia.block;

import java.util.*;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.EarthPonyKickAbility.Buckable;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;

public class FruitBearingBlock extends LeavesBlock implements TintedBlock, Buckable {
    public static final IntProperty AGE = Properties.AGE_25;
    public static final int WITHER_AGE = 15;
    public static final EnumProperty<Stage> STAGE = EnumProperty.of("stage", Stage.class);

    public static final List<FruitBearingBlock> REGISTRY = new ArrayList<>();

    private final Supplier<Block> fruit;
    private final Supplier<ItemStack> rottenFruitSupplier;

    private final int overlay;

    public FruitBearingBlock(Settings settings, int overlay, Supplier<Block> fruit, Supplier<ItemStack> rottenFruitSupplier) {
        super(settings
                .ticksRandomly()
                .nonOpaque()
                .allowsSpawning(UBlocks::canSpawnOnLeaves)
                .suffocates(UBlocks::never)
                .blockVision(UBlocks::never));
        setDefaultState(getDefaultState().with(STAGE, Stage.IDLE));
        this.overlay = overlay;
        this.fruit = fruit;
        this.rottenFruitSupplier = rottenFruitSupplier;
        REGISTRY.add(this);
        FlammableBlockRegistry.getDefaultInstance().add(this, 30, 60);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STAGE).add(AGE);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        if (shouldDecay(state) || state.get(PERSISTENT)) {
            return;
        }

        if (world.isDay()) {
            BlockSoundGroup group = getSoundGroup(state);

            if (state.get(STAGE) == Stage.FRUITING) {
                state = state.cycle(AGE);
                if (state.get(AGE) > 20) {
                    state = state.with(AGE, 0).cycle(STAGE);
                }
            } else {
                state = state.with(AGE, 0).cycle(STAGE);
            }
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            BlockPos fruitPosition = pos.down();

            Stage stage = state.get(STAGE);

            if (stage == Stage.FRUITING && isPositionValidForFruit(state, pos)) {
                if (world.isAir(fruitPosition)) {
                    world.setBlockState(fruitPosition, fruit.get().getDefaultState(), Block.NOTIFY_ALL);
                }
            }

            BlockState fruitState = world.getBlockState(fruitPosition);

            if (stage == Stage.WITHERING && fruitState.isOf(fruit.get())) {
                if (world.random.nextInt(2) == 0) {
                    Block.dropStack(world, fruitPosition, rottenFruitSupplier.get());
                } else {
                    Block.dropStacks(fruitState, world, fruitPosition, fruitState.hasBlockEntity() ? world.getBlockEntity(fruitPosition) : null, null, ItemStack.EMPTY);
                }

                if (world.removeBlock(fruitPosition, false)) {
                    world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(fruitState));
                }

                world.playSound(null, pos, USounds.ITEM_APPLE_ROT, SoundCategory.BLOCKS, group.getVolume(), group.getPitch());
            }
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockState newState = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);

        return newState;
    }

    @Override
    public List<ItemStack> onBucked(ServerWorld world, BlockState state, BlockPos pos) {
        world.setBlockState(pos, state.with(STAGE, Stage.IDLE).with(AGE, 0));

        pos = pos.down();
        state = world.getBlockState(pos);
        if (state.isOf(fruit.get()) && state.getBlock() instanceof Buckable buckable) {
            return buckable.onBucked(world, state, pos);
        }
        return List.of();
    }

    @Override
    public int getTint(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int foliageColor) {
        return TintedBlock.blend(foliageColor, overlay);
    }

    private boolean isPositionValidForFruit(BlockState state, BlockPos pos) {
        return state.getRenderingSeed(pos) % 3 == 1;
    }

    public enum Stage implements StringIdentifiable {
        IDLE,
        FLOWERING,
        FRUITING,
        WITHERING;

        private static final Stage[] VALUES = values();

        public Stage getNext() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
