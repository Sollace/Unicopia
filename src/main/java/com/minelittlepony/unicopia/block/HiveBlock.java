package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.mojang.serialization.MapCodec;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable.Serializer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;

public class HiveBlock extends ConnectingBlock implements BlockEntityProvider {
    public static final MapCodec<HiveBlock> CODEC = createCodec(HiveBlock::new);
    static final BooleanProperty AWAKE = BooleanProperty.of("awake");
    static final BooleanProperty CONSUMING = BooleanProperty.of("consuming");
    static final Collection<BooleanProperty> PROPERTIES = FACING_PROPERTIES.values();

    public HiveBlock(Settings settings) {
        super(0.5F, settings);
        setDefaultState(getDefaultState().with(AWAKE, false).with(CONSUMING, false));
        PROPERTIES.forEach(property -> {
            setDefaultState(getDefaultState().with(property, true));
        });
    }

    @Override
    protected MapCodec<? extends HiveBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.toArray(Property[]::new));
        builder.add(AWAKE, CONSUMING);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        if (random.nextInt(250) == 0) {
            world.playSoundAtBlockCenter(pos, USounds.BLOCK_CHITIN_AMBIENCE, SoundCategory.BLOCKS, 0.13F, 0.2F, true);

            for (int i = 0; i < 9; i++) {
                world.addParticle(random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                        pos.getX() + random.nextDouble(),
                        pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                        random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                );
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(CONSUMING)) {
            return state;
        }
        boolean connected = !neighborState.isAir();
        state = state.with(FACING_PROPERTIES.get(direction), connected);

        if (!connected) {
            return state.with(AWAKE, true);
        }

        return state;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(CONSUMING) || !state.get(AWAKE)) {
            return;
        }

        for (var property : FACING_PROPERTIES.entrySet()) {
            if (!state.get(property.getValue())) {
                BlockPos neighborPos = pos.offset(property.getKey());
                //world.setBlockState(neighborPos, getDefaultState());
                world.setBlockState(neighborPos, UBlocks.CHITIN.getDefaultState());

                world.playSound(null, neighborPos, USounds.Vanilla.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.BLOCKS);
                world.playSound(null, neighborPos, USounds.BLOCK_CHITIN_AMBIENCE, SoundCategory.BLOCKS, 0.13F, 0.2F);

                for (int i = 0; i < 9; i++) {
                    ParticleUtils.spawnParticle(world, random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                            neighborPos.getX() + random.nextDouble(),
                            neighborPos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                            random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                    );
                }
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (EquineContext.of(player).getCompositeRace().includes(Race.CHANGELING)) {
            world.setBlockState(pos, state.with(CONSUMING, true));
            if (!world.isClient) {
                BlockEntityUtil.getOrCreateBlockEntity(world, pos, UBlockEntities.HIVE_STORAGE).ifPresent(data -> {

                    if (data.opening) {
                        data.opening = false;
                        data.closing = true;
                    } else {
                        data.opening = true;
                        data.closing = false;
                    }
                    data.tickNow = true;
                    data.markDirty();
                });
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Deprecated
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        world.scheduleBlockTick(pos, this, 15);
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return EquineContext.of(context).getSpecies() == Race.CHANGELING ? VoxelShapes.empty() : super.getCollisionShape(state, world, pos, context);
    }

    @Override
    protected float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float delta = super.calcBlockBreakingDelta(state, player, world, pos);
        delta *= Pony.of(player).getSpecies() == Race.CHANGELING ? 2 : 1;
        return delta;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(CONSUMING) ? new TileData(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!state.get(CONSUMING)) {
            return null;
        }
        return BlockEntityUtil.checkType(type, UBlockEntities.HIVE_STORAGE, TileData::tick);
    }

    static class TileData extends BlockEntity implements GameEventListener.Holder<TileData.Listener> {
        private final Map<BlockPos, Entry> storedBlocks = new HashMap<>();
        private final List<Set<BlockPos>> lastConsumed = new ArrayList<>();
        private boolean opening;
        private boolean closing;
        private boolean tickNow;
        private long lastTick;
        private final Listener listener;

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.HIVE_STORAGE, pos, state);
            listener = new Listener(pos);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            opening = nbt.getBoolean("opening");
            closing = nbt.getBoolean("closing");
            storedBlocks.clear();
            if (nbt.contains("storedBlocks", NbtElement.LIST_TYPE)) {
                Entry.SERIALIZER.readAll(nbt.getList("storedBlocks", NbtElement.COMPOUND_TYPE), lookup).forEach(entry -> {
                    storedBlocks.put(entry.pos(), entry);
                });
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            nbt.putBoolean("opening", opening);
            nbt.putBoolean("closing", closing);
            nbt.put("storedBlocks", Entry.SERIALIZER.writeAll(storedBlocks.values(), lookup));
        }

        static void tick(World world, BlockPos pos, BlockState state, TileData data) {
            if (data.tickNow || (world.getTime() > data.lastTick + 2)) {
                data.tickNow = false;
                data.lastTick = world.getTime();
                if (data.closing) {
                    data.stepClosing(world, pos);

                    if (data.lastConsumed.isEmpty() && state.get(CONSUMING)) {
                        world.setBlockState(pos, state.with(CONSUMING, false));
                    }
                } else if (data.opening) {
                    data.stepOpening(world, pos);
                }
            }
        }

        private void stepOpening(World world, BlockPos pos) {
            if (lastConsumed.size() >= 4) {
                return;
            }

            Set<BlockPos> consumed = new HashSet<>();
            for (BlockPos neighbor : lastConsumed.isEmpty() ? Set.of(pos) : lastConsumed.get(lastConsumed.size() - 1)) {
                if (!(neighbor.equals(pos) || (storedBlocks.containsKey(neighbor) && storedBlocks.get(neighbor).state().isOf(UBlocks.CHITIN)))) {
                    continue;
                }
                PosHelper.adjacentNeighbours(neighbor).forEach(adjacent -> {
                    BlockState s = world.getBlockState(adjacent);

                    if (consumed.add(adjacent.toImmutable()) && !storedBlocks.containsKey(adjacent)) {
                        BlockEntity data = world.getBlockEntity(adjacent);
                        storedBlocks.put(adjacent.toImmutable(), new Entry(adjacent.toImmutable(), s, data instanceof TileData ? null : data));

                        if (s.isOf(UBlocks.CHITIN)) {
                            world.breakBlock(adjacent, false);
                        } else if (s.isOf(UBlocks.HIVE)) {
                            world.setBlockState(adjacent, s.with(CONSUMING, true));
                            TileData next = BlockEntityUtil.getOrCreateBlockEntity(world, adjacent.toImmutable(), UBlockEntities.HIVE_STORAGE).orElse(null);
                            if (next != null) {
                                next.opening = opening;
                                next.closing = closing;
                                next.tickNow = true;
                                next.markDirty();
                            }
                        }
                    }
                });
            }
            lastConsumed.add(consumed);
            markDirty();
        }

        private void stepClosing(World world, BlockPos pos) {
            if (lastConsumed.isEmpty()) {
                closing = false;
                markDirty();
                return;
            }

            for (BlockPos neighbor : lastConsumed.remove(lastConsumed.size() - 1)) {
                @Nullable
                Entry entry = storedBlocks.remove(neighbor);
                if (entry != null && !entry.state().isAir()) {
                    if (world.getBlockState(entry.pos()).isOf(UBlocks.HIVE)) {
                        BlockEntityUtil.getOrCreateBlockEntity(world, entry.pos(), UBlockEntities.HIVE_STORAGE).ifPresent(data -> {
                            data.closing = closing;
                            data.opening = opening;
                            data.tickNow = true;
                            data.markDirty();
                        });
                    } else {
                        entry.restore(world);
                    }
                }
            }

            markDirty();
        }

        record Entry (BlockPos pos, BlockState state, @Nullable BlockEntity data) {
            @SuppressWarnings("deprecation")
            public static final Serializer<NbtCompound, Entry> SERIALIZER = Serializer.of((compound, lookup) -> new Entry(
                NbtSerialisable.decode(BlockPos.CODEC, compound.getCompound("pos")).orElse(BlockPos.ORIGIN),
                NbtSerialisable.decode(BlockState.CODEC, compound.get("state")).orElse(Blocks.AIR.getDefaultState()),
                compound.getCompound("data"),
                lookup
            ), (entry, lookup) -> {
                NbtCompound compound = new NbtCompound();
                compound.put("pos", NbtSerialisable.encode(BlockPos.CODEC, entry.pos()));
                compound.put("state", NbtSerialisable.encode(BlockState.CODEC, entry.state()));
                if (entry.data() != null) {
                    compound.put("data", entry.data().createNbtWithId(lookup));
                }
                return compound;
            });

            Entry(BlockPos pos, BlockState state, NbtCompound nbt, WrapperLookup lookup) {
                this(pos, state, BlockEntity.createFromNbt(pos, state, nbt, lookup));
            }

            @SuppressWarnings("deprecation")
            public void restore(World world) {
                if (world.isAir(pos)) {
                    world.setBlockState(pos, state);
                    if (data != null) {
                        data.setCachedState(state);
                        world.addBlockEntity(data);
                    }
                }
            }
        }

        @Override
        public Listener getEventListener() {
            return listener;
        }

        class Listener implements GameEventListener {
            private final PositionSource position;

            Listener(BlockPos pos) {
                this.position = new BlockPositionSource(pos);
            }

            @Override
            public PositionSource getPositionSource() {
                return position;
            }

            @Override
            public int getRange() {
                return 15;
            }

            @Override
            public boolean listen(ServerWorld world, RegistryEntry<GameEvent> event, Emitter emitter, Vec3d emitterPos) {
                if (isImportant(event) || (EquinePredicates.IS_PLAYER.test(emitter.sourceEntity()) && !EquinePredicates.CHANGELING.test(emitter.sourceEntity()))) {
                    closing = true;
                    markDirty();
                    return true;
                }
                return false;
            }

            private boolean isImportant(RegistryEntry<GameEvent> event) {
                return event == GameEvent.EXPLODE
                    || event == GameEvent.PRIME_FUSE
                    || event == GameEvent.SHRIEK;
            }
        }
    }
}
