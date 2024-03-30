package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.mixin.MixinEntityBucketItem;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ItemJarBlock extends JarBlock implements BlockEntityProvider, InventoryProvider {

    public ItemJarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.OFF_HAND) {
            return ActionResult.PASS;
        }
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(data -> data.interact(player, hand)).orElse(ActionResult.PASS);
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).ifPresent(data -> {
                data.getContents().onDestroyed();
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR)
                .map(TileData::getItems)
                .map(data -> Math.min(16, data.getStacks().size()))
                .orElse(0);
    }

    @Deprecated
    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }


    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(TileData::getItems).orElse(null);
    }

    public static class TileData extends BlockEntity {

        private JarContents contents = new ItemsJarContents(this);

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.ITEM_JAR, pos, state);
        }

        public ActionResult interact(PlayerEntity player, Hand hand) {
            TypedActionResult<JarContents> result = contents.interact(player, hand);
            contents = result.getValue();
            return result.getResult();
        }

        public JarContents getContents() {
            return contents;
        }

        @Nullable
        public ItemsJarContents getItems() {
            return getContents() instanceof ItemsJarContents c ? c : null;
        }

        @Nullable
        public EntityJarContents getEntity() {
            return getContents() instanceof EntityJarContents c ? c : null;
        }

        @Override
        public Packet<ClientPlayPacketListener> toUpdatePacket() {
            return BlockEntityUpdateS2CPacket.create(this);
        }

        @Override
        public NbtCompound toInitialChunkDataNbt() {
            return createNbt();
        }

        @Override
        public void markDirty() {
            super.markDirty();
            if (getWorld() instanceof ServerWorld sw) {
                sw.getChunkManager().markForUpdate(getPos());
            }
        }

        @Override
        public void readNbt(NbtCompound nbt) {
            if (nbt.contains("items", NbtElement.COMPOUND_TYPE)) {
                contents = new ItemsJarContents(this);
                contents.fromNBT(nbt.getCompound("items"));
            } else if (nbt.contains("entity", NbtElement.COMPOUND_TYPE)) {
                contents = new EntityJarContents(this);
                contents.fromNBT(nbt.getCompound("entity"));
            }
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
            var items = getItems();
            if (items != null) {
                nbt.put("items", items.toNBT());
            } else if (getEntity() != null) {
                nbt.put("entity", getEntity().toNBT());
            }
        }
    }


    public interface JarContents extends NbtSerialisable {
        TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand);

        void onDestroyed();
    }

    public static class EntityJarContents implements JarContents {
        @Nullable
        private EntityType<?> entityType;
        @Nullable
        private Entity renderEntity;

        private final TileData tile;

        public EntityJarContents(TileData tile) {
            this(tile, null);
        }

        public EntityJarContents(TileData tile, EntityType<?> entityType) {
            this.tile = tile;
            this.entityType = entityType;
        }

        @Nullable
        public Entity getOrCreateEntity() {
            if (entityType == null && tile.getWorld() != null) {
                return null;
            }

            if (renderEntity == null || renderEntity.getType() != entityType) {
                renderEntity = entityType.create(tile.getWorld());
            }
            return renderEntity;
        }

        @Override
        public TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(Items.BUCKET)) {
                if (getOrCreateEntity() instanceof Bucketable bucketable) {
                    if (!player.isCreative()) {
                        stack.decrement(1);
                        if (stack.isEmpty()) {
                            player.setStackInHand(hand, bucketable.getBucketItem());
                        } else {
                            player.giveItemStack(bucketable.getBucketItem());
                        }
                    }
                    player.playSound(bucketable.getBucketFillSound(), 1, 1);
                }
                tile.markDirty();
                return TypedActionResult.success(new ItemsJarContents(tile));
            }
            return TypedActionResult.pass(this);
        }

        @Override
        public void onDestroyed() {
            tile.getWorld().setBlockState(tile.getPos(), Blocks.WATER.getDefaultState());
            Entity entity = getOrCreateEntity();
            if (entity != null) {
                entity.refreshPositionAfterTeleport(tile.getPos().toCenterPos());
                tile.getWorld().spawnEntity(entity);
            }
        }

        @Override
        public void toNBT(NbtCompound compound) {
            compound.putString("entity", EntityType.getId(entityType).toString());
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            entityType = Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(compound.getString("entity"))).orElse(null);
        }

    }

    public static class ItemsJarContents implements JarContents, SidedInventory {
        private static final int[] SLOTS = IntStream.range(0, 16).toArray();

        private final TileData tile;
        private List<ItemStack> stacks = new ArrayList<>();

        public ItemsJarContents(TileData tile) {
            this.tile = tile;
        }

        @Override
        public TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand) {
            ItemStack handStack = player.getStackInHand(hand);

            if (handStack.isEmpty()) {
                if (stacks.isEmpty()) {
                    return TypedActionResult.fail(this);
                }
                dropStack(tile.getWorld(), tile.getPos(), stacks.remove(0));
                markDirty();
                return TypedActionResult.success(this);
            }

            if (stacks.isEmpty()) {
                if (handStack.getItem() instanceof MixinEntityBucketItem bucket) {
                    if (!player.isCreative()) {
                        handStack.decrement(1);
                        if (handStack.isEmpty()) {
                            player.setStackInHand(hand, Items.BUCKET.getDefaultStack());
                        } else {
                            player.giveItemStack(Items.BUCKET.getDefaultStack());
                        }
                    }

                    player.playSound(bucket.getEmptyingSound(), 1, 1);
                    markDirty();
                    return TypedActionResult.success(new EntityJarContents(tile, bucket.getEntityType()));
                }
            }

            if (stacks.size() >= size()) {
                return TypedActionResult.fail(this);
            }
            stacks.add(player.isCreative() ? handStack.copyWithCount(1) : handStack.split(1));
            markDirty();

            return TypedActionResult.success(this);
        }

        @Override
        public void onDestroyed() {
            stacks.forEach(stack -> {
                dropStack(tile.getWorld(), tile.getPos(), stack);
            });
        }

        public List<ItemStack> getStacks() {
            return stacks;
        }

        @Override
        public int size() {
            return 15;
        }

        @Override
        public boolean isEmpty() {
            return stacks.isEmpty();
        }

        @Override
        public ItemStack getStack(int slot) {
            return slot < 0 || slot >= stacks.size() ? ItemStack.EMPTY : stacks.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            if (slot < 0 || slot >= stacks.size()) {
                return ItemStack.EMPTY;
            }

            try {
                ItemStack stack = stacks.get(slot);
                ItemStack removed = stack.split(1);
                if (stack.isEmpty()) {
                    stacks.remove(slot);
                }
                return removed;
            } finally {
                markDirty();
            }
        }

        @Override
        public ItemStack removeStack(int slot) {
            if (slot < 0 || slot >= stacks.size()) {
                return ItemStack.EMPTY;
            }

            try {
                return stacks.remove(slot);
            } finally {
                markDirty();
            }
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= stacks.size()) {
                stacks.add(stack);
            } else {
                stacks.set(slot, stack);
            }
            markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return false;
        }

        @Override
        public void clear() {
            stacks.clear();
            markDirty();
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return SLOTS;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, Direction dir) {
            return slot >= 0 && slot < size() && slot >= stacks.size();
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return slot >= 0 && slot < size() && slot < stacks.size();
        }

        @Override
        public void toNBT(NbtCompound compound) {
            compound.put("items", NbtSerialisable.ITEM_STACK.writeAll(stacks));
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            stacks = NbtSerialisable.ITEM_STACK.readAll(compound.getList("items", NbtElement.COMPOUND_TYPE))
                    .limit(size())
                    .collect(Collectors.toList());
        }

        @Override
        public void markDirty() {
            tile.markDirty();
        }
    }
}
