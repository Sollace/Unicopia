package com.minelittlepony.unicopia.magic.spell;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.EtherialListener;
import com.minelittlepony.unicopia.magic.Spell;
import com.minelittlepony.unicopia.particles.MagicParticleEffect;
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

public class AttractiveSpell extends ShieldSpell implements EtherialListener {

    @Nullable
    private BlockPos homingPos;

    @Override
    public String getName() {
        return "vortex";
    }

    @Override
    public int getTint() {
        return 0x4CDEE7;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public void render(Caster<?> source) {
        int range = 4 + (source.getCurrentLevel() * 2);
        Vec3d pos = source.getOriginVector();

        source.spawnParticles(new Sphere(false, range), range * 9, p -> {
            source.addParticle(new MagicParticleEffect(getTint()), p, p.subtract(pos));
        });
    }

    @Override
    public double getDrawDropOffRange(Caster<?> caster) {
        return 10 + (caster.getCurrentLevel() * 2);
    }

    @Override
    protected List<Entity> getTargets(Caster<?> source, double radius) {

        if (homingPos != null) {
            return VecHelper.findAllEntitiesInRange(source.getEntity(), source.getWorld(), source.getOrigin(), radius)
                .filter(i -> i instanceof ItemEntity)
                .collect(Collectors.toList());
        }

        return super.getTargets(source, radius);
    }

    @Override
    protected void applyRadialEffect(Caster<?> source, Entity target, double distance, double radius) {
        Vec3d pos = homingPos == null ? source.getOriginVector() : Vec3d.of(homingPos);

        double force = 2.5F / distance;

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
    public void onNearbySpellChange(Caster<?> source, Spell effect, int newState) {
        if (effect instanceof ChargingSpell && !isDead()) {
            if (newState == ADDED) {
                if (homingPos == null) {
                    homingPos = source.getOrigin();
                }
                setDirty(true);
            } else if (homingPos.equals(source.getOrigin())) {
                setDead();
                setDirty(true);
                source.notifyNearbySpells(this, 5, REMOVED);
            }
        }
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
