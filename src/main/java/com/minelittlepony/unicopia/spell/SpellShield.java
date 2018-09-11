package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UClient;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellShield extends AbstractSpell {

	private int strength = 0;

	public SpellShield() {
	}

	public SpellShield(int type) {
		setStrength(type);
	}

	public void setStrength(int level) {
		strength = level;
	}

	@Override
    public String getName() {
        return "shield";
    }

	@Override
	public int getMaxLevel() {
		return -1;
	}

	@Override
	public void render(Entity source) {
		if (UClient.isClientSide()) {
			spawnParticles(source.getEntityWorld(), source.posX, source.posY, source.posZ, 4 + (strength * 2));
		}
	}

	public void renderAt(ICaster<?> source, World w, double x, double y, double z, int level) {
		if (UClient.isClientSide()) {
			if (w.rand.nextInt(4 + level * 4) == 0) {
				spawnParticles(w, x, y, z, 4 + (level * 2));
			}
		}
	}

	protected void spawnParticles(World w, double x, double y, double z, int strength) {
	    IShape sphere = new Sphere(true, strength);

	    Vec3d pos = sphere.computePoint(w.rand);
	    Particles.instance().spawnParticle(Unicopia.MAGIC_PARTICLE, false,
	            pos.x + x, pos.y + y, pos.z + z,
	            0, 0, 0);
	}

	@Override
	public boolean update(Entity source) {
		applyEntities(null, source, source.getEntityWorld(), source.posX, source.posY, source.posZ, strength);
		if (source.getEntityWorld().getWorldTime() % 50 == 0) {
			double radius = 4 + (strength * 2);
			if (!IPower.takeFromPlayer((EntityPlayer)source, radius/4)) {
				setDead();
			}
		}
		return !isDead;
	}

	@Override
	public boolean updateAt(ICaster<?> source, World w, double x, double y, double z, int level) {
		return applyEntities(source, source.getOwner(), w, x, y, z, level);
    }

	private boolean applyEntities(ICaster<?> source, Entity owner, World w, double x, double y, double z, int level) {
		double radius = 4 + (level * 2);

		AxisAlignedBB bb = new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);

		for (Entity i : w.getEntitiesWithinAABBExcludingEntity(source.getEntity(), bb)) {
			if ((!i.equals(owner)
			        || (owner instanceof EntityPlayer
			                && !PlayerSpeciesList.instance().getPlayer((EntityPlayer)owner).getPlayerSpecies().canCast()))) {

				double dist = i.getDistance(x, y, z);
				double dist2 = i.getDistance(x, y - i.getEyeHeight(), z);

				boolean projectile = ProjectileUtil.isProjectile(i);

				if (dist <= radius || dist2 <= radius) {
					if (projectile) {
						if (!ProjectileUtil.isProjectileThrownBy(i, owner)) {
							if (dist < radius/2) {
								i.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1f, 1);
								i.setDead();
							} else {
								ricochet(i, x, y, z);
							}
						}
					} else if (i instanceof EntityLivingBase) {
						double force = dist;
						if (i instanceof EntityPlayer) {
							force = calculateForce((EntityPlayer)i);
						}

						i.addVelocity(
						        -(x - i.posX) / force,
						        -(y - i.posY) / force + (dist < 1 ? dist : 0),
						        -(z - i.posZ) / force);
					}
				}
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

	private void ricochet(Entity projectile, double x, double y, double z) {
		Vec3d position = new Vec3d(projectile.posX, projectile.posY, projectile.posZ);
		Vec3d motion = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ);

		Vec3d normal = position.subtract(x, y, z).normalize();
		Vec3d approach = motion.subtract(normal);

		if (approach.length() >= motion.length()) {
			ProjectileUtil.setThrowableHeading(projectile, normal.x, normal.y, normal.z, (float)motion.length(), 0);
		}
	}

	public void writeToNBT(NBTTagCompound compound) {
		compound.setInteger("spell_strength", strength);
	}

	public void readFromNBT(NBTTagCompound compound) {
		strength = compound.getInteger("spell_strength");
	}
}
