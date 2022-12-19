package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

public class DisplacementSpell extends AbstractSpell implements HomingSpell, PlaceableSpell.PlacementDelegate {

    private final EntityReference<Entity> target = new EntityReference<>();

    private int ticks = 10;

    protected DisplacementSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        return toPlaceable().apply(caster);
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
            target.ifPresent(originator.asWorld(), target -> {

                Vec3d destinationPos = target.getPos();
                Vec3d destinationVel = target.getVelocity();

                Vec3d sourcePos = originator.getOriginVector();
                Vec3d sourceVel = originator.asEntity().getVelocity();

                teleport(target, sourcePos, sourceVel);
                teleport(originator.asEntity(), destinationPos, destinationVel);
                originator.subtractEnergyCost(destinationPos.distanceTo(sourcePos) / 20F);
            });
        }

        return ticks >= -10;
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

    private void teleport(Entity entity, Vec3d pos, Vec3d vel) {
        entity.teleport(pos.x, pos.y, pos.z);
        entity.setVelocity(vel);
        entity.setGlowing(false);
        entity.playSound(USounds.SPELL_DISPLACEMENT_TELEPORT, 1, 1);

        float damage = getTraits().get(Trait.BLOOD);
        if (damage > 0) {
            entity.damage(MagicalDamageSource.EXHAUSTION, damage);
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
    public void onDestroyed(Caster<?> caster) {
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
