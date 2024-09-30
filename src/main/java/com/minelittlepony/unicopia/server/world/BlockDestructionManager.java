package com.minelittlepony.unicopia.server.world;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgBlockDestruction;
import com.minelittlepony.unicopia.util.Tickable;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockDestructionManager implements Tickable {
    private static final Identifier ID = Unicopia.id("destruction_manager");

    public static final int DESTRUCTION_COOLDOWN = 50;
    public static final int UNSET_DAMAGE = -1;
    public static final int MAX_DAMAGE = 10;

    private final WorldOverlay<Destruction> chunks;

    public static Supplier<BlockDestructionManager> create(World world) {
        return Suppliers.memoize(() -> new BlockDestructionManager(world));
    }

    public static BlockDestructionManager of(World world) {
        return ((BlockDestructionManager.Source)world).getDestructionManager();
    }

    private BlockDestructionManager(World world) {
        this.chunks = WorldOverlay.getOverlay(world, ID, w -> new WorldOverlay<>(world, Destruction::new, this::sendUpdates));
    }

    public float getBlockDestruction(BlockPos pos) {
        Destruction destr = chunks.getState(pos);
        return destr == null ? UNSET_DAMAGE : destr.amount;
    }

    public void setBlockDestruction(BlockPos pos, float amount) {
        chunks.getOrCreateState(pos).set(amount);
        chunks.markDirty();
    }

    public float damageBlock(BlockPos pos, float amount) {
        if (amount == 0) {
            return getBlockDestruction(pos);
        }
        amount = MathHelper.clamp(Math.max(getBlockDestruction(pos), 0) + amount, 0, MAX_DAMAGE);
        setBlockDestruction(pos, amount);
        return amount;
    }

    public void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newstate) {
        if (oldState.getBlock() != newstate.getBlock()) {
            setBlockDestruction(pos, UNSET_DAMAGE);
        }
    }

    @Override
    public void tick() {
        chunks.tick();
    }

    private void sendUpdates(Long2ObjectMap<Destruction> destructions, List<ServerPlayerEntity> players) {
        Long2ObjectOpenHashMap<Float> values = new Long2ObjectOpenHashMap<>();

        destructions.forEach((blockPos, item) -> {
            if (item.dirty) {
                item.dirty = false;
                values.put(blockPos.longValue(), (Float)item.amount);
            }
        });

        var packet = Channel.SERVER_BLOCK_DESTRUCTION.toPacket(new MsgBlockDestruction(values));
        players.forEach(player -> {
            if (player instanceof ServerPlayerEntity spe) {
                spe.networkHandler.sendPacket(packet);
            }
        });
    }

    private class Destruction implements WorldOverlay.State {
        float amount = UNSET_DAMAGE;
        int age = DESTRUCTION_COOLDOWN;
        boolean dirty;

        @Override
        public boolean tick() {
            if (age-- > 0) {
                return false;
            }

            if (amount >= 0) {
                set(amount - 1);
            }
            return amount < 0 || age-- <= 0;
        }

        void set(float amount) {
            this.age = DESTRUCTION_COOLDOWN;
            this.amount = amount >= 0 && amount < MAX_DAMAGE ? amount : UNSET_DAMAGE;
            this.dirty = true;
        }

        @Override
        public void toNBT(NbtCompound compound, WrapperLookup lookup) {
            compound.putFloat("destruction", amount);
            compound.putInt("age", age);
        }

        @Override
        public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
            amount = compound.getFloat("destruction");
            age = compound.getInt("age");
            dirty = true;
        }
    }

    public interface Source {
        BlockDestructionManager getDestructionManager();
    }
}
