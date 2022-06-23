package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.IllusionarySpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Shapeshifts the player.
 * <p>
 * Internal. Used by the changeling ability.
 */
public class DispersableDisguiseSpell extends AbstractDisguiseSpell implements IllusionarySpell {

    private int suppressionCounter;

    public DispersableDisguiseSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public boolean isVulnerable(Caster<?> otherSource, Spell other) {
        return suppressionCounter <= otherSource.getLevel().get();
    }

    @Override
    public void onSuppressed(Caster<?> otherSource, float time) {
        time /= getTraits().getOrDefault(Trait.STRENGTH, 1);
        suppressionCounter = (int)(100 * time);
        setDirty();
    }

    @Override
    public boolean isSuppressed() {
        return suppressionCounter > 0;
    }

    @Override
    public boolean update(Caster<?> source, boolean tick) {
        if (source.isClient()) {
            if (isSuppressed()) {
                source.spawnParticles(MagicParticleEffect.UNICORN, 5);
                source.spawnParticles(UParticles.CHANGELING_MAGIC, 5);
            } else if (source.getReferenceWorld().random.nextInt(30) == 0) {
                source.spawnParticles(UParticles.CHANGELING_MAGIC, 2);
            }
        }

        LivingEntity owner = source.getMaster();

        Entity entity = getDisguise().getAppearance();

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

        return super.update(source, tick);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("suppressionCounter", suppressionCounter);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        suppressionCounter = compound.getInt("suppressionCounter");
    }

    @Override
    public Optional<EntityAppearance> getAppearance() {
        return isSuppressed() ? Optional.empty() : super.getAppearance();
    }
}
