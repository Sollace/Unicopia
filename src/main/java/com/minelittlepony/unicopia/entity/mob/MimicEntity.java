package com.minelittlepony.unicopia.entity.mob;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MimicEntity extends PathAwareEntity {
    static final byte OPEN_MOUTH = (byte)200;
    static final byte CLOSE_MOUTH = (byte)201;
    static final TrackedData<NbtCompound> CHEST_DATA = DataTracker.registerData(MimicEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    static final TrackedData<Boolean> MOUTH_OPEN = DataTracker.registerData(MimicEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Nullable
    private ChestBlockEntity chestData;

    private int openTicks;
    private final Set<PlayerEntity> observingPlayers = new HashSet<>();

    public static boolean shouldConvert(World world, BlockPos pos, PlayerEntity player, RegistryKey<LootTable> lootTable) {
        if (!shouldGenerateMimic(lootTable)
                || !world.getBlockState(pos).isIn(UTags.Blocks.MIMIC_CHESTS)
                || !(world.getBlockEntity(pos) instanceof ChestBlockEntity be)
                || be.getCachedState().getOrEmpty(ChestBlock.CHEST_TYPE).orElse(ChestType.SINGLE) != ChestType.SINGLE) {
            return false;
        }

        // TODO: Local difficulty?
        int difficulty = world.getDifficulty().ordinal() - 1;
        float threshold = 0.35F * ((EnchantmentUtil.getLuck(0, player) / 20F) + 0.5F);
        return difficulty > 0 && world.random.nextFloat() < (difficulty / 3F) * threshold;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static MimicEntity spawnFromChest(World world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof ChestBlockEntity be)) {
            return null;
        }
        world.removeBlockEntity(pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        MimicEntity mimic = UEntities.MIMIC.create(world);
        BlockState state = be.getCachedState();
        Direction facing = state.getOrEmpty(ChestBlock.FACING).orElse(null);
        float yaw = facing.asRotation();
        be.setCachedState(be.getCachedState().getBlock().getDefaultState());
        mimic.updatePositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        mimic.setHeadYaw(yaw);
        mimic.setBodyYaw(yaw);
        mimic.setYaw(yaw);
        mimic.setChest(be);
        world.spawnEntity(mimic);
        return mimic;
    }

    public static boolean shouldGenerateMimic(@Nullable RegistryKey<LootTable> lootTable) {
        return lootTable != null
             && lootTable.getValue().getPath().indexOf("village") == -1
             && lootTable.getValue().getPath().indexOf("bastion") == -1
             && lootTable.getValue().getPath().indexOf("underwater") == -1
             && lootTable.getValue().getPath().indexOf("shipwreck") == -1
             && !LootTables.SPAWN_BONUS_CHEST.equals(lootTable);
    }

    MimicEntity(EntityType<? extends MimicEntity> type, World world) {
        super(type, world);
        ignoreCameraFrustum = true;
    }

    @Override
    protected void initDataTracker(Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHEST_DATA, new NbtCompound());
        builder.add(MOUTH_OPEN, false);
    }

    @Override
    public boolean isCollidable() {
        return isAlive();
    }

    @Nullable
    @Override
    public ItemStack getPickBlockStack() {
        Item item = chestData.getCachedState().getBlock().asItem();
        return item == Items.AIR ? null : item.getDefaultStack();
    }

    @Override
    protected void initGoals() {
        goalSelector.add(2, new AttackGoal(this, 0.6F, false));
    }

    public void setChest(ChestBlock chest) {
        if (chest.createBlockEntity(getBlockPos(), chest.getDefaultState()) instanceof ChestBlockEntity be) {
            setChest(be);
            be.setLootTable(LootTables.ABANDONED_MINESHAFT_CHEST, getWorld().getRandom().nextLong());
            if (!getWorld().isClient) {
                dataTracker.set(CHEST_DATA, writeChestData(chestData));
            }
        }
    }

    public void setChest(ChestBlockEntity chestData) {
        this.chestData = chestData;
        ((MimicGeneratable)chestData).setAllowMimics(false);
        chestData.setWorld(getWorld());
        if (!getWorld().isClient) {
            dataTracker.set(CHEST_DATA, writeChestData(chestData));
        }
    }

    @Nullable
    public ChestBlockEntity getChestData() {
        return chestData;
    }

    public boolean isMouthOpen() {
        return dataTracker.get(MOUTH_OPEN);
    }

    public void setMouthOpen(boolean mouthOpen) {
        if (mouthOpen == isMouthOpen()) {
            return;
        }

        playSound(mouthOpen ? SoundEvents.BLOCK_CHEST_OPEN : SoundEvents.BLOCK_CHEST_CLOSE, 0.5F, 1F);
        dataTracker.set(MOUTH_OPEN, mouthOpen);
        if (chestData != null) {
            chestData.onSyncedBlockEvent(1, mouthOpen ? 1 : 0);
        }
    }

    public void playChompAnimation() {
        openTicks = 5;
        setMouthOpen(true);
    }

    public float getPeekAmount() {
        return MathHelper.clamp((float)getVelocity().lengthSquared() * 50F, 0, 1);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isClient) {
            if (age < 12 || (getTarget() == null && this.lastAttackedTicks < age - 30)) {
                BlockPos pos = getBlockPos();
                setPosition(
                        pos.getX() + 0.5,
                        getY(),
                        pos.getZ() + 0.5
                );
                setBodyYaw(MathHelper.floor(getBodyYaw() / 90) * 90);
                setYaw(MathHelper.floor(getYaw() / 90) * 90);
                setHeadYaw(MathHelper.floor(getHeadYaw() / 90) * 90);
                if (getHealth() < getMaxHealth() && getWorld().random.nextInt(20) == 0) {
                    heal(1);
                } else if (age % 150 == 0 && chestData != null && !isMouthOpen()) {
                    if (getWorld().getClosestPlayer(this, 15) == null) {
                        getWorld().setBlockState(getBlockPos(), chestData.getCachedState().withIfExists(ChestBlock.FACING, getHorizontalFacing()));
                        if (getWorld().getBlockEntity(getBlockPos()) instanceof ChestBlockEntity be) {
                            InventoryUtil.copyInto(chestData, be);
                            ((MimicGeneratable)be).setMimic(true);
                            discard();
                        }
                    }
                }
            }

            if (!observingPlayers.isEmpty()) {
                setMouthOpen(true);
            }
        }

        if (chestData == null) {
            setChest((ChestBlock)Blocks.CHEST);
        }

        if (getWorld().isClient) {
            ((MixinBlockEntity)chestData).setPos(getBlockPos());
            ChestBlockEntity.clientTick(getWorld(), getBlockPos(), chestData.getCachedState(), chestData);
        }

        if (openTicks > 0 && --openTicks <= 0) {
            setMouthOpen(false);
        }

        if (getTarget() != null) {
            if (openTicks <= 0 && age % 20 == 0) {
                playChompAnimation();
            }
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (getTarget() == null && chestData != null) {
            player.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return chestData.getDisplayName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return createScreenHandler(syncId, inv, player);
                }
            });

            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public ScreenHandler createScreenHandler(int syncId, PlayerInventory inv, PlayerEntity player) {
        chestData.generateLoot(player);
        setChest(chestData);
        var inventory = InventoryUtil.copyInto(chestData, new SimpleInventory(chestData.size()) {
            @Override
            public void onOpen(PlayerEntity player) {
                observingPlayers.add(player);
                //setMouthOpen(true);
            }

            @Override
            public void onClose(PlayerEntity player) {
                observingPlayers.remove(player);
                setMouthOpen(!observingPlayers.isEmpty());
            }
        });
        inventory.addListener(sender -> {
            if (InventoryUtil.contentEquals(inventory, chestData)) {
                return;
            }
            observingPlayers.clear();
            playChompAnimation();
            setTarget(player);
            if (player instanceof ServerPlayerEntity spe) {
                spe.closeHandledScreen();
            }
        });
        return GenericContainerScreenHandler.createGeneric9x3(syncId, inv, inventory);
    }

    @Override
    public void setAttacking(boolean attacking) {
        super.setAttacking(attacking);
        if (attacking) {
            playChompAnimation();
        }
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        playChompAnimation();
        if (source.getAttacker()  instanceof LivingEntity l) {
            setTarget(l);
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (CHEST_DATA.equals(data) && getWorld().isClient) {
            setChest(readChestData(dataTracker.get(CHEST_DATA)));
        } else if (MOUTH_OPEN.equals(data)) {
            if (chestData != null) {
                chestData.onSyncedBlockEvent(1, isMouthOpen() ? 1 : 0);
            }
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("chest", NbtElement.COMPOUND_TYPE)) {
            chestData = readChestData(nbt.getCompound("chest"));
        } else {
            chestData = null;
        }
    }

    @Nullable
    private ChestBlockEntity readChestData(NbtCompound nbt) {
        BlockState state = BlockState.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("state")).result().get().getFirst();
        if (BlockEntity.createFromNbt(getBlockPos(), state, nbt.getCompound("data"), getRegistryManager()) instanceof ChestBlockEntity data) {
            data.setWorld(getWorld());
            ((MimicGeneratable)data).setAllowMimics(false);
            return data;
        }
        return null;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (chestData != null) {
            nbt.put("chest", writeChestData(chestData));
        }
    }

    @Nullable
    private NbtCompound writeChestData(ChestBlockEntity chestData) {
        NbtCompound chest = new NbtCompound();
        chest.put("data", chestData.createNbtWithId(getRegistryManager()));
        chest.put("state", BlockState.CODEC.encode(chestData.getCachedState(), NbtOps.INSTANCE, new NbtCompound()).result().get());
        return chest;
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        if (chestData != null) {
            ItemScatterer.spawn(getWorld(), this, chestData);
            ItemScatterer.spawn(getWorld(), getX(), getY(), getZ(), chestData.getCachedState().getBlock().asItem().getDefaultStack());
        }
    }

    public class AttackGoal extends MeleeAttackGoal {
        private int ticks;

        public AttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
            super(mob, speed, pauseWhenMobIdle);
        }

        @Override
        public void start() {
            super.start();
            this.ticks = 0;
        }

        @Override
        public void stop() {
            super.stop();
            setAttacking(false);
        }

        @Override
        public void tick() {
            super.tick();
            ++ticks;
            if (ticks >= 5 && getCooldown() < getMaxCooldown() / 2) {
                setAttacking(true);
            } else {
                setAttacking(false);
            }
        }
    }

    public interface MimicGeneratable {
        void setAllowMimics(boolean allowMimics);

        void setMimic(boolean mimic);

        void readMimicAttributes(NbtCompound nbt);

        void writeMimicAttributes(NbtCompound nbt);

        void configureMimic(@Nullable PlayerEntity player);
    }
}
