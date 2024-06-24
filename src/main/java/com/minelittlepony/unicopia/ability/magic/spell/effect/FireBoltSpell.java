package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

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

    private static final SpellAttribute<Float> VELOCITY = SpellAttribute.create(SpellAttributeType.VELOCITY, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.STRENGTH, strength -> 1.3F + (strength / 11F));
    private static final SpellAttribute<Integer> PROJECTILE_COUNT = SpellAttribute.create(SpellAttributeType.PROJECTILE_COUNT, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.EARTH, earth -> 11 + (int)earth * 3);
    private static final SpellAttribute<Boolean> FOLLOWS_TARGET = SpellAttribute.createConditional(SpellAttributeType.FOLLOWS_TARGET, Trait.FOCUS, focus -> focus >= 50);
    private static final SpellAttribute<Float> FOLLOW_RANGE = SpellAttribute.create(SpellAttributeType.FOLLOW_RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.FOCUS, focus -> Math.max(0F, focus - 49));
    private static final SpellAttribute<Float> MAX_EXPLOSION_STRENGTH = SpellAttribute.create(SpellAttributeType.EXPLOSION_STRENGTH, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.FOCUS, focus -> focus >= 50 ? 10F : 1F);
    private static final SpellAttribute<Float> EXPLOSION_STRENGTH = SpellAttribute.create(SpellAttributeType.EXPLOSION_STRENGTH, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, (traits, focus) -> MathHelper.clamp(focus / 50, 0, MAX_EXPLOSION_STRENGTH.get(traits)));

    static final TooltipFactory TOOLTIP = TooltipFactory.of(MAX_EXPLOSION_STRENGTH, EXPLOSION_STRENGTH, VELOCITY, PROJECTILE_COUNT, FOLLOWS_TARGET, FOLLOW_RANGE.conditionally(FOLLOWS_TARGET::get));

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
        boolean followTarget = FOLLOWS_TARGET.get(getTraits());
        float followRage = FOLLOW_RANGE.get(getTraits());
        if (situation == Situation.PROJECTILE) {
            if (caster instanceof MagicProjectileEntity projectile && followTarget) {
                caster.findAllEntitiesInRange(
                        followRage,
                    EntityPredicates.VALID_LIVING_ENTITY.and(TargetSelecter.validTarget(this, caster))
                ).findFirst().ifPresent(target -> projectile.setHomingTarget(target));
            }

            return true;
        }

        if (followTarget && target.getOrEmpty(caster.asWorld()).isEmpty()) {
            target.set(caster.findAllEntitiesInRange(
                    followRage,
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
        projectile.addThrowDamage(EXPLOSION_STRENGTH.get(getTraits()));
        projectile.setFireTicks(900000);
        projectile.setVelocity(projectile.getVelocity().multiply(VELOCITY.get(getTraits())));
    }

    protected int getNumberOfBalls(Caster<?> caster) {
        return PROJECTILE_COUNT.get(getTraits()) + caster.asWorld().random.nextInt(3);
    }

    @Override
    public boolean setTarget(Entity target) {
        if (FOLLOWS_TARGET.get(getTraits())) {
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
