package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class DisplacementSpell extends AbstractSpell implements HomingSpell, ProjectileDelegate.HitListener {

    private static final SpellAttribute<Float> DAMAGE_TO_TARGET = SpellAttribute.create(SpellAttributeType.DAMAGE_TO_TARGET, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.BLOOD, blood -> blood);

    static final TooltipFactory TOOLTIP = DAMAGE_TO_TARGET;

    private final EntityReference<Entity> target = dataTracker.startTracking(new EntityReference<>());

    private int ticks = 10;

    protected DisplacementSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method.isIndirectCause() ? this : toPlaceable();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        Caster<?> originator = source.getOriginatingCaster();

        originator.asEntity().setGlowing(true);

        if (situation == Situation.PROJECTILE) {
            return !isDead();
        }

        ticks--;

        if (originator.isClient()) {
            return !isDead() || ticks >= -10;
        }

        if (!originator.isClient()) {
            target.ifPresent(originator.asWorld(), target -> {
                target.setGlowing(true);
                if (ticks == 0) {
                    apply(originator, target);
                }
            });
        }

        return ticks >= -10;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        HitListener.super.onImpact(projectile, hit);
        Caster.of(projectile.getMaster()).ifPresent(originator -> {
            apply(originator, hit.getEntity());
        });
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        if (projectile.getMaster() != null) {
            projectile.getMaster().setGlowing(false);
        }
        target.ifPresent(projectile.asWorld(), e -> e.setGlowing(false));
    }

    private void apply(Caster<?> originator, Entity target) {
        Vec3d destinationPos = target.getPos();
        Vec3d destinationVel = target.getVelocity();

        Vec3d sourcePos = originator.getOriginVector();
        Vec3d sourceVel = originator.asEntity().getVelocity();

        teleport(originator, target, sourcePos, sourceVel);
        teleport(originator, originator.asEntity(), destinationPos, destinationVel);
        originator.subtractEnergyCost(destinationPos.distanceTo(sourcePos) / 20F);
    }

    private void teleport(Caster<?> source, Entity entity, Vec3d pos, Vec3d vel) {
        // TODO: teleport -> requestTeleport
        entity.requestTeleport(pos.x, pos.y, pos.z);
        entity.setVelocity(vel);
        entity.setGlowing(false);
        entity.playSound(USounds.SPELL_DISPLACEMENT_TELEPORT, 1, 1);

        float damage = DAMAGE_TO_TARGET.get(getTraits());
        if (damage > 0) {
            entity.damage(source.damageOf(UDamageTypes.EXHAUSTION, source), damage);
        }
    }

    @Override
    public boolean setTarget(Entity target) {
        this.target.set(target);
        return false;
    }

    @Override
    public int getRange(Caster<?> caster) {
        return 200 + Math.min(2000, 50 * (1 + caster.getLevel().get()));
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        caster.getOriginatingCaster().asEntity().setGlowing(false);
        target.ifPresent(caster.asWorld(), e -> e.setGlowing(false));
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.putInt("ticks", ticks);
        compound.put("target", target.toNBT(lookup));
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        ticks = compound.getInt("ticks");
        target.fromNBT(compound.getCompound("target"), lookup);
    }
}
