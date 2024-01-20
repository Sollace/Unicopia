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
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A spell that can be attached to a specific location in the world.
 * <p>
 * The spell's effects are still powered by the casting player, so if the player dies or leaves the area, their
 * spell loses affect until they return.
 */
public class PlaceableSpell extends AbstractDelegatingSpell implements OrientedSpell {
    /**
     * Dimension the spell was originally cast in
     */
    @Nullable
    private RegistryKey<World> dimension;

    /**
     * The visual effect
     */
    private final ParticleHandle particlEffect = new ParticleHandle();

    /**
     * ID of the placed counterpart of this spell.
     */
    @Nullable
    private UUID placedSpellId;

    /**
     * The cast spell entity
     */
    private final EntityReference<CastSpellEntity> castEntity = new EntityReference<>();

    /**
     * The spell being cast
     */
    private Spell spell;

    public float pitch;
    public float yaw;

    private Optional<Vec3d> position = Optional.empty();

    public PlaceableSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    public PlaceableSpell setSpell(Spell spell) {
        this.spell = spell;
        return this;
    }

    @Override
    public Collection<Spell> getDelegates() {
        return List.of(spell);
    }

    @Override
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
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

            if (spell instanceof PlacementDelegate delegate) {
                delegate.updatePlacement(source, this);
            }

            getParticleEffectAttachment(source).ifPresent(p -> {
                p.setAttribute(Attachment.ATTR_COLOR, spell.getType().getColor());
            });

            return super.tick(source, Situation.GROUND);
        }

        return !isDead();
    }

    private void checkDetachment(Caster<?> source, EntityValues<?> target) {
        if (getWorld(source).map(Ether::get).map(ether -> ether.get(getType(), target, placedSpellId)).isEmpty()) {
            setDead();
        }
    }

    private void spawnPlacedEntity(Caster<?> source) {
        CastSpellEntity entity = UEntities.CAST_SPELL.create(source.asWorld());
        Vec3d pos = getPosition().orElse(position.orElse(source.getOriginVector()));
        entity.updatePositionAndAngles(pos.x, pos.y, pos.z, source.asEntity().getYaw(), source.asEntity().getPitch());
        PlaceableSpell copy = spell.toPlaceable();
        if (spell instanceof PlacementDelegate delegate) {
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
        getDelegates(spell -> spell instanceof OrientedSpell o ? o : null)
            .forEach(spell -> spell.setOrientation(pitch, yaw));
        setDirty();
    }

    public void setPosition(Caster<?> source, Vec3d position) {
        this.position = Optional.of(position);
        this.dimension = source.asWorld().getRegistryKey();
        castEntity.ifPresent(source.asWorld(), entity -> {
            entity.updatePositionAndAngles(position.x, position.y, position.z, entity.getYaw(), entity.getPitch());
        });
        getDelegates(spell -> spell instanceof PlaceableSpell o ? o : null)
            .forEach(spell -> spell.setPosition(source, position));
        setDirty();
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        if (!source.isClient()) {
            castEntity.getTarget().ifPresent(target -> {
                getWorld(source).map(Ether::get)
                    .ifPresent(ether -> ether.remove(getType(), target.uuid()));
            });
            castEntity.set(null);
            getSpellEntity(source).ifPresent(e -> {
                castEntity.set(null);
            });

            if (source.asEntity() instanceof CastSpellEntity spellcast) {
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

    public Optional<Attachment> getParticleEffectAttachment(Caster<?> source) {
        return particlEffect.update(getUuid(), source, spawner -> {
            spawner.addParticle(new OrientedBillboardParticleEffect(UParticles.MAGIC_RUNES, pitch + 90, yaw), Vec3d.ZERO, Vec3d.ZERO);
        });
    }

    protected Optional<World> getWorld(Caster<?> source) {
        return Optional.ofNullable(dimension)
                .map(dim -> source.asWorld().getServer().getWorld(dim));
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putFloat("pitch", pitch);
        compound.putFloat("yaw", yaw);
        position.ifPresent(pos -> {
            compound.put("position", NbtSerialisable.writeVector(pos));
        });
        if (dimension != null) {
            compound.putString("dimension", dimension.getValue().toString());
        }
        compound.put("castEntity", castEntity.toNBT());

    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        pitch = compound.getFloat("pitch");
        yaw = compound.getFloat("yaw");
        position = compound.contains("position") ? Optional.of(NbtSerialisable.readVector(compound.getList("position", NbtElement.FLOAT_TYPE))) : Optional.empty();
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
    protected void loadDelegates(NbtCompound compound) {
        spell = Spell.SERIALIZER.read(compound.getCompound("spell"));
    }

    @Override
    protected void saveDelegates(NbtCompound compound) {
        compound.put("spell", Spell.SERIALIZER.write(spell));

    }

    @Override
    public PlaceableSpell toPlaceable() {
        return this;
    }

    public interface PlacementDelegate {

        void onPlaced(Caster<?> source, PlaceableSpell parent, CastSpellEntity entity);

        void updatePlacement(Caster<?> source, PlaceableSpell parent);
    }
}
