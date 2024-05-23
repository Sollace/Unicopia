package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;

/**
 * Interface for a magic spells
 */
public interface Spell extends NbtSerialisable, Affine {
    Serializer<Spell> SERIALIZER = Serializer.of(Spell::readNbt, Spell::writeNbt);

    /**
     * Returns the full type that describes this spell.
     */
    CustomisedSpellType<?> getTypeAndTraits();

    default boolean isOf(SpellType<?> type) {
        return getTypeAndTraits().type() == type;
    }

    @Override
    default Affinity getAffinity() {
        return getTypeAndTraits().type().getAffinity();
    }

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
    @SuppressWarnings("unchecked")
    default <T extends Spell> Stream<T> findMatches(SpellPredicate<T> predicate) {
        return predicate == null || predicate.test(this) ? Stream.of((T)this) : Stream.empty();
    }

    /**
     * Sets this effect as dead.
     */
    void setDead();

    /**
     * Returns true if this spell is dead, and must be cleaned up.
     */
    boolean isDead();

    boolean isDying();

    /**
     * Returns true if this effect has changes that need to be sent to the client.
     */
    boolean isDirty();

    /**
     * Applies this spell to the supplied caster.
     * @param caster    The caster to apply the spell to
     */
    default boolean apply(Caster<?> caster) {
        caster.getSpellSlot().put(this);
        if (!caster.isClient()) {
            Ether.get(caster.asWorld()).getOrCreate(this, caster);
        }
        return true;
    }

    /**
     * Gets the default form of this spell used to apply to a caster.
     * @param caster    The caster currently fueling this spell
     */
    @Nullable
    default Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return this;
    }

    /**
     * Called to generate this spell's effects.
     * @param caster    The caster currently fueling this spell
     * @param situation The situation in which the spell is being applied.
     */
    boolean tick(Caster<?> caster, Situation situation);

    /**
     * Called on spells that are actively dying to update any post-death animations before removal.
     * @param caster    The caster currently fueling this spell
     */
    void tickDying(Caster<?> caster);

    /**
     * Marks this effect as dirty.
     */
    void setDirty();

    boolean isHidden();

    void setHidden(boolean hidden);

    /**
     * Called when a gem is destroyed.
     */
    void destroy(Caster<?> caster);

    /**
     * Converts this spell into a placeable spell.
     */
    default PlacementControlSpell toPlaceable() {
        return new PlacementControlSpell(SpellType.PLACE_CONTROL_SPELL.withTraits(), this);
    }

    /**
     * Converts this spell into a throwable spell.
     * @return
     */
    default ThrowableSpell toThrowable() {
        return SpellType.THROWN_SPELL.withTraits().create().setSpell(this);
    }

    @Nullable
    static <T extends Spell> T readNbt(@Nullable NbtCompound compound) {
        try {
            if (compound != null) {
                return CustomisedSpellType.<T>fromNBT(compound).create(compound);
            }
        } catch (Exception e) {
            Unicopia.LOGGER.fatal("Invalid spell nbt {}", e);
        }

        return null;
    }

    static UUID getUuid(@Nullable NbtCompound compound) {
        return compound == null || !compound.containsUuid("uuid") ? Util.NIL_UUID :  compound.getUuid("uuid");
    }

    static NbtCompound writeNbt(Spell effect) {
        NbtCompound compound = effect.toNBT();
        effect.getTypeAndTraits().toNbt(compound);
        return compound;
    }
}
