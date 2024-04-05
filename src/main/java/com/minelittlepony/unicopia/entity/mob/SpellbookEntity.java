package com.minelittlepony.unicopia.entity.mob;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.AltarRecipeMatch;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpellbookStateChanged;
import com.minelittlepony.unicopia.server.world.Altar;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
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
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.World.ExplosionSourceType;

public class SpellbookEntity extends MobEntity implements MagicImmune {
    private static final TrackedData<Byte> LOCKED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> ALTERED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final byte ALTAR_BEAMS_START = 61;
    private static final byte ALTAR_BEAMS_END = 62;

    private static final int TICKS_TO_SLEEP = 600;

    private int activeTicks = TICKS_TO_SLEEP;
    private boolean prevDaytime;

    private final SpellbookState state = new SpellbookState();

    private Optional<Altar> altar = Optional.empty();

    private boolean hasBeams;
    private int beamsActive;

    @Nullable
    private AltarRecipeMatch activeRecipe;

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
                        Channel.SERVER_SPELLBOOK_UPDATE.sendToPlayer(MsgSpellbookStateChanged.create(book, state), recipient);
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

    public void setAltar(Altar altar) {
        this.altar = Optional.of(altar);
    }

    public Optional<Altar> getAltar() {
        return altar;
    }

    public boolean hasBeams() {
        return hasBeams && altar.isPresent();
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

        if (!getWorld().isClient && age % 15 == 0) {

            if (altar.isEmpty()) {
                altar = Altar.locateAltar(getWorld(), getBlockPos()).map(altar -> {
                    altar.generateDecorations(getWorld());
                    return altar;
                });
            }

            altar = altar.filter(altar -> {
                if (!altar.isValid(getWorld())) {
                    altar.tearDown(null, getWorld());
                    return false;
                }

                tickAltarCrafting(altar);

                Vec3d origin = altar.origin().toCenterPos();
                altar.pillars().forEach(pillar -> tickAltarPillar(origin, pillar));

                return true;
            });
        }
    }

    public void setBeamTicks(int ticks) {
        getWorld().sendEntityStatus(this, ticks > 0 ? ALTAR_BEAMS_START : ALTAR_BEAMS_END);
        beamsActive = ticks;
    }

    private void tickAltarCrafting(Altar altar) {
        if (activeRecipe == null || activeRecipe.isRemoved()) {
            activeRecipe = AltarRecipeMatch.of(getWorld().getEntitiesByClass(ItemEntity.class, Box.of(altar.origin().toCenterPos(), 2, 2, 2), EntityPredicates.VALID_ENTITY));

            if (activeRecipe != null) {
                setBeamTicks(5);
            }
        }

        if (beamsActive <= 0) {
            return;
        }

        if (--beamsActive > 0) {
            playSound(USounds.Vanilla.ENTITY_GUARDIAN_ATTACK, 1.5F, 0.5F);
            return;
        }

        setBeamTicks(0);

        if (activeRecipe == null) {
            return;
        }

        activeRecipe.craft();
        activeRecipe = null;
        getWorld().createExplosion(this, altar.origin().getX(), altar.origin().getY(), altar.origin().getZ(), 0, ExplosionSourceType.NONE);
    }

    private void tickAltarPillar(Vec3d origin, BlockPos pillar) {
        Vec3d center = pillar.toCenterPos().add(
                random.nextTriangular(0.5, 0.2),
                random.nextTriangular(0.5, 0.2),
                random.nextTriangular(0.5, 0.2)
        );

        ((ServerWorld)getWorld()).spawnParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                center.x - 0.5, center.y + 0.5, center.z - 0.5,
                0,
                0.5, 0.5, 0.5, 0);

        if (random.nextInt(12) != 0) {
            return;
        }

        Vec3d vel = center.subtract(origin).normalize();

        ((ServerWorld)getWorld()).spawnParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                center.x - 0.5, center.y + 0.5, center.z - 0.5,
                0,
                vel.x, vel.y, vel.z, -0.2);

        if (random.nextInt(2000) == 0) {
            if (getWorld().getBlockState(pillar).isOf(Blocks.CRYING_OBSIDIAN)) {
                pillar = pillar.down();
            }
            getWorld().setBlockState(pillar, Blocks.CRYING_OBSIDIAN.getDefaultState());
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
    public void remove(RemovalReason reason) {
        super.remove(reason);
        altar.ifPresent(altar -> altar.tearDown(this, getWorld()));
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
    public boolean isImmuneToExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return super.isInvulnerableTo(damageSource) || damageSource.isIn(UTags.DamageTypes.SPELLBOOK_IMMUNE_TO);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        prevDaytime = compound.getBoolean("prevDaytime");
        activeTicks = compound.getInt("activeTicks");
        setAltered(compound.getBoolean("altered"));
        setForcedState(compound.contains("locked") ? TriState.of(compound.getBoolean("locked")) : TriState.DEFAULT);
        state.fromNBT(compound.getCompound("spellbookState"));
        altar = Altar.SERIALIZER.readOptional("altar", compound);
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
        Altar.SERIALIZER.writeOptional("altar", compound, altar);
    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case ALTAR_BEAMS_START:
                altar = Altar.locateAltar(getWorld(), getBlockPos());
                hasBeams = altar.isPresent();
                break;
            case ALTAR_BEAMS_END:
                altar = Optional.empty();
                hasBeams = false;
            default:
                super.handleStatus(status);
        }
    }
}
