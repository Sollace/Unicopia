package com.minelittlepony.unicopia.spell;

import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.particle.Particles;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class SpellPortal extends AbstractSpell implements IUseAction {

	private static final IShape portalZone_X = new Sphere(true, 1, 0, 2, 1);
	private static final IShape portalZone_Y = new Sphere(true, 1, 2, 0, 2);
	private static final IShape portalZone_Z = new Sphere(true, 1, 1, 2, 0);

	private int cooldown = 0;

	@Nullable
	private SpellPortal sibling = null;

	@Nullable
	private BlockPos position = null;

	@Nullable
	private BlockPos destinationPos = null;

	private EnumFacing.Axis axis = EnumFacing.Axis.Y;

    @Override
    public String getName() {
        return "portal";
    }

    @Override
    public int getTint() {
        return 0x384C38;
    }

	@Override
	public void setDead() {
		super.setDead();

		if (sibling != null && !sibling.getDead()) {
		    sibling.setDead();
		}
	}

	@Override
	public SpellCastResult onUse(ItemStack stack, SpellAffinity affinity, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
	    position = pos.offset(side);
	    axis = player.getHorizontalFacing().getAxis();

		IPlayer prop = PlayerSpeciesList.instance().getPlayer(player);

		IMagicEffect other = prop.getEffect();
		if (other instanceof SpellPortal && other != this && !other.getDead()) {
			((SpellPortal)other).notifyMatched(this);

			if (!world.isRemote) {
			    prop.setEffect(null);
			}
		} else {
		    if (!world.isRemote) {
		        prop.setEffect(this);
		    }
		}

		return SpellCastResult.PLACE;
	}

	@Override
	public SpellCastResult onUse(ItemStack stack, SpellAffinity affinity, EntityPlayer player, World world, @Nonnull Entity hitEntity) {
		return SpellCastResult.NONE;
	}

	public void notifyMatched(SpellPortal other) {
		if (sibling == null) {
			sibling = other;
			other.sibling = this;

			sibling.destinationPos = position;
			destinationPos = sibling.position;
		}
	}

	@Override
    public boolean updateOnPerson(ICaster<?> caster) {
        return true;
    }

    @Override
    public boolean update(ICaster<?> source, int level) {
        if (!source.getWorld().isRemote) {
            if (cooldown > 0) {
                cooldown--;
            }

            if (cooldown <= 0) {
                if (destinationPos != null) {
                    getDestinationPortal(source.getWorld()).ifPresent(dest -> {
                        if (!dest.getDead() && teleportNear(source, level)) {
                            dest.cooldown = 30;
                            cooldown = 30;
                        }
                    });
                }
            }
        }

        return true;
    }

    @Override
    public void renderOnPerson(ICaster<?> source) {
        /*noop*/
    }

    @Override
    public void render(ICaster<?> source, int level) {
        source.spawnParticles(getPortalZone(), 10, pos -> {
            Particles.instance().spawnParticle(Unicopia.MAGIC_PARTICLE, false, pos, 0, 0, 0);
        });
    }

	public IShape getPortalZone() {
	    switch (axis) {
	        case X: return portalZone_X;
	        default:
	        case Y: return portalZone_Y;
	        case Z: return portalZone_Z;
	    }
	}

	private static final AxisAlignedBB TELEPORT_BOUNDS = new AxisAlignedBB(-0.5, -0.5, -0.5, 0.5, 3, 0.5);
	private static final AxisAlignedBB DESTINATION_BOUNDS = new AxisAlignedBB(0, 0, 0, 1.5F, 1.5F, 1.5F);

	private boolean teleportNear(ICaster<?> source, int level) {
		return source.getWorld().getEntitiesWithinAABB(Entity.class, TELEPORT_BOUNDS.offset(source.getOrigin())).stream().filter(i -> {
		    if (!(i instanceof IMagicals) && i.timeUntilPortal == 0) {
                EnumFacing offset = i.getHorizontalFacing();

                double destX = destinationPos.getX() + (i.posX - source.getOrigin().getX()) + offset.getXOffset();
                double destY = destinationPos.getY() + (i.posY - source.getOrigin().getY()) + 1;
                double destZ = destinationPos.getZ() + (i.posZ - source.getOrigin().getZ()) + offset.getZOffset();

                i.timeUntilPortal = i.getPortalCooldown();

                i.getEntityWorld().playSound(null, i.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
                i.setPositionAndUpdate(destX, destY, destZ);
                i.getEntityWorld().playSound(null, i.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);

                return true;
            }

		    return false;
		}).count() > 0;
	}

	public Optional<SpellPortal> getDestinationPortal(World w) {
		if (sibling == null) {
		    w.getEntitiesWithinAABB(EntitySpell.class, DESTINATION_BOUNDS.offset(destinationPos)).stream()
		        .filter(i -> i.getEffect() instanceof SpellPortal)
		        .map(i -> (SpellPortal)i.getEffect())
		        .findFirst()
		        .ifPresent(s -> sibling = s);
		}

		return Optional.ofNullable(sibling);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		if (destinationPos != null) {
			NBTTagCompound dest = new NBTTagCompound();

			dest.setInteger("X", destinationPos.getX());
			dest.setInteger("Y", destinationPos.getY());
			dest.setInteger("Z", destinationPos.getZ());

			compound.setTag("destination", dest);
		}

		compound.setInteger("portal_cooldown", cooldown);
		compound.setString("axis", axis.getName2());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("destination")) {
			NBTTagCompound dest = compound.getCompoundTag("destination");

			destinationPos = new BlockPos(
					dest.getInteger("X"),
					dest.getInteger("Y"),
					dest.getInteger("Z")
			);
		}

		cooldown = compound.getInteger("portal_cooldown");
		if (compound.hasKey("axis")) {
    	    axis = EnumFacing.Axis.byName(compound.getString("axis").toLowerCase(Locale.ROOT));

    	    if (axis == null) {
    	        axis = EnumFacing.Axis.Y;
    	    }
		} else {
		    axis = EnumFacing.Axis.Y;
		}
	}
}
