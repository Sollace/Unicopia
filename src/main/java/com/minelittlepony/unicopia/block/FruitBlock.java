package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.EarthPonyKickAbility.Buckable;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class FruitBlock extends Block implements Buckable {
    public static final int DEFAULT_FRUIT_SIZE = 5;
    public static final double DEFAULT_STEM_OFFSET = 2.6F;
    public static final VoxelShape DEFAULT_SHAPE = createFruitShape(DEFAULT_STEM_OFFSET, DEFAULT_FRUIT_SIZE);
    private static final MapCodec<FruitBlock> CODEC = createCodec(FruitBlock::new);

    protected final Direction attachmentFace;
    protected final Set<Block> stem;
    protected final VoxelShape shape;
    protected final RegistryKey<Item> fruitKey;

    @Nullable
    private Item cachedItem;

    public static <T extends FruitBlock> MapCodec<T> createCodec(Function6<Direction, Set<Block>, RegistryKey<Item>, VoxelShape, Boolean, Settings, T> constructor) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Direction.CODEC.fieldOf("attachment_face").forGetter(b -> b.attachmentFace),
                CodecUtils.setOf(Registries.BLOCK.getCodec()).fieldOf("stem").forGetter(b -> b.stem),
                RegistryKey.createCodec(RegistryKeys.ITEM).fieldOf("fruit_item").forGetter(b -> b.fruitKey),
                RecordCodecBuilder.<VoxelShape>create(i -> i.group(
                        Codec.DOUBLE.fieldOf("stem_offset").forGetter(b -> (double)0),
                        Codec.DOUBLE.fieldOf("fruit_offset").forGetter(b -> (double)0)
                ).apply(i, FruitBlock::createFruitShape)).fieldOf("shape").forGetter(b -> b.shape),
                Codec.BOOL.fieldOf("flammable").forGetter(b -> false),
                BedBlock.createSettingsCodec()
        ).apply(instance, constructor));
    }

    public static VoxelShape createFruitShape(double stemOffset, double fruitSize) {
        final double min = (16 - fruitSize) * 0.5;
        final double max = 16 - min;
        final double top = 16 - stemOffset;
        return createCuboidShape(min, top - fruitSize, min, max, top, max);
    }

    @Override
    public MapCodec<? extends FruitBlock> getCodec() {
        return CODEC;
    }

    public FruitBlock(Direction attachmentFace, Block stem, RegistryKey<Item> fruitKey, VoxelShape shape, Settings settings) {
        this(attachmentFace, Set.of(stem), fruitKey, shape, true, settings.sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY));
    }

    public FruitBlock(Direction attachmentFace, Block stem, RegistryKey<Item> fruitKey, VoxelShape shape, boolean flammable, Settings settings) {
        this(attachmentFace, Set.of(stem), fruitKey, shape, flammable, settings);
    }

    public FruitBlock(Direction attachmentFace, Set<Block> stem, RegistryKey<Item> fruitKey, VoxelShape shape, Settings settings) {
        this(attachmentFace, stem, fruitKey, shape, true, settings.sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY));
    }

    public FruitBlock(Direction attachmentFace, Set<Block> stem, RegistryKey<Item> fruitKey, VoxelShape shape, boolean flammable, Settings settings) {
        super(settings.nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never));
        this.attachmentFace = attachmentFace;
        this.stem = stem;
        this.shape = shape;
        this.fruitKey = fruitKey;

        if (flammable) {
            FlammableBlockRegistry.getDefaultInstance().add(this, 20, 50);
        }
    }

    @Override
    public Item asItem() {
        if (cachedItem == null) {
            cachedItem = Registries.ITEM.get(fruitKey);
        }

        return cachedItem;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos attachedPos = pos.offset(attachmentFace.getOpposite());
        BlockState attachedState = world.getBlockState(attachedPos);
        return canAttachTo(attachedState);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!newState.isOf(state.getBlock())) {
            BlockState leaves = world.getBlockState(pos.up());
            if (leaves.contains(FruitBearingBlock.STAGE)) {
                world.setBlockState(pos.up(), leaves.withIfExists(FruitBearingBlock.AGE, 0).with(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.IDLE));
            }
        }
    }

    protected boolean canAttachTo(BlockState state) {
        return stem.contains(state.getBlock());
    }

    @Override
    public List<ItemStack> onBucked(ServerWorld world, BlockState state, BlockPos pos) {
        List<ItemStack> stacks = Block.getDroppedStacks(state, world, pos, null);
        world.breakBlock(pos, false);
        return stacks;
    }
}
