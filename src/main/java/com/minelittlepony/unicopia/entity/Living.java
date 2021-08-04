package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.spell.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;

public abstract class Living<T extends LivingEntity> implements Equine<T>, Caster<T> {

    protected final T entity;

    private final EffectSync effectDelegate;

    private boolean prevSneaking;
    private boolean prevLanded;

    @Nullable
    private Runnable landEvent;

    @Nullable
    private Entity attacker;

    private int invinsibilityTicks;

    private final Enchantments enchants = new Enchantments(this);

    protected Living(T entity, TrackedData<NbtCompound> effect) {
        this.entity = entity;
        this.effectDelegate = new EffectSync(this, effect);

        entity.getDataTracker().startTracking(effect, new NbtCompound());
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
    public SpellContainer getSpellSlot() {
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
        getSpellSlot().get(SpellPredicate.IS_ATTACHED, true).ifPresent(effect -> {
            if (!effect.onBodyTick(this)) {
                setSpell(null);
            }
        });

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

    @Nullable
    @Override
    public final Entity getAttacker() {
        return attacker;
    }

    @Override
    public Optional<Boolean> onDamage(DamageSource source, float amount) {

        if (source == DamageSource.LIGHTNING_BOLT) {
            if (invinsibilityTicks > 0 || tryCaptureLightning()) {
                return Optional.of(false);
            }
        }

        if (source instanceof MagicalDamageSource) {
            Entity attacker = ((MagicalDamageSource)source).getSpell();
            if (attacker != null) {
                this.attacker = attacker;
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
        return getSpellSlot().get(true)
                .filter(effect -> !effect.isDead()
                        && effect instanceof ProjectileImpactListener
                        && ((ProjectileImpactListener)effect).onProjectileImpact(projectile))
                .isPresent();
    }

    protected void handleFall(float distance, float damageMultiplier, DamageSource cause) {
        getSpellSlot().get(SpellType.DISGUISE, false).ifPresent(spell -> {
            spell.getDisguise().onImpact(this, distance, damageMultiplier, cause);
        });
    }

    @Override
    public void toNBT(NbtCompound compound) {
        enchants.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        enchants.fromNBT(compound);
    }
}
