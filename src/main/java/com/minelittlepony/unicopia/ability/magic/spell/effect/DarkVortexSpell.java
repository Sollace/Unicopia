package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * More powerful version of the vortex spell which creates a black hole.
 *
 * TODO: Possible uses
 *  - Garbage bin
 *  - Link with a teleportation spell to create a wormhole
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

        if (situation == Situation.PROJECTILE) {
            return false;
        }

        if (situation == Situation.BODY) {
            return true;
        }

        age++;
        setDirty();

        if (age % 20 == 0) {
            source.getWorld().playSound(null, source.getOrigin(), SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, SoundCategory.AMBIENT, 1, 1);
        }

        source.subtractEnergyCost(-accumulatedMass);

        if (!source.isClient() && source.getWorld().random.nextInt(300) == 0) {
            ParticleUtils.spawnParticle(source.getWorld(), UParticles.LIGHTNING_BOLT, getOrigin(source), Vec3d.ZERO);
        }

        return super.tick(source, situation);
    }

    @Override
    public boolean isFriendlyTogether(Affine other) {
        return accumulatedMass < 4;
    }

    @Override
    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        return getAttractiveForce(source, entity) > 0;
    }

    @Override
    public void generateParticles(Caster<?> source) {
        super.generateParticles(source);

        float radius = (float)getEventHorizonRadius();

        particlEffect.update(getUuid(), source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(UParticles.SPHERE, getType().getColor(), 0.99F, radius, SPHERE_OFFSET), source.getOriginVector(), Vec3d.ZERO);
        }).ifPresent(p -> {
            p.setAttribute(0, radius);
        });
        particlEffect.update(getUuid(), "_ring", source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(UParticles.DISK, 0xFFFFFFFF, 0.4F, radius + 1, SPHERE_OFFSET), getOrigin(source), Vec3d.ZERO);
        }).ifPresent(p -> {
            p.setAttribute(0, radius * 2F);
            p.setAttribute(1, 0xAAAAAA);
        });

        double angle = age % 260;

        source.spawnParticles(ParticleTypes.SMOKE, 3);

        if (radius > 2) {
            source.addParticle(new SphereParticleEffect(UParticles.DISK, 0xFF0000, 1, radius),
                getOrigin(source).add(0, 0.2, 0), new Vec3d(0, angle, 10));
            source.addParticle(new SphereParticleEffect(UParticles.DISK, 0xFF0000, 1, radius),
                getOrigin(source).add(0, -0.2, 0), new Vec3d(0, angle, 10));
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

            if (radius > 5) {
                Vec3d origin = getOrigin(source);
                PosHelper.getAllInRegionMutable(source.getOrigin(), new Sphere(false, radius)).forEach(i -> {
                    if (!canAffect(source, i)) {
                        return;
                    }
                    if (source.getOrigin().isWithinDistance(i, getEventHorizonRadius())) {
                        source.getWorld().breakBlock(i, false);
                    } else {
                        CatapultSpell.createBlockEntity(source.getWorld(), i, e -> {
                            applyRadialEffect(source, e, e.getPos().distanceTo(origin), radius);
                        });
                    }
                    setDirty();
                });
            }
        }

        return super.applyEntities(source);
    }

    protected boolean canAffect(Caster<?> source, BlockPos pos) {
        return source.canModifyAt(pos)
            && source.getWorld().getFluidState(pos).isEmpty()
            && source.getWorld().getBlockState(pos).getHardness(source.getWorld(), pos) >= 0;
    }

    // 1. force decreases with distance: distance scale 1 -> 0
    // 2. max force (at dist 0) is taken from accumulated mass
    // 3. force reaches 0 at distance of drawDropOffRange

    private double getEventHorizonRadius() {
        return Math.sqrt(Math.max(0.001, getMass() - 12));
    }

    private double getAttractiveForce(Caster<?> source, Entity target) {
        return (getMass() * getMass(target)) / Math.pow(getOrigin(source).distanceTo(target.getPos()), 2);
    }

    private double getMass() {
        float pulse = (float)Math.sin(age * 8) / 1F;
        return 10 + Math.min(15, Math.min(0.5F + pulse, (float)Math.exp(age) / 8F - 90) + accumulatedMass / 10F) + pulse;
    }

    private double getMass(Entity entity) {
        return entity.getWidth() * entity.getHeight();
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {

        if (target instanceof FallingBlockEntity && source.isClient()) {
            return;
        }

        if (distance <= getEventHorizonRadius()) {

            if (target instanceof MagicProjectileEntity) {
                Item item = ((MagicProjectileEntity)target).getStack().getItem();
                if (item instanceof ProjectileDelegate) {
                    ((ProjectileDelegate) item).onImpact(((MagicProjectileEntity)target), source.getMaster());
                }
            } else if (target instanceof PersistentProjectileEntity) {
                source.getMaster().damage(DamageSource.thrownProjectile(target, ((PersistentProjectileEntity)target).getOwner()), 4);
                target.discard();
                return;
            }

            accumulatedMass += getMass(target);
            setDirty();
            target.damage(MagicalDamageSource.create("black_hole"), Integer.MAX_VALUE);

            source.subtractEnergyCost(-getMass(target) * 10);

            source.getWorld().playSound(null, source.getOrigin(), SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, SoundCategory.AMBIENT, 2, 0.02F);

        } else {
            double force = getAttractiveForce(source, target);

            target.setVelocity(target.getVelocity().multiply(Math.min(1, 1 - force)));
            applyForce(getOrigin(source), target, -force, 0);

            source.subtractEnergyCost(-2);
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
