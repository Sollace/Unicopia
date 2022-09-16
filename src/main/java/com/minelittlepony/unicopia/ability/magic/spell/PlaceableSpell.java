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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * A spell that can be attached to a specific location in the world.
 * <p>
 * The spell's effects are still powered by the casting player, so if the player dies or leaves the area, their
 * spell loses affect until they return.
 */
public class PlaceableSpell extends AbstractDelegatingSpell {
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
                    dimension = source.getReferenceWorld().getRegistryKey();
                    setDirty();
                }

                if (getWorld(source).map(castEntity::isUnlinked).orElse(false)) {
                    CastSpellEntity entity = UEntities.CAST_SPELL.create(source.getReferenceWorld());
                    Vec3d pos = castEntity.getPosition().orElse(source.getOriginVector());
                    entity.updatePositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                    entity.getSpellSlot().put(spell.toPlaceable());
                    entity.setMaster(source);
                    entity.world.spawnEntity(entity);
                    Ether.get(entity.world).put(getType(), entity);

                    castEntity.set(entity);
                    setDirty();
                } else {

                    if (getWorld(source).map(Ether::get)
                        .flatMap(ether -> castEntity.getId().flatMap(id -> ether.getEntry(getType(), id)))
                        .isEmpty()) {
                        setDead();
                    }
                }
            }

            return !isDead();
        }

        if (situation == Situation.GROUND_ENTITY) {

            if (!source.isClient()) {
                if (Ether.get(source.getReferenceWorld()).getEntry(getType(), source).isEmpty()) {
                    setDead();
                    return false;
                }
            }

            particlEffect.update(getUuid(), source, spawner -> {
                spawner.addParticle(new OrientedBillboardParticleEffect(UParticles.MAGIC_RUNES, 90, 0), source.getOriginVector(), Vec3d.ZERO);
            }).ifPresent(p -> {
                p.setAttribute(Attachment.ATTR_COLOR, spell.getType().getColor());
            });

            return super.tick(source, Situation.GROUND);
        }

        return !isDead();
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
        }
        super.onDestroyed(source);
    }

    public Optional<CastSpellEntity> getSpellEntity(Caster<?> source) {
        return getWorld(source).map(castEntity::get);
    }

    protected Optional<World> getWorld(Caster<?> source) {
        return Optional.ofNullable(dimension)
                .map(dim -> source.getReferenceWorld().getServer().getWorld(dim));
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        if (dimension != null) {
            compound.putString("dimension", dimension.getValue().toString());
        }
        compound.put("castEntity", castEntity.toNBT());

    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
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
}
