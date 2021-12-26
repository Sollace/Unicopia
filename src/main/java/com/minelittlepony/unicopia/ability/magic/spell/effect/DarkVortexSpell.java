package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.DiskParticleEffect;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

/**
 * More powerful version of the vortex spell which creates a black hole
 */
public class DarkVortexSpell extends AttractiveSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.CHAOS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 70)
            .with(Trait.DARKNESS, 100)
            .build();

    private static final Vec3d SPHERE_OFFSET = new Vec3d(0, 2, 0);

    private int age = 0;
    private float accumulatedMass = 0;

    protected DarkVortexSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public boolean apply(Caster<?> source) {
        return toPlaceable().apply(source);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.BODY) {
            return true;
        }

        age++;
        setDirty();

        if (age % 20 == 0) {
            source.getWorld().playSound(null, source.getOrigin(), SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, SoundCategory.AMBIENT, 1, 1);
        }

        return super.tick(source, situation);
    }

    @Override
    public boolean isFriendlyTogether(Affine other) {
        return accumulatedMass < 150 && super.isFriendlyTogether(other);
    }

    @Override
    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        return getAttractiveForce(source, entity) > 0;
    }

    @Override
    public void generateParticles(Caster<?> source) {
        super.generateParticles(source);

        float radius = (float)getEventHorizonRadius();

        particlEffect.ifAbsent(getUuid(), source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(getType().getColor(), 0.99F, radius, SPHERE_OFFSET), source.getOriginVector(), Vec3d.ZERO);
        }).ifPresent(p -> {
            p.setAttribute(0, radius);
        });

        source.spawnParticles(ParticleTypes.SMOKE, 3);

        if (age % 11 == 0) {
            source.addParticle(new DiskParticleEffect(Vec3f.ZERO, 1, radius + 1), getOrigin(source), Vec3d.ZERO);
        }
    }

    @Override
    public double getDrawDropOffRange(Caster<?> source) {
        return getEventHorizonRadius() * 20;
    }

    @Override
    protected Vec3d getOrigin(Caster<?> source) {
        return source.getOriginVector().add(SPHERE_OFFSET);
    }

    @Override
    protected long applyEntities(Caster<?> source) {
        if (!source.isClient()) {

            double radius = getEventHorizonRadius();

            if (radius > 3) {
                Vec3d origin = getOrigin(source);
                PosHelper.getAllInRegionMutable(source.getOrigin(), new Sphere(false, radius)).forEach(i -> {
                    CatapultSpell.createBlockEntity(source.getWorld(), i, e -> {
                        applyRadialEffect(source, e, e.getPos().distanceTo(origin), radius);
                    });
                    setDirty();
                });
            }
        }

        return super.applyEntities(source);
    }

    // 1. force decreases with distance: distance scale 1 -> 0
    // 2. max force (at dist 0) is taken from accumulated mass
    // 3. force reaches 0 at distance of drawDropOffRange

    private double getEventHorizonRadius() {
        return Math.sqrt(Math.max(0.001, getMass() - 10));
    }

    private double getAttractiveForce(Caster<?> source, Entity target) {
        return (getMass() * getMass(target)) / Math.pow(getOrigin(source).distanceTo(target.getPos()), 2);
    }

    private double getMass() {
        float pulse = (float)Math.sin(age * 6) / 8F;
        return 10 + Math.min(15, Math.min(0.5F + pulse, (float)Math.exp(age) / 8F - 90) + pulse + accumulatedMass / 10F);
    }

    private double getMass(Entity entity) {
        return entity.getWidth() * entity.getHeight();
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {

        if (distance <= getEventHorizonRadius()) {
            accumulatedMass += getMass(target);
            target.damage(MagicalDamageSource.create("black_hole"), Integer.MAX_VALUE);
            target.discard();
        } else {
            double force = getAttractiveForce(source, target);

            target.setVelocity(target.getVelocity().multiply(Math.min(1, 1 - force)));
            applyForce(getOrigin(source), target, -force, 0);
        }
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("age", age);
        compound.putFloat("accumulatedMass", accumulatedMass);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        age = compound.getInt("age");
        accumulatedMass = compound.getFloat("accumulatedMass");
    }
}
