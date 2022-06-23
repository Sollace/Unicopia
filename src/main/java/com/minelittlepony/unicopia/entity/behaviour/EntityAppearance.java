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
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.player.PlayerAttributes;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.WorldAccess;

public class EntityAppearance implements NbtSerialisable, PlayerDimensions.Provider, FlightType.Provider {
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

        entity = InteractionManager.instance().createPlayer(source.getEntity(), profile);
        entity.setCustomName(source.getMaster().getName());
        ((PlayerEntity)entity).readNbt(nbt.getCompound("playerNbt"));
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
                    entity = EntityType.fromNbt(nbt).map(type -> type.create(source.getReferenceWorld())).orElse(null);
                    if (entity != null) {
                        try {
                            entity.readNbt(nbt);
                        } catch (Exception ignored) {
                            // Mojang pls
                        }
                        entity = EntityBehaviour.forEntity(entity).onCreate(entity, this, true);
                    }
                } else {
                    entity = EntityType.loadEntityWithPassengers(nbt, source.getReferenceWorld(), e -> {
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
        source.getEntity().calculateDimensions();

        if (entity == null) {
            return;
        }

        Caster.of(entity).ifPresent(c -> c.getSpellSlot().clear());

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).getAttributeInstance(PlayerAttributes.ENTITY_GRAVTY_MODIFIER).clearModifiers();
        }

        if (source.isClient()) {
            source.getReferenceWorld().spawnEntity(entity);
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
        if (compound.contains("entity") && compound.getCompound("entity").contains("playerName")) {
            newPlayerName = compound.getCompound("entity").getString("playerName");
        }

        String oldPlayerName = entity != null && entity instanceof PlayerEntity ? ((PlayerEntity)entity).getGameProfile().getName() : null;

        if (!Objects.equals(newId, entityId) || !Objects.equals(newPlayerName, oldPlayerName)) {
            entityNbt = null;
            remove();
        }

        if (compound.contains("entity")) {
            entityId = newId;

            entityNbt = compound.getCompound("entity");

            compound.getString("entityData");

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

        if (entity instanceof PlayerEntity) {
            GameProfile profile = ((PlayerEntity)entity).getGameProfile();

            entityNbt.putString("id", "player");
            if (profile.getId() != null) {
                entityNbt.putUuid("playerId", profile.getId());
            }
            entityNbt.putString("playerName", profile.getName());

            NbtCompound tag = new NbtCompound();

            entity.writeNbt(tag);

            entityNbt.put("playerNbt", tag);
        } else {
            entity.saveSelfNbt(entityNbt);
        }

        return entityNbt;
    }

    void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output) {
        getCollissionShapes(getAppearance(), context, output);
        getAttachments().forEach(e -> getCollissionShapes(e, context, output));
    }

    private static void getCollissionShapes(@Nullable Entity entity, ShapeContext context, Consumer<VoxelShape> output) {
        if (entity == null) {
            return;
        }

        if (entity.isCollidable()) {
            output.accept(VoxelShapes.cuboid(entity.getBoundingBox()));
        } else if (entity instanceof FallingBlockEntity) {
            BlockPos pos = entity.getBlockPos();
            output.accept(((FallingBlockEntity) entity).getBlockState()
                    .getCollisionShape(entity.world, entity.getBlockPos(), context)
                    .offset(pos.getX(), pos.getY(), pos.getZ())
            );
        }
    }

    public static List<VoxelShape> getColissonShapes(@Nullable Entity entity, WorldAccess world, Box box) {
        List<VoxelShape> shapes = new ArrayList<>();
        ShapeContext ctx = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
        VoxelShape entityShape = VoxelShapes.cuboid(box.expand(1.0E-6D));

        world.getOtherEntities(entity, box.expand(0.5), e -> {
            Caster.of(e).flatMap(c -> c.getSpellSlot().get(SpellPredicate.IS_DISGUISE, false)).ifPresent(p -> {
                p.getDisguise().getCollissionShapes(ctx, shape -> {
                    if (!shape.isEmpty() && VoxelShapes.matchesAnywhere(shape, entityShape, BooleanBiFunction.AND)) {
                        shapes.add(shape);
                    }
                });
            });
            return false;
        });

        return shapes;
    }
}
