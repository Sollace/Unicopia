package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.jar.EntityJarContents;
import com.minelittlepony.unicopia.block.jar.FluidOnlyJarContents;
import com.minelittlepony.unicopia.block.jar.ItemsJarContents;
import com.minelittlepony.unicopia.util.TypedActionResult;
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
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.OFF_HAND) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(data -> data.interact(player, hand)).orElse(ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved && !state.isOf(world.getBlockState(pos).getBlock())) {
            world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).ifPresent(data -> {
                data.getContents().onDestroyed();
            });
        }
        super.onStateReplaced(state, world, pos, moved);
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

        public ActionResult interact(PlayerEntity player, Hand hand) {
            TypedActionResult<JarContents> result = contents.interact(player, hand);
            contents = result.value();
            return result.result().isAccepted() ? ActionResult.SUCCESS : result.result() == ActionResult.FAIL ? ActionResult.FAIL : ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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
            if (nbt.contains("items")) {
                contents = new ItemsJarContents(this, nbt.getCompoundOrEmpty("items"), lookup);
            } else if (nbt.contains("entity")) {
                contents = new EntityJarContents(this, nbt.getCompoundOrEmpty("entity"));
            } else if (nbt.contains("fluid")) {
                contents = new FluidOnlyJarContents(this, nbt.getCompoundOrEmpty("fluid"), lookup);
            } else if (nbt.contains("fakeFluid")) {
                contents = new FakeFluidJarContents(this, nbt.getCompoundOrEmpty("fakeFluid"));
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
