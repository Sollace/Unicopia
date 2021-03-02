package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.EffectSync;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;

public abstract class Living<T extends LivingEntity> implements Equine<T>, Caster<T> {

    protected final T entity;

    private final EffectSync effectDelegate;

    private boolean prevSneaking;
    private boolean prevLanded;

    @Nullable
    private Runnable landEvent;

    private int invinsibilityTicks;

    private final Enchantments enchants = new Enchantments(this);

    protected Living(T entity, TrackedData<CompoundTag> effect) {
        this.entity = entity;
        this.effectDelegate = new EffectSync(this, effect);

        entity.getDataTracker().startTracking(effect, new CompoundTag());
    }

    public void waitForFall(Runnable action) {
        if (entity.isOnGround()) {
            action.run();
        } else {
            landEvent = action;
        }
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

    public Enchantments getEnchants() {
        return enchants;
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
                if (!effect.onBodyTick(this)) {
                    setSpell(null);
                }
            }
        }

        if (invinsibilityTicks > 0) {
            invinsibilityTicks--;
        }

        if (landEvent != null && entity.isOnGround() && landedChanged()) {
            landEvent.run();
            landEvent = null;
        }

        enchants.tick();

        prevSneaking = entity.isSneaking();
        prevLanded = entity.isOnGround();
    }

    @Override
    public void onJump() {
        if (getPhysics().isGravityNegative()) {
            entity.setVelocity(entity.getVelocity().multiply(1, -1, 1));
        }
    }

    @Override
    public Optional<Boolean> onDamage(DamageSource source, float amount) {

        if (source == DamageSource.LIGHTNING_BOLT) {
            if (invinsibilityTicks > 0 || tryCaptureLightning()) {
                return Optional.of(false);
            }
        }

        return Optional.empty();
    }

    private boolean tryCaptureLightning() {
        return getInventoryStacks().filter(stack -> !stack.isEmpty() && stack.getItem() == UItems.EMPTY_JAR).findFirst().map(stack -> {
            invinsibilityTicks = 20;
            stack.split(1);
            giveBackItem(UItems.LIGHTNING_JAR.getDefaultStack());
            return stack;
        }).isPresent();
    }

    protected Stream<ItemStack> getInventoryStacks() {
        return Stream.of(entity.getStackInHand(Hand.MAIN_HAND), entity.getStackInHand(Hand.OFF_HAND));
    }

    protected void giveBackItem(ItemStack stack) {
        entity.dropStack(stack);
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
        getSpellOrEmpty(DisguiseSpell.class, false).ifPresent(spell -> {
            spell.getDisguise().onImpact(this, distance, damageMultiplier);
        });
    }

    @Override
    public void toNBT(CompoundTag compound) {
        enchants.toNBT(compound);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        enchants.fromNBT(compound);
    }
}
