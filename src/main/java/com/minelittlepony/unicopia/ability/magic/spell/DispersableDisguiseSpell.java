package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.IllusionarySpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

/**
 * Shapeshifts the player.
 * <p>
 * Internal. Used by the changeling ability.
 */
public class DispersableDisguiseSpell extends AbstractDisguiseSpell implements IllusionarySpell {

    private final DataTracker.Entry<Boolean> suppressed = dataTracker.startTracking(TrackableDataType.BOOLEAN, false);
    private int suppressionCounter;

    public DispersableDisguiseSpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    @Override
    public boolean isVulnerable(Caster<?> otherSource, Spell other) {
        return suppressionCounter <= otherSource.getLevel().get();
    }

    @Override
    public void onSuppressed(Caster<?> otherSource, float time) {
        time /= getTraits().getOrDefault(Trait.STRENGTH, 1);
        suppressionCounter = (int)time;
        suppressed.set(true);
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
            } else if (source.asWorld().random.nextInt(30) == 0) {
                source.spawnParticles(UParticles.CHANGELING_MAGIC, 2);
            }
        }

        if (!source.canUse(Abilities.DISGUISE)) {
            setDead();
        }

        Entity owner = source.asEntity();
        Entity appearance = getDisguise().getAppearance();

        if (isSuppressed()) {
            if (--suppressionCounter <= 0) {
                suppressed.set(false);
            }

            owner.setInvisible(false);
            if (source instanceof Pony) {
                ((Pony)source).setInvisible(false);
            }

            if (appearance != null) {
                appearance.setInvisible(true);
                appearance.setPos(appearance.getX(), Integer.MIN_VALUE, appearance.getY());
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
        if (suppressionCounter > 0) {
            suppressed.set(true);
        }
    }

    @Override
    public Optional<EntityAppearance> getAppearance() {
        return isSuppressed() ? Optional.empty() : super.getAppearance();
    }
}
