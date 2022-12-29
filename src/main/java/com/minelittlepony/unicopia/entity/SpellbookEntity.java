package com.minelittlepony.unicopia.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpellbookStateChanged;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class SpellbookEntity extends MobEntity {
    private static final TrackedData<Boolean> AWAKE = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> BORED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Byte> LOCKED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> ALTERED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final int TICKS_TO_SLEEP = 2000;

    private int activeTicks = TICKS_TO_SLEEP;

    private final SpellbookState state = new SpellbookState();

    public SpellbookEntity(EntityType<SpellbookEntity> type, World world) {
        super(type, world);
        setPersistent();
        setAltered(world.random.nextInt(3) == 0);
        if (!world.isClient) {
            state.setSynchronizer(state -> {
                getWorld().getPlayers().forEach(player -> {
                    if (player instanceof ServerPlayerEntity recipient
                            && player.currentScreenHandler instanceof SpellbookScreenHandler book
                            && getUuid().equals(book.entityId)) {
                        Channel.SERVER_SPELLBOOK_UPDATE.sendToPlayer(new MsgSpellbookStateChanged<>(player.currentScreenHandler.syncId, state), recipient);
                    }
                });
            });
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(AWAKE, true);
        dataTracker.startTracking(BORED, false);
        dataTracker.startTracking(LOCKED, (byte)1);
        dataTracker.startTracking(ALTERED, false);
    }

    public SpellbookState getSpellbookState() {
        return state;
    }

    @Override
    public ItemStack getPickBlockStack() {
        ItemStack stack = UItems.SPELLBOOK.getDefaultStack();
        stack.getOrCreateNbt().put("spellbookState", state.toNBT());
        return stack;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    public boolean isAltered() {
        return dataTracker.get(ALTERED);
    }

    public void setAltered(boolean altered) {
        dataTracker.set(ALTERED, altered);
    }

    protected void setLocked(TriState closed) {
        dataTracker.set(LOCKED, (byte)closed.ordinal());
    }

    @Nullable
    protected TriState isLocked() {
        return TriState.values()[Math.abs(dataTracker.get(LOCKED)) % 3];
    }

    public boolean isOpen() {
        return isLocked().orElse(isAwake()) && !isBored();
    }

    public boolean isAwake() {
        return dataTracker.get(AWAKE);
    }

    public void setAwake(boolean awake) {
        if (awake != isAwake()) {
            dataTracker.set(AWAKE, awake);
        }
    }

    public boolean isBored() {
        return dataTracker.get(BORED);
    }

    public void setBored(boolean bored) {
        activeTicks = TICKS_TO_SLEEP;
        if (bored != isBored()) {
            dataTracker.set(BORED, bored);
        }
    }

    @Override
    public void tick() {
        boolean awake = isAwake();
        jumping = awake && isTouchingWater();
        super.tick();

        if (world.isClient && isOpen()) {
            for (int offX = -2; offX <= 1; ++offX) {
                for (int offZ = -2; offZ <= 1; ++offZ) {
                    if (offX > -1 && offX < 1 && offZ == -1) {
                        offZ = 1;
                    }

                    if (random.nextInt(320) == 0) {
                        for (int offY = 0; offY <= 1; ++offY) {
                            world.addParticle(ParticleTypes.ENCHANT,
                                    getX(), getY(), getZ(),
                                    offX/2F + random.nextFloat(),
                                    offY/2F - random.nextFloat() + 0.5f,
                                    offZ/2F + random.nextFloat()
                            );
                        }
                    }
                }
            }
        }

        if (awake) {
            world.getOtherEntities(this, getBoundingBox().expand(2), EquinePredicates.PLAYER_UNICORN.and(e -> e instanceof PlayerEntity)).stream().findFirst().ifPresent(player -> {
                setBored(false);
                if (isOpen()) {
                    Vec3d diff = player.getPos().subtract(getPos());
                    double yaw = Math.atan2(diff.z, diff.x) * 180D / Math.PI - 90;

                    setHeadYaw((float)yaw);
                    setBodyYaw((float)yaw);
                }
            });

            if (!world.isClient) {
                if (activeTicks > 0 && --activeTicks <= 0) {
                    setBored(true);
                }
            }
        }

        if (!world.isClient && world.random.nextInt(30) == 0) {
            float celest = world.getSkyAngle(1) * 4;

            boolean daytime = celest > 3 || celest < 1;

            setAwake(daytime);

            if (daytime != awake && daytime == isLocked().orElse(daytime)) {
                setLocked(TriState.DEFAULT);
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!world.isClient) {
            remove(Entity.RemovalReason.KILLED);

            BlockSoundGroup sound = BlockSoundGroup.WOOD;

            world.playSound(getX(), getY(), getZ(), sound.getBreakSound(), SoundCategory.BLOCKS, sound.getVolume(), sound.getPitch(), true);

            if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
                dropStack(getPickBlockStack(), 1);
            }
        }
        return false;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        if (player.isSneaking()) {
            setBored(false);
            setAwake(!isOpen());
            setLocked(TriState.of(isAwake()));
            player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 2, 1);
            return ActionResult.SUCCESS;
        }

        if (isOpen()) {
            setBored(false);
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return SpellbookEntity.this.getDisplayName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new SpellbookScreenHandler(syncId, inv, ScreenHandlerContext.create(world, getBlockPos()), state, getUuid());
                }

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    state.toPacket(buf);
                }
            });
            player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 2, 1);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setAwake(compound.getBoolean("awake"));
        setBored(compound.getBoolean("bored"));
        setAltered(compound.getBoolean("altered"));
        setLocked(compound.contains("locked") ? TriState.of(compound.getBoolean("locked")) : TriState.DEFAULT);

        state.fromNBT(compound.getCompound("spellbookState"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putBoolean("awake", isAwake());
        compound.putBoolean("bored", isBored());
        compound.putBoolean("altered", isAltered());

        TriState locked = isLocked();
        if (locked != TriState.DEFAULT) {
            compound.putBoolean("locked", locked.get());
        }

        compound.put("spellbookState", state.toNBT());
    }
}
