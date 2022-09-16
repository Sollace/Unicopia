package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;

/**
 * Interface for a magic spells
 */
public interface Spell extends NbtSerialisable, Affine {
    Serializer<Spell> SERIALIZER = Serializer.of(Spell::readNbt, Spell::writeNbt);

    /**
     * Returns the registered type of this spell.
     */
    SpellType<?> getType();

    /**
     * Gets the traits of this spell.
     */
    SpellTraits getTraits();

    /**
     * The unique id of this particular spell instance.
     */
    UUID getUuid();

    /**
     * Determines whether this spell is a valid surrogate for the given spell's id.
     */
    default boolean equalsOrContains(UUID id) {
        return Objects.equal(getUuid(), id);
    }

    /**
     * Returns an optional containing the spell that matched the given predicate.
     */
    default Stream<Spell> findMatches(Predicate<Spell> predicate) {
        return predicate.test(this) ? Stream.of(this) : Stream.empty();
    }

    /**
     * Sets this effect as dead.
     */
    void setDead();

    /**
     * Returns true if this spell is dead, and must be cleaned up.
     */
    boolean isDead();

    /**
     * Returns true if this effect has changes that need to be sent to the client.
     */
    boolean isDirty();

    /**
     * Applies this spell to the supplied caster.
     */
    default boolean apply(Caster<?> caster) {
        caster.getSpellSlot().put(this);
        return true;
    }

    /**
     * Called to generate this spell's effects.
     * @param caster    The caster currently fueling this spell
     * @param situation The situation in which the spell is being applied.
     */
    boolean tick(Caster<?> caster, Situation situation);

    /**
     * Marks this effect as dirty.
     */
    void setDirty();

    /**
     * Called when a gem is destroyed.
     */
    void onDestroyed(Caster<?> caster);

    /**
     * Converts this spell into a placeable spell.
     */
    default PlaceableSpell toPlaceable() {
        return SpellType.PLACED_SPELL.withTraits().create().setSpell(this);
    }

    /**
     * Converts this spell into a throwable spell.
     * @return
     */
    default ThrowableSpell toThrowable() {
        return SpellType.THROWN_SPELL.withTraits().create().setSpell(this);
    }

    @Nullable
    static Spell readNbt(@Nullable NbtCompound compound) {
        if (compound != null && compound.contains("effect_id")) {
            Spell effect = SpellType.getKey(compound).withTraits().create();

            if (effect != null) {
                effect.fromNBT(compound);
            }

            return effect;
        }

        return null;
    }

    static NbtCompound writeNbt(Spell effect) {
        NbtCompound compound = effect.toNBT();
        effect.getType().toNbt(compound);
        return compound;
    }
}
