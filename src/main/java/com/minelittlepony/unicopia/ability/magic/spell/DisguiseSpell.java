package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.FlightPredicate;
import com.minelittlepony.unicopia.ability.HeightPredicate;
import com.minelittlepony.unicopia.ability.magic.AttachableSpell;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.CasterUtils;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.Suppressable;
import com.minelittlepony.unicopia.entity.behaviour.EntityBehaviour;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class DisguiseSpell extends AbstractSpell implements AttachableSpell, Suppressable, FlightPredicate, HeightPredicate {

    @Nonnull
    private String entityId = "";

    @Nullable
    private Entity entity;

    @Nullable
    private CompoundTag entityNbt;

    private int suppressionCounter;

    @Override
    public String getName() {
        return "disguise";
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    @Override
    public int getTint() {
        return 0x19E48E;
    }

    @Override
    public boolean isVulnerable(Caster<?> otherSource, Spell other) {
        return suppressionCounter <= otherSource.getCurrentLevel();
    }

    @Override
    public void onSuppressed(Caster<?> otherSource) {
        suppressionCounter = 100;
        setDirty(true);
    }

    @Override
    public boolean isSuppressed() {
        return suppressionCounter > 0;
    }

    public Entity getDisguise() {
        return entity;
    }

    public DisguiseSpell setDisguise(@Nullable Entity entity) {
        if (entity == this.entity) {
            entity = null;
        }
        this.entityNbt = null;
        this.entityId = "";

        removeDisguise();

        if (entity != null) {
            entityNbt = encodeEntityToNBT(entity);
            entityId = entityNbt.getString("id");
        }

        setDirty(true);

        return this;
    }

    protected void removeDisguise() {
        if (entity != null) {
            entity.remove();
            entity = null;
        }
    }

    protected CompoundTag encodeEntityToNBT(Entity entity) {
        CompoundTag entityNbt = new CompoundTag();

        if (entity instanceof PlayerEntity) {
            GameProfile profile = ((PlayerEntity)entity).getGameProfile();

            entityNbt.putString("id", "player");
            entityNbt.putUuid("playerId", profile.getId());
            entityNbt.putString("playerName", profile.getName());

            CompoundTag tag = new CompoundTag();

            entity.saveToTag(tag);

            entityNbt.put("playerNbt", tag);
        } else {
            entity.saveToTag(entityNbt);
        }

        return entityNbt;
    }

    protected synchronized void createPlayer(CompoundTag nbt, GameProfile profile, Caster<?> source) {
        removeDisguise();

        entity = InteractionManager.instance().createPlayer(source.getEntity(), profile);
        entity.setCustomName(source.getOwner().getName());
        ((PlayerEntity)entity).fromTag(nbt.getCompound("playerNbt"));
        entity.setUuid(UUID.randomUUID());
        entity.extinguish();

        onEntityLoaded(source);
    }

    @SuppressWarnings("unchecked")
    protected void checkAndCreateDisguiseEntity(Caster<?> source) {
        if (entity == null && entityNbt != null) {
            CompoundTag nbt = entityNbt;
            entityNbt = null;

            if ("player".equals(entityId)) {
                createPlayer(nbt, new GameProfile(
                        nbt.getUuid("playerId"),
                        nbt.getString("playerName")
                    ), source);
                new Thread(() -> createPlayer(nbt, SkullBlockEntity.loadProperties(new GameProfile(
                    null,
                    nbt.getString("playerName")
                )), source)).start();
            } else {
                if (source.isClient()) {
                    entity = EntityType.fromTag(nbt).map(type -> type.create(source.getWorld())).orElse(null);
                    EntityBehaviour.forEntity(entity).ifPresent(behaviour -> {
                        ((EntityBehaviour<Entity>)behaviour).onCreate(entity);
                    });
                } else {
                    entity = EntityType.loadEntityWithPassengers(nbt, source.getWorld(), e -> {
                        EntityBehaviour.forEntity(e).ifPresent(behaviour -> {
                           ((EntityBehaviour<Entity>)behaviour).onCreate(e);
                        });

                        return e;
                    });
                }
            }

            onEntityLoaded(source);
        }
    }

    protected void onEntityLoaded(Caster<?> source) {
        source.getEntity().calculateDimensions();

        if (entity == null) {
            return;
        }

        CasterUtils.toCaster(entity).ifPresent(c -> c.setSpell(null));

        if (source.isClient()) {
            source.getWorld().spawnEntity(entity);
        }
    }

    @Override
    public boolean handleProjectileImpact(ProjectileEntity projectile) {
        return getDisguise() == projectile;
    }

    protected void copyBaseAttributes(LivingEntity from, Entity to) {

        // Set first because position calculations rely on it
        to.age = from.age;
        to.removed = from.removed;
        to.setOnGround(from.isOnGround());

        if (isAttachedEntity(entity)) {

            double x = Math.floor(from.getX()) + 0.5;
            double y = Math.floor(from.getY());
            double z = Math.floor(from.getZ()) + 0.5;

            to.prevX = x;
            to.prevY = y;
            to.prevZ = z;

            to.lastRenderX = x;
            to.lastRenderY = y;
            to.lastRenderZ = z;

            to.updatePosition(x, y, z);

            if (entity instanceof FallingBlockEntity) {
                ((FallingBlockEntity)entity).setFallingBlockPos(from.getBlockPos());
            }
        } else {
            to.copyPositionAndRotation(from);

            to.prevX = from.prevX;
            to.prevY = from.prevY;
            to.prevZ = from.prevZ;

            to.chunkX = from.chunkX;
            to.chunkY = from.chunkY;
            to.chunkZ = from.chunkZ;

            to.lastRenderX = from.lastRenderX;
            to.lastRenderY = from.lastRenderY;
            to.lastRenderZ = from.lastRenderZ;
        }

        if (to instanceof PlayerEntity) {
            PlayerEntity l = (PlayerEntity)to;

            l.capeX = l.getX();
            l.capeY = l.getY();
            l.capeZ = l.getZ();
        }

        to.setVelocity(from.getVelocity());

        to.pitch = from.pitch;
        to.prevPitch = from.prevPitch;
        to.yaw = from.yaw;
        to.prevYaw = from.prevYaw;
        to.horizontalSpeed = from.horizontalSpeed;
        to.prevHorizontalSpeed = from.prevHorizontalSpeed;

        to.distanceTraveled = from.distanceTraveled;

        if (to instanceof LivingEntity) {
            LivingEntity l = (LivingEntity)to;

            l.headYaw = from.headYaw;
            l.prevHeadYaw = from.prevHeadYaw;
            l.bodyYaw = from.bodyYaw;
            l.prevBodyYaw = from.prevBodyYaw;

            l.limbDistance = from.limbDistance;
            l.limbAngle = from.limbAngle;
            l.lastLimbDistance = from.lastLimbDistance;

            l.handSwingProgress = from.handSwingProgress;
            l.lastHandSwingProgress = from.lastHandSwingProgress;
            l.handSwingTicks = from.handSwingTicks;
            l.handSwinging = from.handSwinging;

            l.hurtTime = from.hurtTime;
            l.deathTime = from.deathTime;
            l.stuckStingerTimer = from.stuckStingerTimer;
            l.stuckArrowTimer = from.stuckArrowTimer;
            l.setHealth(from.getHealth());

            for (EquipmentSlot i : EquipmentSlot.values()) {
                ItemStack neu = from.getEquippedStack(i);
                ItemStack old = l.getEquippedStack(i);
                if (old != neu) {
                    l.equipStack(i, neu);
                }
            }
        }

        /*if (to instanceof RangedAttackMob) {
            ItemStack activeItem = from.getActiveItem();

            ((RangedAttackMob)to).setSwingingArms(!activeItem.isEmpty() && activeItem.getUseAction() == UseAction.BOW);
        }*/

        if (to instanceof TameableEntity) {
            ((TameableEntity)to).setSitting(from.isSneaking());
        }

        if (from.age < 100 || from instanceof PlayerEntity && ((PlayerEntity)from).isCreative()) {
            to.extinguish();
        }

        if (to.isOnFire()) {
            from.setOnFireFor(1);
        } else {
            from.extinguish();
        }

        to.setSneaking(from.isSneaking());
    }

    @Override
    public boolean updateOnPerson(Caster<?> caster) {
        return update(caster);
    }

    @Override
    public boolean update(Caster<?> source) {
        return update(source, true);
    }

    @SuppressWarnings("unchecked")
    public boolean update(Caster<?> source, boolean tick) {
        LivingEntity owner = source.getOwner();

        if (isSuppressed()) {
            suppressionCounter--;

            owner.setInvisible(false);
            if (source instanceof Pony) {
                ((Pony)source).setInvisible(false);
            }

            if (entity != null) {
                entity.setInvisible(true);
                entity.setPos(entity.getX(), Integer.MIN_VALUE, entity.getY());
            }

            return true;
        }

        checkAndCreateDisguiseEntity(source);

        if (owner == null) {
            return true;
        }

        if (entity == null) {
            if (source instanceof Pony) {
                owner.setInvisible(false);
                ((Pony) source).setInvisible(false);
            }

            return false;
        }

        entity.noClip = true;

        if (entity instanceof MobEntity) {
            ((MobEntity)entity).setAiDisabled(true);
        }

        entity.setInvisible(false);
        entity.setNoGravity(true);

        copyBaseAttributes(owner, entity);

        if (tick && !skipsUpdate(entity)) {
            entity.tick();
        }

        EntityBehaviour.forEntity(entity).ifPresent(b -> {
            ((EntityBehaviour<Entity>)b).update(source, entity);
        });

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            player.setInvisible(true);
            source.getOwner().setInvisible(true);

            if (entity instanceof Owned) {
                ((Owned<LivingEntity>)entity).setOwner(player.getOwner());
            }

            if (entity instanceof PlayerEntity) {
                entity.getDataTracker().set(PlayerAccess.getModelBitFlag(), owner.getDataTracker().get(PlayerAccess.getModelBitFlag()));
            }

            if (player.isClientPlayer() && InteractionManager.instance().getViewMode() == 0) {
                entity.setInvisible(true);
                entity.setPos(entity.getX(), Integer.MIN_VALUE, entity.getY());
            }

            return player.getSpecies() == Race.CHANGELING;
        }

        return !source.getOwner().removed;
    }

    @Override
    public void setDead() {
        super.setDead();
        removeDisguise();
    }

    @Override
    public void render(Caster<?> source) {
        if (isSuppressed()) {
            source.spawnParticles(MagicParticleEffect.UNICORN, 5);
            source.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
        } else if (source.getWorld().random.nextInt(30) == 0) {
            source.spawnParticles(UParticles.CHANGELING_MAGIC, 2);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        compound.putInt("suppressionCounter", suppressionCounter);
        compound.putString("entityId", entityId);

        if (entityNbt != null) {
            compound.put("entity", entityNbt);
        } else if (entity != null) {
            compound.put("entity", encodeEntityToNBT(entity));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        suppressionCounter = compound.getInt("suppressionCounter");

        String newId = compound.getString("entityId");

        if (!newId.contentEquals(entityId)) {
            entityNbt = null;
            removeDisguise();
        }

        if (compound.contains("entity")) {
            entityId = newId;

            entityNbt = compound.getCompound("entity");

            compound.getString("entityData");

            if (entity != null) {
                try {
                    entity.fromTag(entityNbt);
                } catch (Exception ignored) {
                    // Mojang pls
                }
            }
        }
    }

    @Override
    public boolean checkCanFly(Pony player) {
        if (entity == null || !player.getSpecies().canFly()) {
            return false;
        }

        if (entity instanceof Owned) {
            @SuppressWarnings("unchecked")
            Pony iplayer = Pony.of(((Owned<PlayerEntity>)entity).getOwner());

            return iplayer != null && iplayer.getSpecies().canFly();
        }

        return entity instanceof FlyingEntity
                || entity instanceof AmbientEntity
                || entity instanceof EnderDragonEntity
                || entity instanceof VexEntity
                || entity instanceof ShulkerBulletEntity
                || entity instanceof Flutterer
                || ProjectileUtil.isProjectile(entity);
    }

    @Override
    public float getTargetEyeHeight(Pony player) {
        if (entity != null && !isSuppressed()) {
            if (entity instanceof FallingBlockEntity) {
                return 0.5F;
            }
            return entity.getStandingEyeHeight();
        }
        return -1;
    }

    @Override
    public float getTargetBodyHeight(Pony player) {
        if (entity != null && !isSuppressed()) {
            if (entity instanceof FallingBlockEntity) {
                return 0.9F;
            }
            return entity.getHeight() - 0.1F;
        }
        return -1;
    }

    public static boolean skipsUpdate(Entity entity) {
        return entity instanceof FallingBlockEntity
            || entity instanceof AbstractDecorationEntity
            || entity instanceof PlayerEntity;
    }

    public static boolean isAttachedEntity(Entity entity) {
        return entity instanceof ShulkerEntity
            || entity instanceof AbstractDecorationEntity
            || entity instanceof FallingBlockEntity;
    }

    static abstract class PlayerAccess extends PlayerEntity {
        public PlayerAccess() { super(null, null, 0, null); }
        static TrackedData<Byte> getModelBitFlag() {
            return PLAYER_MODEL_PARTS;
        }
    }
}
