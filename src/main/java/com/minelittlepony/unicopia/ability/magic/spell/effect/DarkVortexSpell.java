package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

public class DarkVortexSpell extends AttractiveSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.CHAOS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 70)
            .with(Trait.DARKNESS, 100)
            .build();

    private int accumulatedMass = 10;

    protected DarkVortexSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (accumulatedMass > 20) {
            Vec3d pos = source.getOriginVector();
            source.getWorld().createExplosion(
                    source.getMaster(),
                    MagicalDamageSource.create("super_nova"),
                    null,
                    pos.getX(), pos.getY(), pos.getZ(), 17, true, Explosion.DestructionType.DESTROY
            );
            return false;
        }
        return super.tick(source, situation);
    }

    @Override
    public void generateParticles(Caster<?> source) {
        int range = 4 + (source.getLevel().get() * 2);
        Vec3d pos = source.getOriginVector();

        source.spawnParticles(new Sphere(false, range), range * 9, p -> {
            source.addParticle(new MagicParticleEffect(getType().getColor()), p, p.subtract(pos));
        });

        float radius = (float)getDrawDropOffRange(source) / 2;

        particlEffect.ifAbsent(source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(getType().getColor(), 0.99F, radius), source.getOriginVector(), Vec3d.ZERO);
        }).ifPresent(p -> {
            p.attach(source);
            p.setAttribute(0, radius);
        });
    }

    @Override
    public double getDrawDropOffRange(Caster<?> caster) {
        return accumulatedMass + (caster.getLevel().get() * 2);
    }

    @Override
    protected long applyEntities(Caster<?> source) {
        PosHelper.getAllInRegionMutable(source.getOrigin(), new Sphere(false, ((int)getDrawDropOffRange(source) / 2F))).forEach(i -> {
            CatapultSpell.createBlockEntity(source.getWorld(), i, null);
        });

        return super.applyEntities(source);
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {

        if (distance < 1) {
            accumulatedMass += 1 + getTraits().get(Trait.CHAOS, 0, 2);
            target.damage(MagicalDamageSource.create("black_hole"), Integer.MAX_VALUE);
        } else {
            super.applyRadialEffect(source, target, distance, radius);
        }
    }


    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("accumulatedMass", accumulatedMass);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        accumulatedMass = compound.getInt("accumulatedMass");
    }
}
