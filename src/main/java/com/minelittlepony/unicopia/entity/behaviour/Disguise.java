package com.minelittlepony.unicopia.entity.behaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.CasterUtils;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.CompoundTag;

public class Disguise implements NbtSerialisable {

    @Nonnull
    private String entityId = "";

    @Nullable
    private Entity entity;

    @Nullable
    private BlockEntity blockEntity;

    private List<Entity> attachments = new ArrayList<>();

    private Optional<EntityDimensions> dimensions = Optional.empty();

    @Nullable
    private CompoundTag entityNbt;

    @Nullable
    public Entity getAppearance() {
        return entity;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public List<Entity> getAttachments() {
        return attachments;
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void attachExtraEntity(Entity entity) {
        attachments.add(entity);
    }

    public void setAppearance(@Nullable Entity entity) {
        remove();

        entityNbt = entity == null ? null : encodeEntityToNBT(entity);
        entityId = entityNbt == null ? "" : entityNbt.getString("id");
    }

    public boolean isPresent() {
        return entity != null;
    }

    public void remove() {
        attachments.clear();
        if (entity != null) {
            EntityBehaviour.forEntity(entity).onDestroy(entity);
            entity = null;
        }
        if (blockEntity != null) {
            blockEntity.markRemoved();
            blockEntity = null;
        }
    }

    private synchronized void createPlayer(CompoundTag nbt, GameProfile profile, Caster<?> source) {
        remove();

        entity = InteractionManager.instance().createPlayer(source.getEntity(), profile);
        entity.setCustomName(source.getOwner().getName());
        ((PlayerEntity)entity).fromTag(nbt.getCompound("playerNbt"));
        entity.setUuid(UUID.randomUUID());
        entity.extinguish();

        onEntityLoaded(source);
    }

    public Entity getOrCreate(Caster<?> source) {
        if (entity == null && entityNbt != null) {
            CompoundTag nbt = entityNbt;
            entityNbt = null;
            attachments.clear();

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
                    if (entity != null) {
                        try {
                            entity.fromTag(nbt);
                        } catch (Exception ignored) {
                            // Mojang pls
                        }
                        entity = EntityBehaviour.forEntity(entity).onCreate(entity, this, true);
                    }
                } else {
                    entity = EntityType.loadEntityWithPassengers(nbt, source.getWorld(), e -> {
                        return EntityBehaviour.forEntity(e).onCreate(e, this, true);
                    });
                }
            }

            onEntityLoaded(source);
        }

        return entity;
    }

    private void onEntityLoaded(Caster<?> source) {
        source.getEntity().calculateDimensions();

        if (entity == null) {
            return;
        }

        CasterUtils.toCaster(entity).ifPresent(c -> c.setSpell(null));

        if (source.isClient()) {
            source.getWorld().spawnEntity(entity);
        }
    }

    public boolean canFly() {
        if (entity == null) {
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

    public float getStandingEyeHeight() {
        if (entity != null) {
            if (entity instanceof FallingBlockEntity) {
                return 0.5F;
            }
            return entity.getStandingEyeHeight();
        }
        return -1;
    }

    public float getHeight() {
        if (entity != null) {
            if (entity instanceof FallingBlockEntity) {
                return 0.9F;
            }
            return entity.getHeight() - 0.1F;
        }
        return -1;
    }

    public Optional<EntityDimensions> getDimensions() {
        return dimensions = EntityBehaviour.forEntity(entity).getDimensions(entity, dimensions);
    }

    public boolean skipsUpdate() {
        return entity instanceof FallingBlockEntity
            || entity instanceof AbstractDecorationEntity
            || entity instanceof PlayerEntity;
    }

    public boolean isAxisAligned() {
        return isAxisAligned(entity);
    }

    public boolean canClimbWalls() {
        return entity instanceof SpiderEntity;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putString("entityId", entityId);

        if (entityNbt != null) {
            compound.put("entity", entityNbt);
        } else if (entity != null) {
            compound.put("entity", encodeEntityToNBT(entity));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        String newId = compound.getString("entityId");

        if (!newId.contentEquals(entityId)) {
            entityNbt = null;
            remove();
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

                attachments.clear();
                entity = EntityBehaviour.forEntity(entity).onCreate(entity, this, false);
            }
        }
    }

    public static boolean isAxisAligned(@Nullable Entity entity) {
        return entity instanceof ShulkerEntity
            || entity instanceof AbstractDecorationEntity
            || entity instanceof FallingBlockEntity;
    }

    private static CompoundTag encodeEntityToNBT(Entity entity) {
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
}
