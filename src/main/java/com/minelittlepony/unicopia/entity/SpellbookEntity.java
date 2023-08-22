package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.player.MeteorlogicalUtil;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class SpellbookEntity extends MobEntity {
    private static final TrackedData<Byte> LOCKED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> ALTERED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final int TICKS_TO_SLEEP = 600;

    private int activeTicks = TICKS_TO_SLEEP;
    private boolean prevDaytime;

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
                        Channel.SERVER_SPELLBOOK_UPDATE.sendToPlayer(new MsgSpellbookStateChanged<>(book.syncId, state), recipient);
                    }
                });
            });
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
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

    protected void setForcedState(TriState state) {
        dataTracker.set(LOCKED, (byte)state.ordinal());
    }

    public void clearForcedState() {
        setForcedState(TriState.DEFAULT);
        activeTicks = 0;
    }

    private TriState getForcedState() {
        if (activeTicks <= 0) {
            setForcedState(TriState.DEFAULT);
        }
        return TriState.values()[Math.abs(dataTracker.get(LOCKED)) % 3];
    }

    public boolean isOpen() {
        return getForcedState().orElse(!shouldBeSleeping());
    }

    public void keepAwake() {
        activeTicks = 100;
    }

    @Override
    public void tick() {
        boolean open = isOpen();
        super.tick();

        if (open && isTouchingWater()) {
            addVelocity(0, 0.01, 0);
            keepAwake();
        }

        if (activeTicks > 0) {
            activeTicks--;
        }

        if (getWorld().isClient && open) {
            for (int offX = -2; offX <= 1; ++offX) {
                for (int offZ = -2; offZ <= 1; ++offZ) {
                    if (offX > -1 && offX < 1 && offZ == -1) {
                        offZ = 1;
                    }

                    if (random.nextInt(320) == 0) {
                        for (int offY = 0; offY <= 1; ++offY) {
                            getWorld().addParticle(ParticleTypes.ENCHANT,
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

        getWorld().getOtherEntities(this, getBoundingBox().expand(2), EquinePredicates.PLAYER_UNICORN.and(e -> e instanceof PlayerEntity)).stream().findFirst().ifPresent(player -> {
            keepAwake();
            if (open) {
                Vec3d diff = player.getPos().subtract(getPos());
                double yaw = Math.atan2(diff.z, diff.x) * 180D / Math.PI - 90;

                setHeadYaw((float)yaw);
                setBodyYaw((float)yaw);
            }
        });

        boolean daytime = MeteorlogicalUtil.getSkyAngle(getWorld()) < 1;
        if (daytime != prevDaytime) {
            prevDaytime = daytime;
            if (daytime != getForcedState().orElse(daytime)) {
                clearForcedState();
            }
        }
    }

    private boolean shouldBeSleeping() {
        return MeteorlogicalUtil.getSkyAngle(getWorld()) > 1 && activeTicks <= 0;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!getWorld().isClient) {
            remove(Entity.RemovalReason.KILLED);

            BlockSoundGroup sound = BlockSoundGroup.WOOD;

            getWorld().playSound(getX(), getY(), getZ(), sound.getBreakSound(), SoundCategory.BLOCKS, sound.getVolume(), sound.getPitch(), true);

            if (getWorld().getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
                dropStack(getPickBlockStack(), 1);
            }
        }
        return false;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        if (player.isSneaking()) {
            setForcedState(TriState.of(!isOpen()));
            keepAwake();
            player.playSound(USounds.Vanilla.ITEM_BOOK_PAGE_TURN, 2, 1);
            return ActionResult.SUCCESS;
        }

        if (isOpen()) {
            keepAwake();
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return SpellbookEntity.this.getDisplayName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new SpellbookScreenHandler(syncId, inv, ScreenHandlerContext.create(getWorld(), getBlockPos()), state, getUuid());
                }

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    state.toPacket(buf);
                }
            });
            player.playSound(USounds.Vanilla.ITEM_BOOK_PAGE_TURN, 2, 1);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean isImmuneToExplosion() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return super.isInvulnerableTo(damageSource) || damageSource.isIn(UTags.SPELLBOOK_IMMUNE_TO);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        prevDaytime = compound.getBoolean("prevDaytime");
        activeTicks = compound.getInt("activeTicks");
        setAltered(compound.getBoolean("altered"));
        setForcedState(compound.contains("locked") ? TriState.of(compound.getBoolean("locked")) : TriState.DEFAULT);
        state.fromNBT(compound.getCompound("spellbookState"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putInt("activeTicks", activeTicks);
        compound.putBoolean("prevDaytime", prevDaytime);
        compound.putBoolean("altered", isAltered());
        compound.put("spellbookState", state.toNBT());
        getForcedState().map(t -> {
            compound.putBoolean("locked", t);
            return null;
        });
    }
}
