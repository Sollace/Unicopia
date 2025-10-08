package com.minelittlepony.unicopia.entity.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.item.ForageableItem;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class LevitatedItemsInventory implements Copyable<LevitatedItemsInventory>, NbtSerialisable, Tickable {

    private final Pony player;

    private final Int2ObjectMap<LevitatingItemEntity> stacks = new Int2ObjectAVLTreeMap<>();
    private final List<LevitatingItemEntity> pending = new ArrayList<>();
    private int nextSlot = 0;

    private final Map<BlockPos, BlockBreakingRecord> blockBreakingRecords = new HashMap<>();

    public LevitatedItemsInventory(Pony player) {
        this.player = player;
    }

    public void onEntitySpawned(LevitatingItemEntity entity) {
        this.stacks.put(entity.getSlot(), entity);
    }

    public void onEntityDespawned(LevitatingItemEntity entity) {
        if (this.stacks.containsKey(entity.getSlot())) {
            this.stacks.remove(entity.getSlot());
        }
    }

    public boolean addStack(ItemStack stack) {
        LevitatingItemEntity entity = new LevitatingItemEntity(nextSlot++, player.asEntity(), stack);
        onEntitySpawned(entity);
        player.asWorld().spawnEntity(entity);
        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1);
        return true;
    }

    public boolean addPassenger(Entity passenger) {
        double yOffset = 0.1;
        if (passenger.getRootVehicle() instanceof LevitatingItemEntity root) {
            if (root.getMaster() == player.asEntity() && root.getPassengerList().size() == 1 && root.getPassengerList().get(0) == passenger) {
                player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1);
                return true;
            }
            passenger.stopRiding();
            passenger.setPosition(root.getPos());
            yOffset = 0;
            if (root.getPassengerList().isEmpty()) {
                root.kill();
            }
        }
        LevitatingItemEntity entity = new LevitatingItemEntity(nextSlot++, player.asEntity(), ItemStack.EMPTY);
        onEntitySpawned(entity);
        entity.setPosition(passenger.getPos());
        entity.setHoldingPosition(passenger.getPos().add(0, yOffset, 0));
        player.asWorld().spawnEntity(entity);
        if (passenger instanceof ItemEntity i) {
            entity.setStack(i.getStack());
            i.discard();
            return addStack(i.getStack());
        } else {
            passenger.startRiding(entity, true);
        }

        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1);
        return true;
    }

    public void dropEverything() {
        List<LevitatingItemEntity> copy = new ArrayList<>(stacks.values());
        stacks.clear();
        nextSlot = 0;
        copy.forEach(stack -> stack.kill());
    }

    public ActionResult tryUseItem(@Nullable BlockHitResult hit) {
        for (var i : stacks.values()) {
            ItemStack stack = i.getStack();
            if (player.asEntity().getItemCooldownManager().isCoolingDown(stack.getItem())) {
                continue;
            }

            if (ForageableItem.use(player.asEntity(), stack, player.asWorld(), Hand.MAIN_HAND, hit).isAccepted()) {
                return ActionResult.SUCCESS;
            }

            System.out.println((player.isClient() ? "CLIENT" : "SERVER") + " TryUse " + stack);
            var result = stack.useOnBlock(new ItemUsageContext(player.asWorld(), player.asEntity(), Hand.MAIN_HAND, stack, hit));
            if (result.isAccepted()) {
                if (player.asEntity() instanceof ServerPlayerEntity spe) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger(spe, hit.getBlockPos(), stack);
                }
                return result;
            }
        }
        return ActionResult.PASS;
    }

    public boolean startMining(BlockState state, BlockPos pos, Direction direction) {
        blockBreakingRecords.values().removeIf(BlockBreakingRecord::tryDiscard);
        BlockBreakingRecord record = blockBreakingRecords.computeIfAbsent(pos, p -> new BlockBreakingRecord(p, state));
        for (var i : stacks.values()) {
            if (i.startMining(player.asEntity(), record, direction)) {
                return true;
            }
        }
        return false;
    }

    public void stopMining(BlockPos pos) {
        for (var i : stacks.values()) {
            i.stopMining(pos);
        }
    }

    @Override
    public void tick() {
        if (!player.isClient()) {
            List<LevitatingItemEntity> copy = new ArrayList<>(pending);
            pending.clear();
            copy.forEach(player.asWorld()::spawnEntity);
        }

        blockBreakingRecords.values().removeIf(BlockBreakingRecord::tryDiscard);
        stacks.values().removeIf(Entity::isRemoved);

        if (!player.isClient()) {
            float cost = 0;
            for (LevitatingItemEntity a : stacks.values()) {
                cost += 0.01F * a.getStack().getCount();
                cost += 0.1F * a.getPassengerList().size();
                for (LevitatingItemEntity b : stacks.values()) {
                    if (a != b) {
                        if (a.getRelativePosition().distanceTo(b.getRelativePosition()) < 0.8) {
                            Vec3d delta = a.getRelativePosition().subtract(b.getRelativePosition()).multiply(0.5);
                            a.setRelativePosition(a.getRelativePosition().add(delta));
                            b.setRelativePosition(b.getRelativePosition().subtract(delta));
                        }
                    }
                }
            }

            player.subtractEnergyCost(cost);
        }

    }

    @Override
    public void copyFrom(LevitatedItemsInventory other, boolean alive) {
        if (alive) {
            for (var stack : other.stacks.values()) {
                addStack(stack.getStack());
                stack.discard();
            }
        }
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        NbtList stacks = new NbtList();
        for (var stack : this.stacks.values()) {
            stacks.add(stack.writeNbt(new NbtCompound()));
        }
        compound.put("stacks", stacks);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        NbtList stacks = compound.getList("stacks", NbtElement.COMPOUND_TYPE);
        this.stacks.clear();
        for (int i = 0; i < stacks.size(); i++) {
            LevitatingItemEntity stack = new LevitatingItemEntity(i, player.asEntity(), ItemStack.EMPTY);
            stack.readNbt(stacks.getCompound(i));
            stack.setSlot(i);
            onEntitySpawned(stack);
            if (!player.isClient()) {
                this.pending.add(stack);
            }
        }
        nextSlot = this.stacks.size();
    }

    public class BlockBreakingRecord {
        public final BlockPos pos;
        public final BlockState state;

        public float breakingProgress;
        public int breakingStage;

        public final Set<LevitatingItemEntity> claimants = new HashSet<>();

        public BlockBreakingRecord(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        public boolean tryDiscard() {
            if (breakingProgress >= 1F || player.asWorld().getBlockState(pos) != state || claimants.isEmpty()) {
                new HashSet<>(claimants).forEach(claimant -> claimant.stopMining(pos));
                claimants.clear();
                return true;
            }

            return false;
        }
    }
}
