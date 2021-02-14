package com.minelittlepony.unicopia.entity;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.network.EffectSync;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundTag;

public abstract class Living<T extends LivingEntity> implements Equine<T>, Caster<T> {

    protected final T entity;

    private final EffectSync effectDelegate;

    private boolean prevSneaking;
    private boolean prevLanded;

    @Nullable
    private Runnable landEvent;

    protected Living(T entity, TrackedData<CompoundTag> effect) {
        this.entity = entity;
        this.effectDelegate = new EffectSync(this, effect);

        entity.getDataTracker().startTracking(effect, new CompoundTag());
    }

    public void waitForFall(Runnable action) {
        landEvent = action;
    }

    public boolean sneakingChanged() {
        return entity.isSneaking() != prevSneaking;
    }

    public boolean landedChanged() {
        return entity.isOnGround() != prevLanded;
    }

    @Override
    public EffectSync getPrimarySpellSlot() {
        return effectDelegate;
    }

    @Override
    public void setMaster(T owner) {
    }

    @Override
    public T getMaster() {
        return entity;
    }

    @Override
    public void tick() {
        if (hasSpell()) {
            Attached effect = getSpell(Attached.class, true);

            if (effect != null) {
                if (entity.getEntityWorld().isClient()) {
                    effect.renderOnPerson(this);
                }

                if (!effect.updateOnPerson(this)) {
                    setSpell(null);
                }
            }
        }

        prevSneaking = entity.isSneaking();
        prevLanded = entity.isOnGround();

        if (getPhysics().isGravityNegative() && entity.getY() > entity.world.getHeight() + 64) {
            entity.damage(DamageSource.OUT_OF_WORLD, 4.0F);
        }
    }

    @Override
    public void onJump() {
        if (getPhysics().isGravityNegative()) {
            entity.setVelocity(entity.getVelocity().multiply(1, -1, 1));
        }
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        if (hasSpell()) {
            Spell effect = getSpell(true);
            if (!effect.isDead() && effect.handleProjectileImpact(projectile)) {
                return true;
            }
        }

        return false;
    }

    protected void handleFall(float distance, float damageMultiplier) {
        if (landEvent != null) {
            landEvent.run();
            landEvent = null;
        }
        getSpellOrEmpty(DisguiseSpell.class, false).ifPresent(spell -> {
            spell.getDisguise().onImpact(this, distance, damageMultiplier);
        });
    }
}
