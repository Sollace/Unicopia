package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.EntityReference.EntityValues;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgCasterLookRequest;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A spell that can be attached to a specific location in the world.
 * <p>
 * The spell's effects are still powered by the casting player, so if the player dies or leaves the area, their
 * spell loses affect until they return.
 * <p>
 * When cast two copies of this spell are created. One is attached to the player and is the controlling spell,
 * the other is attached to a cast spell entity and placed in the world.
 *
 * TODO: Split this up into separate classes.
 */
public class PlaceableSpell extends AbstractDelegatingSpell implements OrientedSpell {
    /**
     * Dimension the spell was originally cast in
     */
    @Nullable
    private RegistryKey<World> dimension;

    /**
     * ID of the placed counterpart of this spell.
     */
    @Nullable
    private UUID placedSpellId;

    /**
     * The cast spell entity
     */
    private final EntityReference<CastSpellEntity> castEntity = new EntityReference<>();

    public float pitch;
    public float yaw;

    private int prevAge;
    private int age;

    private boolean dead;
    private int prevDeathTicks;
    private int deathTicks;

    private Optional<Vec3d> position = Optional.empty();

    public PlaceableSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    public PlaceableSpell setSpell(Spell spell) {
        delegate.set(spell);
        return this;
    }

    public float getAge(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevAge, age);
    }

    public float getScale(float tickDelta) {
        float add = MathHelper.clamp(getAge(tickDelta) / 25F, 0, 1);
        float subtract = dead ? 1 - (MathHelper.lerp(tickDelta, prevDeathTicks, deathTicks) / 20F) : 0;
        return MathHelper.clamp(add - subtract, 0, 1);
    }

    @Override
    public boolean isDying() {
        return dead && deathTicks > 0;
    }

    @Override
    public void setDead() {
        super.setDead();
        dead = true;
        deathTicks = 20;
    }

    @Override
    public boolean isDead() {
        return dead && deathTicks <= 0 && super.isDead();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        System.out.println("Placed Tick: " + source + " " + source.isClient() + " " + situation);
        if (situation == Situation.BODY) {
            if (!source.isClient()) {
                if (dimension == null) {
                    dimension = source.asWorld().getRegistryKey();
                    if (source instanceof Pony) {
                        Channel.SERVER_REQUEST_PLAYER_LOOK.sendToPlayer(new MsgCasterLookRequest(getUuid()), (ServerPlayerEntity)source.asEntity());
                    }
                    setDirty();
                }

                castEntity.getTarget().ifPresentOrElse(
                        target -> checkDetachment(source, target),
                        () -> spawnPlacedEntity(source)
                );
            }

            return !isDead();
        }

        if (situation == Situation.GROUND_ENTITY) {
            if (!source.isClient()) {
                if (Ether.get(source.asWorld()).get(this, source) == null) {
                    setDead();
                    return false;
                }
            }

            prevAge = age;
            if (age < 25) {
                age++;
            }

            return super.tick(source, Situation.GROUND);
        }

        return !isDead();
    }

    @Override
    public void tickDying(Caster<?> caster) {
        prevDeathTicks = deathTicks;
        deathTicks--;
    }

    private void checkDetachment(Caster<?> source, EntityValues<?> target) {
        if (getWorld(source).map(Ether::get).map(ether -> ether.get(getTypeAndTraits().type(), target, placedSpellId)).isEmpty()) {
            setDead();
        }
    }

    private void spawnPlacedEntity(Caster<?> source) {
        CastSpellEntity entity = UEntities.CAST_SPELL.create(source.asWorld());
        Vec3d pos = getPosition().orElse(position.orElse(source.asEntity().getPos()));
        entity.updatePositionAndAngles(pos.x, pos.y, pos.z, source.asEntity().getYaw(), source.asEntity().getPitch());
        PlaceableSpell copy = delegate.get().toPlaceable();
        if (delegate.get() instanceof PlacementDelegate delegate) {
            delegate.onPlaced(source, copy, entity);
        }
        entity.getSpellSlot().put(copy);
        entity.setCaster(source);
        entity.getWorld().spawnEntity(entity);
        placedSpellId = copy.getUuid();
        Ether.get(entity.getWorld()).getOrCreate(copy, entity);

        castEntity.set(entity);
        setDirty();
    }

    @Override
    public void setOrientation(float pitch, float yaw) {
        this.pitch = -90 - pitch;
        this.yaw = -yaw;
        if (delegate.get() instanceof OrientedSpell o) {
            o.setOrientation(pitch, yaw);
        }
        setDirty();
    }

    public void setPosition(Caster<?> source, Vec3d position) {
        this.position = Optional.of(position);
        this.dimension = source.asWorld().getRegistryKey();
        castEntity.ifPresent(source.asWorld(), entity -> {
            entity.updatePositionAndAngles(position.x, position.y, position.z, entity.getYaw(), entity.getPitch());
        });
        if (delegate.get() instanceof PlaceableSpell o) {
            o.setPosition(source, position);
        }
        setDirty();
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        if (!source.isClient()) {
            castEntity.getTarget().ifPresent(target -> {
                getWorld(source).map(Ether::get)
                    .ifPresent(ether -> ether.remove(getTypeAndTraits().type(), target.uuid()));
            });
            castEntity.set(null);
            getSpellEntity(source).ifPresent(e -> {
                castEntity.set(null);
            });

            if (source.asEntity() instanceof CastSpellEntity) {
                Ether.get(source.asWorld()).remove(this, source);
            }
        }
        super.onDestroyed(source);
    }

    public Optional<CastSpellEntity> getSpellEntity(Caster<?> source) {
        return getWorld(source).map(castEntity::get);
    }

    public Optional<Vec3d> getPosition() {
        return castEntity.getTarget().map(EntityValues::pos);
    }

    protected Optional<World> getWorld(Caster<?> source) {
        return Optional.ofNullable(dimension)
                .map(dim -> source.asWorld().getServer().getWorld(dim));
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("dead", dead);
        compound.putInt("deathTicks", deathTicks);
        compound.putInt("age", age);
        compound.putFloat("pitch", pitch);
        compound.putFloat("yaw", yaw);
        position.ifPresent(pos -> {
            compound.put("position", NbtSerialisable.writeVector(pos));
        });
        if (placedSpellId != null) {
            compound.putUuid("placedSpellId", placedSpellId);
        }
        if (dimension != null) {
            compound.putString("dimension", dimension.getValue().toString());
        }
        compound.put("castEntity", castEntity.toNBT());

    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        dead = compound.getBoolean("dead");
        deathTicks = compound.getInt("deathTicks");
        age = compound.getInt("age");
        pitch = compound.getFloat("pitch");
        yaw = compound.getFloat("yaw");
        position = compound.contains("position") ? Optional.of(NbtSerialisable.readVector(compound.getList("position", NbtElement.FLOAT_TYPE))) : Optional.empty();
        placedSpellId = compound.containsUuid("placedSpellId") ? compound.getUuid("placedSpellId") : null;
        if (compound.contains("dimension", NbtElement.STRING_TYPE)) {
            Identifier id = Identifier.tryParse(compound.getString("dimension"));
            if (id != null) {
                dimension = RegistryKey.of(RegistryKeys.WORLD, id);
            }
        }
        if (compound.contains("castEntity")) {
            castEntity.fromNBT(compound.getCompound("castEntity"));
        }
    }

    @Override
    public PlaceableSpell toPlaceable() {
        return this;
    }

    public interface PlacementDelegate {
        void onPlaced(Caster<?> source, PlaceableSpell parent, CastSpellEntity entity);
    }
}
