package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public abstract class AbstractDelegatingSpell implements Spell,
    ProjectileDelegate.ConfigurationListener, ProjectileDelegate.BlockHitListener, ProjectileDelegate.EntityHitListener {

    private boolean dirty;
    private boolean hidden;
    private boolean destroyed;

    private UUID uuid = UUID.randomUUID();

    private final SpellType<?> type;

    public AbstractDelegatingSpell(CustomisedSpellType<?> type) {
        this.type = type.type();
    }

    public abstract Collection<Spell> getDelegates();

    @Override
    public boolean equalsOrContains(UUID id) {
        return Spell.super.equalsOrContains(id) || getDelegates().stream().anyMatch(s -> s.equalsOrContains(id));
    }

    @Override
    public Stream<Spell> findMatches(Predicate<Spell> predicate) {
        return Stream.concat(Spell.super.findMatches(predicate), getDelegates().stream().flatMap(s -> s.findMatches(predicate)));
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public SpellType<?> getType() {
        return type;
    }

    @Override
    public SpellTraits getTraits() {
        return getDelegates().stream().map(Spell::getTraits).reduce(SpellTraits.EMPTY, SpellTraits::union);
    }

    @Override
    public final UUID getUuid() {
        return uuid;
    }

    @Override
    public void setDead() {
        getDelegates().forEach(Spell::setDead);
    }

    @Override
    public void tickDying(Caster<?> caster) {
    }

    @Override
    public boolean isDead() {
        return getDelegates().isEmpty() || getDelegates().stream().allMatch(Spell::isDead);
    }

    @Override
    public boolean isDying() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return dirty || getDelegates().stream().anyMatch(Spell::isDirty);
    }

    @Override
    public void setDirty() {
        dirty = true;
    }

    @Override
    public boolean isHidden() {
        return hidden || getDelegates().stream().allMatch(Spell::isHidden);
    }

    @Override
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public final void destroy(Caster<?> caster) {
        if (destroyed) {
            return;
        }
        destroyed = true;
        setDead();
        onDestroyed(caster);
    }

    protected void onDestroyed(Caster<?> caster) {
        getDelegates().forEach(a -> a.destroy(caster));
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        return execute(getDelegates().stream(), a -> {
            if (a.isDying()) {
                a.tickDying(source);
                return !a.isDead();
            }
            return a.tick(source, situation);
        });
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        getDelegates(BlockHitListener.PREDICATE).forEach(a -> a.onImpact(projectile, hit));
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        getDelegates(EntityHitListener.PREDICATE).forEach(a -> a.onImpact(projectile, hit));
    }

    @Override
    public void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster) {
        getDelegates(ConfigurationListener.PREDICATE).forEach(a -> a.configureProjectile(projectile, caster));
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putUuid("uuid", uuid);
        compound.putBoolean("hidden", hidden);
        saveDelegates(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        dirty = false;
        hidden = compound.getBoolean("hidden");
        if (compound.contains("uuid")) {
            uuid = compound.getUuid("uuid");
        }
        loadDelegates(compound);
    }

    protected abstract void loadDelegates(NbtCompound compound);

    protected abstract void saveDelegates(NbtCompound compound);

    protected <T> Stream<T> getDelegates(Function<? super Spell, T> cast) {
        return getDelegates().stream().map(cast).filter(Objects::nonNull);
    }

    protected static boolean execute(Stream<Spell> spells, Function<Spell, Boolean> action) {
        return spells.reduce(false, (u, a) -> action.apply(a), (a, b) -> a || b);
    }
}
