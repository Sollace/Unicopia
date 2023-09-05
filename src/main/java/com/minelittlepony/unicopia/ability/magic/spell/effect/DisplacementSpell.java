package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class DisplacementSpell extends AbstractSpell implements HomingSpell, PlaceableSpell.PlacementDelegate, ProjectileDelegate.EntityHitListener {

    private final EntityReference<Entity> target = new EntityReference<>();

    private int ticks = 10;

    protected DisplacementSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return toPlaceable();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        Caster<?> originator = source.getOriginatingCaster();

        originator.asEntity().setGlowing(true);

        ticks--;

        if (originator.isClient()) {
            return !isDead() || ticks >= -10;
        }

        if (ticks == 0) {
            target.ifPresent(originator.asWorld(), target -> apply(originator, target));
        }

        return ticks >= -10;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Caster.of(projectile.getMaster()).ifPresent(originator -> apply(originator, hit.getEntity()));
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

    @Override
    public void onPlaced(Caster<?> source, PlaceableSpell parent, CastSpellEntity entity) {

    }

    @Override
    public void updatePlacement(Caster<?> caster, PlaceableSpell parent) {
        parent.getParticleEffectAttachment(caster).ifPresent(attachment -> {
            float r = 3 - (1 - ((ticks + 10) / 20F)) * 3;
            attachment.setAttribute(Attachment.ATTR_RADIUS, r);
        });
    }

    private void teleport(Caster<?> source, Entity entity, Vec3d pos, Vec3d vel) {
        entity.teleport(pos.x, pos.y, pos.z);
        entity.setVelocity(vel);
        entity.setGlowing(false);
        entity.playSound(USounds.SPELL_DISPLACEMENT_TELEPORT, 1, 1);

        float damage = getTraits().get(Trait.BLOOD);
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
        caster.getOriginatingCaster().asEntity().setGlowing(false);
        target.ifPresent(caster.asWorld(), e -> e.setGlowing(false));
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("ticks", ticks);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        ticks = compound.getInt("ticks");
    }
}
