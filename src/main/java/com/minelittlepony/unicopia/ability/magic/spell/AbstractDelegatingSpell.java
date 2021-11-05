package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractDelegatingSpell implements Spell, ProjectileCapable {

    private boolean isDirty;

    private UUID uuid = UUID.randomUUID();

    private final SpellType<?> type;

    public AbstractDelegatingSpell(SpellType<?> type, SpellTraits traits) {
        this.type = type;
    }

    protected abstract Collection<Spell> getDelegates();

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public SpellType<?> getType() {
        return type;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setDead() {
        getDelegates().forEach(Spell::setDead);
    }

    @Override
    public boolean isDead() {
        return getDelegates().isEmpty() || getDelegates().stream().allMatch(Spell::isDead);
    }

    @Override
    public boolean isDirty() {
        return isDirty || getDelegates().stream().anyMatch(Spell::isDirty);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        caster.setSpell(this);
        return true;
    }

    @Override
    public void setDirty() {
        isDirty = true;
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        getDelegates().forEach(spell -> spell.onDestroyed(caster));
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockPos pos, BlockState state) {
        getDelegates().stream().filter(a -> a instanceof ProjectileCapable).forEach(a -> {
            ((ProjectileCapable)a).onImpact(projectile, pos, state);
        });
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, Entity entity) {
        getDelegates().stream().filter(a -> a instanceof ProjectileCapable).forEach(a -> {
            ((ProjectileCapable)a).onImpact(projectile, entity);
        });
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        return execute(getDelegates().stream(), spell -> spell.tick(source, situation));
    }

    private boolean execute(Stream<Spell> spells, Function<Spell, Boolean> action) {
        return spells.reduce(false, (u, a) -> action.apply(a), (a, b) -> a || b);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putUuid("uuid", uuid);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        isDirty = false;
        if (compound.contains("uuid")) {
            uuid = compound.getUuid("uuid");
        }
    }
}
