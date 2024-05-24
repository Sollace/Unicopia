package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.SpellAttributes;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;

public class FireBoltSpell extends AbstractSpell implements HomingSpell,
        ProjectileDelegate.ConfigurationListener, ProjectileDelegate.EntityHitListener {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 10)
            .with(Trait.CHAOS, 1)
            .with(Trait.STRENGTH, 11)
            .with(Trait.FIRE, 60)
            .build();
    public static final SpellTraits HOMING_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 50)
            .with(Trait.CHAOS, 10)
            .with(Trait.STRENGTH, 11)
            .with(Trait.FIRE, 60)
            .build();

    public static void appendTooltip(CustomisedSpellType<? extends FireBoltSpell> type, List<Text> tooltip) {
        tooltip.add(SpellAttributes.of(SpellAttributes.EXPLOSION_STRENGTH, type.traits().get(Trait.POWER, 0, type.traits().get(Trait.FOCUS) >= 50 ? 500 : 50) / 10F));
        tooltip.add(SpellAttributes.of(SpellAttributes.VELOCITY, 1.3F + type.traits().get(Trait.STRENGTH) / 11F));
        tooltip.add(SpellAttributes.of(SpellAttributes.PROJECTILE_COUNT, 1 + (int)type.traits().get(Trait.EARTH) * 3));

        float homingRange = type.traits().get(Trait.FOCUS);

        if (homingRange >= 50) {
            tooltip.add(SpellAttributes.FOLLOWS_TARGET);
            tooltip.add(SpellAttributes.of(SpellAttributes.FOLLOW_RANGE, homingRange - 50));
        }
    }

    private final EntityReference<Entity> target = new EntityReference<>();

    protected FireBoltSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        hit.getEntity().setOnFireFor(90);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            if (caster instanceof MagicProjectileEntity projectile && getTraits().get(Trait.FOCUS) >= 50) {
                caster.findAllEntitiesInRange(
                    getTraits().get(Trait.FOCUS) - 49,
                    EntityPredicates.VALID_LIVING_ENTITY.and(TargetSelecter.validTarget(this, caster))
                ).findFirst().ifPresent(target -> projectile.setHomingTarget(target));
            }

            return true;
        }

        if (getTraits().get(Trait.FOCUS) >= 50 && target.getOrEmpty(caster.asWorld()).isEmpty()) {
            target.set(caster.findAllEntitiesInRange(
                getTraits().get(Trait.FOCUS) - 49,
                EntityPredicates.VALID_LIVING_ENTITY.and(TargetSelecter.validTarget(this, caster))
            ).findFirst().orElse(null));
        }

        for (int i = 0; i < getNumberOfBalls(caster); i++) {
            getTypeAndTraits().create().toThrowable().throwProjectile(caster, 2).ifPresent(c -> {
                target.ifPresent(caster.asWorld(), c::setHomingTarget);
            });

            caster.playSound(USounds.SPELL_FIRE_BOLT_SHOOT, 0.7F, 0.4F / (caster.asWorld().random.nextFloat() * 0.4F + 0.8F));
        }
        return false;
    }

    @Override
    public void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster) {
        projectile.setItem(Items.FIRE_CHARGE.getDefaultStack());
        projectile.addThrowDamage(getTraits().get(Trait.POWER, 0, getTraits().get(Trait.FOCUS) >= 50 ? 500 : 50) / 10F);
        projectile.setFireTicks(900000);
        projectile.setVelocity(projectile.getVelocity().multiply(1.3 + getTraits().get(Trait.STRENGTH) / 11F));
    }

    protected int getNumberOfBalls(Caster<?> caster) {
        return 1 + caster.asWorld().random.nextInt(3) + (int)getTraits().get(Trait.EARTH) * 3;
    }

    @Override
    public boolean setTarget(Entity target) {
        if (getTraits().get(Trait.FOCUS) >= 50) {
            this.target.set(target);
            return true;
        }
        return false;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("target", target.toNBT());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        target.fromNBT(compound.getCompound("target"));
    }
}
