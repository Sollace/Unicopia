package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.jar.EntityJarContents;
import com.minelittlepony.unicopia.block.jar.FluidOnlyJarContents;
import com.minelittlepony.unicopia.block.jar.ItemsJarContents;
import com.mojang.serialization.MapCodec;
import com.minelittlepony.unicopia.block.jar.FakeFluidJarContents;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ItemJarBlock extends JarBlock implements BlockEntityProvider, InventoryProvider {
    public static final MapCodec<ItemJarBlock> CODEC = createCodec(ItemJarBlock::new);

    public ItemJarBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends TransparentBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.OFF_HAND) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(data -> data.interact(player, hand)).orElse(ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).ifPresent(data -> {
                data.getContents().onDestroyed();
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR)
                .map(TileData::getItems)
                .map(data -> Math.min(16, data.stacks().size()))
                .orElse(0);
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }

    @Nullable
    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(TileData::getItems).orElse(null);
    }

    public static class TileData extends BlockEntity {

        private JarContents contents = new ItemsJarContents(this);

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.ITEM_JAR, pos, state);
        }

        public ItemActionResult interact(PlayerEntity player, Hand hand) {
            TypedActionResult<JarContents> result = contents.interact(player, hand);
            contents = result.getValue();
            return result.getResult().isAccepted() ? ItemActionResult.SUCCESS : result.getResult() == ActionResult.FAIL ? ItemActionResult.FAIL : ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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

        @Nullable
        public FluidJarContents getFluid() {
            return getContents() instanceof FluidJarContents c ? c : null;
        }

        @Nullable
        public FakeFluidJarContents getFakeFluid() {
            return getContents() instanceof FakeFluidJarContents c ? c : null;
        }

        @Override
        public Packet<ClientPlayPacketListener> toUpdatePacket() {
            return BlockEntityUpdateS2CPacket.create(this);
        }

        @Override
        public NbtCompound toInitialChunkDataNbt(WrapperLookup lookup) {
            return createNbt(lookup);
        }

        @Override
        public void markDirty() {
            super.markDirty();
            if (getWorld() instanceof ServerWorld sw) {
                sw.getChunkManager().markForUpdate(getPos());
            }
        }

        @Override
        public void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            if (nbt.contains("items", NbtElement.COMPOUND_TYPE)) {
                contents = new ItemsJarContents(this, nbt.getCompound("items"), lookup);
            } else if (nbt.contains("entity", NbtElement.COMPOUND_TYPE)) {
                contents = new EntityJarContents(this, nbt.getCompound("entity"));
            } else if (nbt.contains("fluid", NbtElement.COMPOUND_TYPE)) {
                contents = new FluidOnlyJarContents(this, nbt.getCompound("fluid"), lookup);
            } else if (nbt.contains("fakeFluid", NbtElement.COMPOUND_TYPE)) {
                contents = new FakeFluidJarContents(this, nbt.getCompound("fakeFluid"));
            }
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            var items = getItems();
            if (items != null) {
                nbt.put("items", items.toNBT(new NbtCompound(), lookup));
            } else if (getEntity() != null) {
                nbt.put("entity", getEntity().toNBT(new NbtCompound(), lookup));
            } else if (getFluid() != null) {
                nbt.put("fluid", getFluid().toNBT(new NbtCompound(), lookup));
            } else if (getFakeFluid() != null) {
                nbt.put("fakeFluid", getFakeFluid().toNBT(new NbtCompound(), lookup));
            }
        }
    }

    public interface JarContents {
        TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand);

        void onDestroyed();

        NbtCompound toNBT(NbtCompound compound, WrapperLookup lookup);

        default void consumeAndSwap(PlayerEntity player, Hand hand, ItemStack output) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(player.getStackInHand(hand), player, output.copy()));
        }
    }

    public interface FluidJarContents extends JarContents {
        FluidVariant fluid();

        default long amount() {
            return FluidConstants.BUCKET;
        }
    }
}
