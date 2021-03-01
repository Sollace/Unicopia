package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Thrown;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AttractiveSpell extends ShieldSpell implements Thrown {

    @Nullable
    private BlockPos homingPos;

    protected AttractiveSpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public void render(Caster<?> source) {
        int range = 4 + (source.getLevel().get() * 2);
        Vec3d pos = source.getOriginVector();

        source.spawnParticles(new Sphere(false, range), range * 9, p -> {
            source.addParticle(new MagicParticleEffect(getType().getColor()), p, p.subtract(pos));
        });
    }

    @Override
    public double getDrawDropOffRange(Caster<?> caster) {
        return 10 + (caster.getLevel().get() * 2);
    }

    @Override
    protected List<Entity> getTargets(Caster<?> source, double radius) {

        if (homingPos != null) {
            return VecHelper.findInRange(source.getEntity(), source.getWorld(), source.getOriginVector(), radius, i -> i instanceof ItemEntity);
        }

        return super.getTargets(source, radius);
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = homingPos == null ? source.getOriginVector() : Vec3d.of(homingPos);

        double force = 2.5F * distance;

        if (source.getAffinity() != Affinity.BAD && target instanceof PlayerEntity) {
            force *= calculateAdjustedForce(Pony.of((PlayerEntity)target));
        }

        if (source.getAffinity() == Affinity.BAD && source.getWorld().random.nextInt(4500) == 0) {
            source.getEntity().damage(MagicalDamageSource.create("vortex"), 4);
        }

        applyForce(pos, target, -force, 0);

        float maxVel = source.getAffinity() == Affinity.BAD ? 1 : 1.6f;

        Vec3d vel = target.getVelocity();

        double x = MathHelper.clamp(vel.x, -maxVel, maxVel);
        double y = MathHelper.clamp(vel.y, -maxVel, maxVel);
        double z = MathHelper.clamp(vel.z, -maxVel, maxVel);

        if (distance < 0.5) {
            z += maxVel * 2;
        }

        target.setVelocity(x, y, z);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        if (homingPos != null) {
            compound.put("homingPos", NbtSerialisable.writeBlockPos(homingPos));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        if (compound.contains("homingPos")) {
            homingPos = NbtSerialisable.readBlockPos(compound.getCompound("homingPos"));
        }
    }

}
