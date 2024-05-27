package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public final class ThrowableSpell extends AbstractDelegatingSpell implements
    ProjectileDelegate.ConfigurationListener, ProjectileDelegate.BlockHitListener, ProjectileDelegate.EntityHitListener {

    public ThrowableSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    public ThrowableSpell(Spell delegate) {
        super(SpellType.THROWN_SPELL.withTraits(delegate.getTypeAndTraits().traits()), delegate);
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
    public Optional<MagicBeamEntity> throwProjectile(Caster<?> caster) {
        return throwProjectile(caster, 1);
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    public Optional<MagicBeamEntity> throwProjectile(Caster<?> caster, float divergance) {
        World world = caster.asWorld();

        caster.playSound(USounds.SPELL_CAST_SHOOT, 0.7F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (caster.isClient()) {
            return Optional.empty();
        }

        return Optional.ofNullable(delegate.get().prepareForCast(caster, CastingMethod.STORED)).map(s -> {
            MagicBeamEntity projectile = new MagicBeamEntity(world, caster.asEntity(), divergance, s);

            configureProjectile(projectile, caster);
            world.spawnEntity(projectile);

            return projectile;
        });
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

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (delegate.get() instanceof BlockHitListener listener) {
            listener.onImpact(projectile, hit);
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (delegate.get() instanceof EntityHitListener listener) {
            listener.onImpact(projectile, hit);
        }
    }

    @Override
    public void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster) {
        if (delegate.get() instanceof ConfigurationListener listener) {
            listener.configureProjectile(projectile, caster);
        }
    }
}
