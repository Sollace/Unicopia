package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.block.data.Ether;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;

import net.minecraft.nbt.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
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
     * The cast spell entity
     */
    private final EntityReference<CastSpellEntity> castEntity = new EntityReference<>();

    /**
     * The spell being cast
     */
    private Spell spell;

    public float pitch;
    public float yaw;

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
                    setDirty();
                }

                castEntity.getId().ifPresentOrElse(
                        id -> checkDetachment(source, id),
                        () -> spawnPlacedEntity(source)
                );
            }

            return !isDead();
        }

        if (situation == Situation.GROUND_ENTITY) {
            if (!source.isClient() && Ether.get(source.asWorld()).getEntry(getType(), source).isEmpty()) {
                setDead();
                return false;
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

    private void checkDetachment(Caster<?> source, UUID id) {
        if (getWorld(source).map(Ether::get).flatMap(ether -> ether.getEntry(getType(), id)).isEmpty()) {
            setDead();
        }
    }

    private void spawnPlacedEntity(Caster<?> source) {
        CastSpellEntity entity = UEntities.CAST_SPELL.create(source.asWorld());
        Vec3d pos = castEntity.getPosition().orElse(source.getOriginVector());
        entity.updatePositionAndAngles(pos.x, pos.y, pos.z, source.asEntity().getYaw(), source.asEntity().getPitch());
        PlaceableSpell copy = spell.toPlaceable();
        if (spell instanceof PlacementDelegate delegate) {
            delegate.onPlaced(source, copy, entity);
        }
        entity.getSpellSlot().put(copy);
        entity.setCaster(source);
        entity.world.spawnEntity(entity);
        Ether.get(entity.world).put(getType(), entity);

        castEntity.set(entity);
        setDirty();
    }

    @Override
    public void setOrientation(float pitch, float yaw) {
        this.pitch = -90 - pitch;
        this.yaw = -yaw;
        getDelegates(spell -> spell instanceof OrientedSpell o ? o : null)
            .forEach(oriented -> oriented.setOrientation(pitch, yaw));
        setDirty();
    }

    @Override
    public void onDestroyed(Caster<?> source) {
        if (!source.isClient()) {
            castEntity.getId().ifPresent(id -> {
                getWorld(source).map(Ether::get)
                    .flatMap(ether -> ether.getEntry(getType(), id))
                    .ifPresent(Ether.Entry::markDead);
            });
            castEntity.set(null);
            getSpellEntity(source).ifPresent(e -> {
                castEntity.set(null);
            });

            if (source.asEntity() instanceof CastSpellEntity spellcast) {
                Ether.get(source.asWorld()).remove(getType(), source);
            }
        }
        super.onDestroyed(source);
    }

    public Optional<CastSpellEntity> getSpellEntity(Caster<?> source) {
        return getWorld(source).map(castEntity::get);
    }

    public Optional<Vec3d> getPosition() {
        return castEntity.getPosition();
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
        if (compound.contains("dimension", NbtElement.STRING_TYPE)) {
            Identifier id = Identifier.tryParse(compound.getString("dimension"));
            if (id != null) {
                dimension = RegistryKey.of(Registry.WORLD_KEY, id);
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
