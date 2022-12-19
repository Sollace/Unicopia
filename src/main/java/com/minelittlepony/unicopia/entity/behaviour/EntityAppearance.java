package com.minelittlepony.unicopia.entity.behaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.minelittlepony.unicopia.entity.UEntityAttributes;
import com.minelittlepony.unicopia.entity.collision.EntityCollisions;
import com.minelittlepony.unicopia.entity.player.PlayerDimensions;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.ProjectileUtil;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.shape.VoxelShape;

public class EntityAppearance implements NbtSerialisable, PlayerDimensions.Provider, FlightType.Provider, EntityCollisions.ComplexCollidable {
    private static final Optional<Float> BLOCK_HEIGHT = Optional.of(0.5F);

    @NotNull
    private String entityId = "";

    @Nullable
    private Entity entity;

    @Nullable
    private BlockEntity blockEntity;

    private List<Entity> attachments = new ArrayList<>();

    private Optional<EntityDimensions> dimensions = Optional.empty();

    /**
     * Tag that allows behaviours to store data between ticks.
     * This is not serialized, so should only be used for server-side data.
     */
    @Nullable
    private NbtCompound tag;

    @Nullable
    private NbtCompound entityNbt;

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

    public NbtCompound getOrCreateTag() {
        if (tag == null) {
            tag = new NbtCompound();
        }
        return tag;
    }

    public boolean hasTag() {
        return tag != null;
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

    private synchronized void createPlayer(NbtCompound nbt, GameProfile profile, Caster<?> source) {
        remove();

        entity = InteractionManager.instance().createPlayer(source.asEntity(), profile);
        entity.setCustomName(source.getMaster().getName());
        ((PlayerEntity)entity).readNbt(nbt.getCompound("playerNbt"));
        if (nbt.contains("playerVisibleParts", NbtElement.BYTE_TYPE)) {
            entity.getDataTracker().set(Disguise.PlayerAccess.getModelBitFlag(), nbt.getByte("playerVisibleParts"));
        }
        entity.setUuid(UUID.randomUUID());
        entity.extinguish();

        onEntityLoaded(source);
    }

    public Entity getOrCreate(Caster<?> source) {
        if (entity == null && entityNbt != null) {
            NbtCompound nbt = entityNbt;
            entity = null;
            entityNbt = null;
            attachments.clear();

            if ("player".equals(entityId)) {
                createPlayer(nbt, new GameProfile(
                        nbt.containsUuid("playerId") ? nbt.getUuid("playerId") : UUID.randomUUID(),
                                nbt.getString("playerName")
                            ), source);

                SkullBlockEntity.loadProperties(new GameProfile(
                        nbt.containsUuid("playerId") ? nbt.getUuid("playerId") : null,
                                nbt.getString("playerName")
                            ), p -> createPlayer(nbt, p, source));
            } else {
                if (source.isClient()) {
                    entity = EntityType.fromNbt(nbt).map(type -> type.create(source.asWorld())).orElse(null);
                    if (entity != null) {
                        try {
                            entity.readNbt(nbt);
                        } catch (Exception ignored) {
                            // Mojang pls
                        }
                        entity = EntityBehaviour.forEntity(entity).onCreate(entity, this, true);
                    }
                } else {
                    entity = EntityType.loadEntityWithPassengers(nbt, source.asWorld(), e -> {
                        return EntityBehaviour.forEntity(e).onCreate(e, this, true);
                    });
                }

                onEntityLoaded(source);
            }
        }

        return entity;
    }

    public void onImpact(Caster<?> pony, float distance, float damageMultiplier, DamageSource cause) {
        EntityBehaviour.forEntity(entity).onImpact(pony, entity, distance, damageMultiplier, cause);
    }

    private void onEntityLoaded(Caster<?> source) {
        source.asEntity().calculateDimensions();

        if (entity == null) {
            return;
        }

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).getAttributeInstance(UEntityAttributes.ENTITY_GRAVTY_MODIFIER).clearModifiers();
        }

        if (source.isClient()) {
            source.asWorld().spawnEntity(entity);
        }
    }

    @Override
    public FlightType getFlightType() {
        if (!isPresent()) {
            return FlightType.UNSET;
        }

        if (entity == null) {
            return FlightType.NONE;
        }

        if (entity instanceof Owned) {
            @SuppressWarnings("unchecked")
            Pony iplayer = Pony.of(((Owned<PlayerEntity>)entity).getMaster());

            return iplayer == null ? FlightType.NONE : iplayer.getSpecies().getFlightType();
        }

        if (entity instanceof FlyingEntity
                || entity instanceof AmbientEntity
                || entity instanceof EnderDragonEntity
                || entity instanceof VexEntity
                || entity instanceof AllayEntity
                || entity instanceof BatEntity
                || entity instanceof ButterflyEntity
                || entity instanceof ShulkerBulletEntity
                || entity instanceof Flutterer
                || ProjectileUtil.isFlyingProjectile(entity)) {
            return FlightType.INSECTOID;
        }

        return FlightType.NONE;
    }

    @Override
    public Optional<Float> getTargetEyeHeight(Pony player) {
        if (entity != null) {
            if (entity instanceof FallingBlockEntity) {
                return BLOCK_HEIGHT;
            }
            return Optional.of(entity.getStandingEyeHeight());
        }
        return Optional.empty();
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

    public Optional<Double> getDistance(Pony player) {
        return EntityBehaviour.forEntity(entity).getCameraDistance(entity, player);
    }

    @Override
    public Optional<EntityDimensions> getTargetDimensions(Pony player) {
        return dimensions = EntityBehaviour.forEntity(entity).getDimensions(entity, dimensions);
    }

    public boolean skipsUpdate() {
        return entity instanceof FallingBlockEntity
            || entity instanceof AbstractDecorationEntity
            || entity instanceof PlayerEntity
            || entity instanceof AbstractDecorationEntity;
    }

    public boolean isAxisAligned() {
        return isAxisAligned(entity);
    }

    public boolean canClimbWalls() {
        return entity instanceof SpiderEntity;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putString("entityId", entityId);

        if (entityNbt != null) {
            compound.put("entity", entityNbt);
        } else if (entity != null) {
            compound.put("entity", encodeEntityToNBT(entity));
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        String newId = compound.getString("entityId");

        String newPlayerName = null;
        if (compound.contains("entity", NbtElement.COMPOUND_TYPE) && compound.getCompound("entity").contains("playerName", NbtElement.STRING_TYPE)) {
            newPlayerName = compound.getCompound("entity").getString("playerName");
        }

        String oldPlayerName = entity != null && entity instanceof PlayerEntity ? ((PlayerEntity)entity).getGameProfile().getName() : null;

        if (!Objects.equals(newId, entityId) || !Objects.equals(newPlayerName, oldPlayerName)) {
            entityNbt = null;
            remove();
        }

        if (compound.contains("entity", NbtElement.COMPOUND_TYPE)) {
            entityId = newId;

            entityNbt = compound.getCompound("entity");

            if (entity != null) {
                try {
                    entity.readNbt(entityNbt);
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

    private static NbtCompound encodeEntityToNBT(Entity entity) {
        NbtCompound entityNbt = new NbtCompound();

        if (entity instanceof PlayerEntity player) {
            GameProfile profile = player.getGameProfile();

            entityNbt.putString("id", "player");
            if (profile.getId() != null) {
                entityNbt.putUuid("playerId", profile.getId());
            }
            entityNbt.putString("playerName", profile.getName());
            entityNbt.putByte("playerVisibleParts", player.getDataTracker().get(Disguise.PlayerAccess.getModelBitFlag()));

            return NbtSerialisable.subTag("playerNbt", entityNbt, playerNbt -> {
                player.writeNbt(playerNbt);
                playerNbt.remove("unicopia_caster");
                Pony pony = Pony.of(player);
                if (pony != null) {
                    NbtSerialisable.subTag("unicopia_caster", playerNbt, pony::toSyncronisedNbt);
                }
            });
        }

        entity.saveSelfNbt(entityNbt);
        entityNbt.remove("unicopia_caster");

        return entityNbt;
    }

    @Override
    public void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output) {
        EntityCollisions.getCollissionShapes(getAppearance(), context, output);
        getAttachments().forEach(e -> EntityCollisions.getCollissionShapes(e, context, output));
    }

}
