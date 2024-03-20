package com.minelittlepony.unicopia.block;

import java.util.*;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.EarthPonyKickAbility.Buckable;
import com.minelittlepony.unicopia.compat.seasons.FertilizableUtil;

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

public class FruitBearingBlock extends LeavesBlock implements TintedBlock, Buckable, Fertilizable {
    public static final IntProperty AGE = Properties.AGE_25;
    public static final int MAX_AGE = 25;
    public static final EnumProperty<Stage> STAGE = EnumProperty.of("stage", Stage.class);

    private final Supplier<Block> fruit;
    private final Supplier<ItemStack> rottenFruitSupplier;

    private final int overlay;

    public FruitBearingBlock(Settings settings, int overlay, Supplier<Block> fruit, Supplier<ItemStack> rottenFruitSupplier) {
        super(settings
                .ticksRandomly()
                .nonOpaque()
                .allowsSpawning(BlockConstructionUtils::canSpawnOnLeaves)
                .suffocates(BlockConstructionUtils::never)
                .blockVision(BlockConstructionUtils::never));
        setDefaultState(getDefaultState().with(AGE, 0).with(STAGE, Stage.IDLE));
        this.overlay = overlay;
        this.fruit = fruit;
        this.rottenFruitSupplier = rottenFruitSupplier;
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

    protected boolean shouldAdvance(Random random) {
        return true;
    }

    public BlockState getPlacedFruitState(Random random) {
        return fruit.get().getDefaultState();
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        if (shouldDecay(state) || state.get(PERSISTENT)) {
            return;
        }

        if (world.getBaseLightLevel(pos, 0) > 8) {
            int steps = FertilizableUtil.getGrowthSteps(world, pos, state, random);
            while (steps-- > 0) {
                if (!shouldAdvance(random)) {
                    continue;
                }

                state = cycleStage(state);
                BlockPos fruitPosition = pos.down();
                BlockState fruitState = world.getBlockState(fruitPosition);

                switch (state.get(STAGE)) {
                    case WITHERING:
                        wither(state, world, pos, fruitPosition, world.getBlockState(fruitPosition));
                    case BEARING:
                        if (!fruitState.isOf(fruit.get())) {
                            state = withStage(state, Stage.IDLE);
                        }
                        break;
                    case FRUITING: {
                        if (!isPositionValidForFruit(state, pos)) {
                            state = withStage(state, Stage.IDLE);
                        } else {
                            state = grow(state, world, pos, fruitPosition, fruitState, random);
                        }
                        break;
                    }
                    default:
                }

                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            }
        }
    }

    protected BlockState withStage(BlockState state, Stage stage) {
        return state.with(AGE, 0).with(STAGE, stage);
    }

    private BlockState cycleStage(BlockState state) {
        state = state.cycle(AGE);
        if (state.get(AGE) == 0) {
            state = state.cycle(STAGE);
        }
        return state;
    }

    protected BlockState grow(BlockState state, World world, BlockPos pos, BlockPos fruitPosition, BlockState fruitState, Random random) {
        if (world.isAir(fruitPosition)) {
            world.setBlockState(fruitPosition, getPlacedFruitState(random), Block.NOTIFY_ALL);
            return withStage(state, Stage.BEARING);
        }

        if (!fruitState.isOf(fruit.get())) {
            return withStage(state, Stage.IDLE);
        }
        return state;
    }

    protected void wither(BlockState state, World world, BlockPos pos, BlockPos fruitPosition, BlockState fruitState) {
        if (!fruitState.isOf(fruit.get())) {
            if (world.random.nextInt(2) == 0) {
                Block.dropStack(world, fruitPosition, rottenFruitSupplier.get());
            } else {
                Block.dropStacks(fruitState, world, fruitPosition, fruitState.hasBlockEntity() ? world.getBlockEntity(fruitPosition) : null, null, ItemStack.EMPTY);
            }

            if (world.removeBlock(fruitPosition, false)) {
                world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(fruitState));
            }

            BlockSoundGroup group = getSoundGroup(state);
            world.playSound(null, pos, USounds.ITEM_APPLE_ROT, SoundCategory.BLOCKS, group.getVolume(), group.getPitch());
        }
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

    public boolean isPositionValidForFruit(BlockState state, BlockPos pos) {
        return state.getRenderingSeed(pos) % 3 == 1;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return switch (state.get(STAGE)) {
            case FLOWERING -> world.isAir(pos.down());
            default -> !world.getBlockState(pos.down()).isOf(fruit.get());
        };
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return isFertilizable(world, pos, state);
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        state = state.cycle(AGE);
        if (state.get(AGE) == 0) {
            state = state.with(STAGE, switch (state.get(STAGE)) {
                case IDLE -> Stage.FLOWERING;
                case FLOWERING -> Stage.FRUITING;
                default -> Stage.FLOWERING;
            });
        }
        if (state.get(STAGE) == Stage.FRUITING && state.get(AGE) == 0) {
            state = grow(state, world, pos, pos.down(), world.getBlockState(pos.down()), random);
        }
        world.setBlockState(pos, state);
    }

    public enum Stage implements StringIdentifiable {
        IDLE,
        FLOWERING,
        FRUITING,
        BEARING,
        WITHERING;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
