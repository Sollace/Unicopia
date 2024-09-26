package com.minelittlepony.unicopia.block;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class CrystalDoorBlock extends DoorBlock implements BlockEntityProvider {
    public static final BooleanProperty LOCKED = Properties.LOCKED;
    public static final MapCodec<CrystalDoorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(CrystalDoorBlock::getBlockSetType),
            DoorBlock.createSettingsCodec()
    ).apply(instance, CrystalDoorBlock::new));

    public CrystalDoorBlock(BlockSetType blockSet, Settings settings) {
        super(blockSet, settings);
        setDefaultState(getDefaultState().with(LOCKED, false));
    }

    @Override
    public MapCodec<? extends CrystalDoorBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LOCKED);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(LOCKED);
    }

    @Deprecated
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (!isLocked(world, pos)) {
            setOnGuard(state, world, pos, false);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!state.get(LOCKED)) {
            boolean powered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.offset(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
            if (!getDefaultState().isOf(sourceBlock) && powered != state.get(POWERED)) {
                if (powered) {
                    state = state.cycle(OPEN);
                    playOpenCloseSound(null, world, pos, state.get(OPEN));
                    world.emitGameEvent(null, state.get(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                }

                world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_LISTENERS);
            }
        }

        if (state.get(HALF) == DoubleBlockHalf.LOWER && sourcePos.getY() == pos.getY() - 1) {
            if (!canPlaceAt(state, world, pos) && world.isAir(sourcePos)) {
                world.setBlockState(sourcePos, Blocks.DIRT.getDefaultState());
            }
        }
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!shouldProvideAccess(world, pos, player)) {
            if (isLocked(world, pos) || !stack.isOf(UItems.MEADOWBROOKS_STAFF)) {
                playOpenCloseSound(player, world, pos, false);
                setOnGuard(state, world, pos, true);
                return ItemActionResult.CONSUME;
            } else {
                world.playSound(player, pos, USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
            }
        } else if (!isLocked(world, pos)) {
            if (stack.isOf(UItems.FRIENDSHIP_BRACELET)) {
                @Nullable
                UUID signator = FriendshipBraceletItem.getSignatorId(stack);
                if (signator != null) {
                    BlockEntityUtil.getOrCreateBlockEntity(world, state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos, UBlockEntities.CRYSTAL_DOOR).ifPresent(data -> {
                        data.setSignator(signator);
                        setOnGuard(state, world, pos, true);
                        world.playSound(player, pos, USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
                    });
                    return ItemActionResult.SUCCESS;
                }
            } else {
                setOnGuard(state, world, pos, false);
            }
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    private void setOnGuard(BlockState state, World world, BlockPos pos, boolean locked) {
        world.setBlockState(pos, state.with(LOCKED, locked));
        pos = pos.offset(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);
        state = world.getBlockState(pos);
        if (state.isOf(this)) {
            world.setBlockState(pos, state.with(LOCKED, locked));
        }
    }

    private boolean shouldProvideAccess(World world, BlockPos pos, PlayerEntity player) {
        UUID signator = getSignator(world, pos);
        if (signator != null) {
            return signator.equals(player.getUuid()) || FriendshipBraceletItem.isComrade(signator, player);
        }
        return EquineContext.of(player).getCompositeRace().any(Race::canCast);
    }

    private boolean isLocked(World world, BlockPos pos) {
        return getSignator(world, pos) != null;
    }

    @Nullable
    private UUID getSignator(World world, BlockPos pos) {
        pos = world.getBlockState(pos).get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos;
        var d = world.getBlockEntity(pos, UBlockEntities.CRYSTAL_DOOR);
        return d.map(data -> data.signator).orElse(null);
    }

    private void playOpenCloseSound(@Nullable Entity entity, World world, BlockPos pos, boolean open) {
        world.playSound(entity, pos, open ? getBlockSetType().doorOpen() : getBlockSetType().doorClose(), SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }

    public static class TileData extends BlockEntity {
        @Nullable
        private UUID signator;

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.CRYSTAL_DOOR, pos, state);
        }

        public void setSignator(UUID signator) {
            this.signator = signator;
            markDirty();
        }

        @Override
        public void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            signator = nbt.containsUuid("signator") ? nbt.getUuid("signator") : null;
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            if (signator != null) {
                nbt.putUuid("signator", signator);
            }
        }

    }
}
