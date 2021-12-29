package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * A spell that can be attached to a specific location in the world.
 * <p>
 * The spell's effects are still powered by the casting player, so if the player dies or leaves the area, their
 * spell loses affect until they return.
 */
public class PlaceableSpell extends AbstractDelegatingSpell {
    @Nullable
    private Identifier dimension;

    private final ParticleHandle particlEffect = new ParticleHandle();

    private final EntityReference<CastSpellEntity> castEntity = new EntityReference<>();

    private Spell spell;

    public PlaceableSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    public PlaceableSpell setSpell(Spell spell) {
        this.spell = spell;
        return this;
    }

    @Override
    protected Collection<Spell> getDelegates() {
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
                    dimension = source.getWorld().getRegistryKey().getValue();
                    setDirty();
                } else if (!source.getWorld().getRegistryKey().getValue().equals(dimension)) {
                    return false;
                }

                if (!castEntity.isPresent(source.getWorld())) {
                    CastSpellEntity entity = UEntities.CAST_SPELL.create(source.getWorld());
                    Vec3d pos = castEntity.getPosition().orElse(source.getOriginVector());
                    entity.updatePositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                    entity.getSpellSlot().put(this);
                    entity.setMaster(source.getMaster());
                    entity.world.spawnEntity(entity);

                    castEntity.set(entity);
                    setDirty();
                }
            }

            return !isDead();
        }

        if (situation == Situation.GROUND_ENTITY) {
            particlEffect.update(getUuid(), source, spawner -> {
                spawner.addParticle(new OrientedBillboardParticleEffect(UParticles.MAGIC_RUNES, 90, 0), source.getOriginVector(), Vec3d.ZERO);
            }).ifPresent(p -> {
                p.setAttribute(1, spell.getType().getColor());
            });

            return super.tick(source, Situation.GROUND);
        }

        return !isDead();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        if (dimension != null) {
            compound.putString("dimension", dimension.toString());
        }
        compound.put("castEntity", castEntity.toNBT());
        compound.put("spell", Spell.writeNbt(spell));
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        if (compound.contains("dimension")) {
            dimension = new Identifier(compound.getString("dimension"));
        }
        if (compound.contains("castEntity")) {
            castEntity.fromNBT(compound.getCompound("castEntity"));
        }
        spell = Spell.readNbt(compound.getCompound("spell"));
    }

    @Override
    public PlaceableSpell toPlaceable() {
        return this;
    }
}
