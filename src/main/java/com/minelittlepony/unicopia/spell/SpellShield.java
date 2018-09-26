package com.minelittlepony.unicopia.spell;

import java.util.Random;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.particle.Particles;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.util.ProjectileUtil;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
	public int getMaxLevel() {
		return 17;
	}

	@Override
	public void render(ICaster<?> source, int level) {
		spawnParticles(source, 4 + (level * 2));
	}

	protected void spawnParticles(ICaster<?> source, int strength) {
	    IShape sphere = new Sphere(true, strength);

	    Random rand = source.getWorld().rand;

	    int x = source.getOrigin().getX();
	    int y = source.getOrigin().getY();
	    int z = source.getOrigin().getZ();

	    for (int i = 0; i < strength * 6; i++) {
    	    Vec3d pos = sphere.computePoint(rand);
    	    Particles.instance().spawnParticle(Unicopia.MAGIC_PARTICLE, false,
    	            pos.x + x, pos.y + y, pos.z + z,
    	            0, 0, 0);
	    }
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

		return !isDead;
	}

	@Override
	public boolean update(ICaster<?> source, int level) {
		double radius = 4 + (level * 2);

		Entity owner = source.getOwner();
		BlockPos pos = source.getOrigin();

		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		BlockPos begin = pos.add(-radius, -radius, -radius);
		BlockPos end = pos.add(radius, radius, radius);

		AxisAlignedBB bb = new AxisAlignedBB(begin, end);

		boolean ownerIsValid = Predicates.MAGI.test(owner);

		for (Entity i : source.getWorld().getEntitiesInAABBexcluding(source.getEntity(), bb, entity -> !(ownerIsValid && entity.equals(owner)))) {
		    double dist = i.getDistance(x, y, z);
            double dist2 = i.getDistance(x, y - i.getEyeHeight(), z);

            if (dist > radius && dist2 > radius) {
                continue;
            }

            if (ProjectileUtil.isProjectile(i)) {
                if (!ProjectileUtil.isProjectileThrownBy(i, owner)) {
                    if (dist < radius/2) {
                        i.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1F, 1);
                        i.setDead();
                    } else {
                        ricochet(i, pos);
                    }
                }
            } else if (i instanceof EntityLivingBase) {
                double force = Math.min(0.25F, dist);

                if (i instanceof EntityPlayer) {
                    force = calculateForce((EntityPlayer)i);
                }

                i.addVelocity(
                        -(x - i.posX) / force,
                        -(y - i.posY) / force + (dist < 1 ? dist : 0),
                        -(z - i.posZ) / force);
            }
		}

		return true;
	}

	protected double calculateForce(EntityPlayer player) {
		Race race = PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies();

		double force = 4 * 8;

		if (race.canUseEarth()) {
			if (player.isSneaking()) {
				force *= 16;
			}
		} else if (race.canFly()) {
			force /= 2;
		}

		return force;
	}

	private void ricochet(Entity projectile, BlockPos pos) {
		Vec3d position = new Vec3d(projectile.posX, projectile.posY, projectile.posZ);
		Vec3d motion = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ);

		Vec3d normal = position.subtract(pos.getX(), pos.getY(), pos.getZ()).normalize();
		Vec3d approach = motion.subtract(normal);

		if (approach.length() >= motion.length()) {
			ProjectileUtil.setThrowableHeading(projectile, normal.x, normal.y, normal.z, (float)motion.length(), 0);
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
