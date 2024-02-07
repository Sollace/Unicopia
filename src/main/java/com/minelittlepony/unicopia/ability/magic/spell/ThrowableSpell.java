package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public final class ThrowableSpell extends AbstractDelegatingSpell {

    private final SpellReference<Spell> spell = new SpellReference<>();

    public ThrowableSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    public ThrowableSpell setSpell(Spell spell) {
        this.spell.set(spell);
        return this;
    }

    @Override
    public Collection<Spell> getDelegates() {
        return List.of(spell.get());
    }

    @Override
    public boolean apply(Caster<?> source) {
        return throwProjectile(source).isPresent();
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    public Optional<MagicProjectileEntity> throwProjectile(Caster<?> caster) {
        return throwProjectile(caster, 1);
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    public Optional<MagicProjectileEntity> throwProjectile(Caster<?> caster, float divergance) {
        World world = caster.asWorld();

        Entity entity = caster.asEntity();

        caster.playSound(USounds.SPELL_CAST_SHOOT, 0.7F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (caster.isClient()) {
            return Optional.empty();
        }

        Spell s = spell.get().prepareForCast(caster, CastingMethod.STORED);
        if (s == null) {
            return Optional.empty();
        }

        MagicProjectileEntity projectile = UEntities.MAGIC_BEAM.create(world);
        projectile.setPosition(entity.getX(), entity.getEyeY() - 0.1F, entity.getZ());
        projectile.setOwner(entity);
        projectile.setItem(UItems.GEMSTONE.getDefaultStack(spell.get().getType()));
        s.apply(projectile);
        projectile.setVelocity(entity, entity.getPitch(), entity.getYaw(), 0, 1.5F, divergance);
        projectile.setNoGravity(true);
        configureProjectile(projectile, caster);
        world.spawnEntity(projectile);

        return Optional.of(projectile);
    }

    @Override
    protected void loadDelegates(NbtCompound compound) {
        spell.fromNBT(compound.getCompound("spell"));
    }

    @Override
    protected void saveDelegates(NbtCompound compound) {
        compound.put("spell", spell.toNBT());
    }

    @Override
    public ThrowableSpell toThrowable() {
        return this;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void setHidden(boolean hidden) {
    }
}
