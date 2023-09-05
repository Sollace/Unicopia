package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

/**
 * Internal.
 * <p>
 * Used by the Rainboom ability.
 */
public class RainboomAbilitySpell extends AbstractSpell {

    private static final int RADIUS = 5;
    private static final Shape EFFECT_RANGE = new Sphere(false, RADIUS);

    private final ParticleHandle particlEffect = new ParticleHandle();

    private int age;

    public RainboomAbilitySpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        particlEffect.destroy();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.BODY) {
            return false;
        }

        particlEffect.update(getUuid(), source, spawner -> {
            spawner.addParticle(UParticles.RAINBOOM_TRAIL, source.getOriginVector(), Vec3d.ZERO);
        }).ifPresent(attachment -> {
            attachment.setAttribute(Attachment.ATTR_BOUND, 1);
        });

        if (source.isClient()) {
           // source.addParticle(new OrientedBillboardParticleEffect(UParticles.RAINBOOM_RING, source.getPhysics().getMotionAngle()), source.getOriginVector(), Vec3d.ZERO);
        }

        source.findAllEntitiesInRange(RADIUS).forEach(e -> {
            e.damage(source.damageOf(UDamageTypes.RAINBOOM, source), 6);
        });
        EFFECT_RANGE.translate(source.getOrigin()).getBlockPositions().forEach(pos -> {
            BlockState state = source.asWorld().getBlockState(pos);
            if (state.isIn(UTags.FRAGILE) && source.canModifyAt(pos, ModificationType.PHYSICAL)) {
                source.asWorld().breakBlock(pos, true);
            }
        });

        Vec3d motion = source.asEntity().getRotationVec(1).multiply(1.5);
        Vec3d velocity = source.asEntity().getVelocity().add(motion);

        while (velocity.length() > 3) {
            velocity = velocity.multiply(0.6);
        }

        source.asEntity().setVelocity(velocity);
        if (source instanceof Pony pony) {
            pony.getMagicalReserves().getExhaustion().multiply(0.2F);
        }

        return !source.asEntity().isRemoved() && age++ < 90 + 7 * source.getLevel().getScaled(9);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("age", age);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        age = compound.getInt("age");
    }
}
