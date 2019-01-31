package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.util.ProjectileUtil;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

public class SpellShield extends AbstractSpell {

	private int strength = 0;

    @Override
    public int getCurrentLevel() {
        return strength;
    }

    @Override
    public void setCurrentLevel(int level) {
        strength = level;
    }

	@Override
    public String getName() {
        return "shield";
    }

	@Override
	public int getTint() {
	    return 0x66CDAA;
	}

	@Override
	public int getMaxLevel() {
		return 17;
	}

	@Override
	public void render(ICaster<?> source, int level) {
		spawnParticles(source, 4 + (level * 2));
	}

	protected void spawnParticles(ICaster<?> source, int strength) {
	    source.spawnParticles(new Sphere(true, strength), strength * 6, pos -> {
	        Particles.instance().spawnParticle(UParticles.MAGIC_PARTICLE, false, pos, 0, 0, 0);
	    });
	}

	@Override
	public boolean updateOnPerson(ICaster<?> source) {
	    update(source, strength);

		if (source.getEntity().getEntityWorld().getWorldTime() % 50 == 0) {
			double radius = 4 + (strength * 2);
			if (!IPower.takeFromPlayer((EntityPlayer)source.getOwner(), radius/4)) {
				setDead();
			}
		}

		return !getDead();
	}

	@Override
	public boolean update(ICaster<?> source, int level) {
		double radius = 4 + (level * 2);

		Entity owner = source.getOwner();

		boolean ownerIsValid = source.getAffinity() != SpellAffinity.BAD && Predicates.MAGI.test(owner);

		source.findAllEntitiesInRange(radius)
	        .filter(entity -> !(ownerIsValid && entity.equals(owner)))
	        .forEach(i -> {
    		    double dist = Math.sqrt(i.getDistanceSq(source.getOrigin()));

    		    applyRadialEffect(source, i, dist, radius);
    	    });

		return true;
	}

	protected void applyRadialEffect(ICaster<?> source, Entity target, double distance, double radius) {
	    Vec3d pos = source.getOriginVector();

        if (ProjectileUtil.isProjectile(target)) {
            if (!ProjectileUtil.isProjectileThrownBy(target, source.getOwner())) {
                if (distance < radius/2) {
                    target.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1F, 1);
                    target.setDead();
                } else {
                    ricochet(target, pos);
                }
            }
        } else if (target instanceof EntityLivingBase) {
            double force = Math.min(0.25F, distance);

            if (source.getAffinity() != SpellAffinity.BAD && target instanceof EntityPlayer) {
                force *= calculateAdjustedForce(PlayerSpeciesList.instance().getPlayer((EntityPlayer)target));
            }

            applyForce(pos, target, force, distance);
        }
	}

	/**
	 * Applies a force to the given entity based on distance from the source.
	 */
	protected void applyForce(Vec3d pos, Entity target, double force, double distance) {
	    pos = target.getPositionVector().subtract(pos);

        target.addVelocity(
                force / pos.x,
                force / pos.y + (distance < 1 ? distance : 0),
                force / pos.z
        );
	}

	/**
	 * Returns a force to apply based on the given player's given race.
	 */
	protected double calculateAdjustedForce(IPlayer player) {
		double force = 0.75;

		if (player.getPlayerSpecies().canUseEarth()) {
		    force /= 2;

			if (player.getOwner().isSneaking()) {
				force /= 6;
			}
		} else if (player.getPlayerSpecies().canFly()) {
			force *= 2;
		}

		return force;
	}

	/**
	 * Reverses a projectiles direction to deflect it off the shield's surface.
	 */
	protected void ricochet(Entity projectile, Vec3d pos) {
		Vec3d position = projectile.getPositionVector();
		Vec3d motion = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ);

		Vec3d normal = position.subtract(pos).normalize();
		Vec3d approach = motion.subtract(normal);

		if (approach.length() >= motion.length()) {
			ProjectileUtil.setThrowableHeading(projectile, normal, (float)motion.length(), 0);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setInteger("spell_strength", strength);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		strength = compound.getInteger("spell_strength");
	}
}
