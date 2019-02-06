package com.minelittlepony.unicopia.spell;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;
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
import net.minecraft.world.WorldServer;

public class SpellPortal extends AbstractSpell.RangedAreaSpell implements IUseAction {

    private static final IShape portalZone_X = new Sphere(true, 1, 0, 2, 1);
    private static final IShape portalZone_Y = new Sphere(true, 1, 2, 0, 2);
    private static final IShape portalZone_Z = new Sphere(true, 1, 1, 2, 0);

    private static final AxisAlignedBB TELEPORT_BOUNDS_VERT = new AxisAlignedBB(-1, -0.5, -1, 1, 0.5, 1);
    private static final AxisAlignedBB TELEPORT_BOUNDS = new AxisAlignedBB(-0.5, -0.5, -0.5, 0.5, 3, 0.5);

    @Nullable
    private SpellPortal sibling = null;

    @Nullable
    private BlockPos position = null;

    @Nullable
    private BlockPos destinationPos = null;

    private EnumFacing.Axis axis = EnumFacing.Axis.Y;

    @Nullable
    private UUID casterId;

    private World world;

    @Nullable
    private UUID destinationId;

    @Override
    public String getName() {
        return "portal";
    }

    @Override
    public int getTint() {
        return 0x384C38;
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.GOOD;
    }

    @Override
    public void setDead() {
        super.setDead();

        sibling = null;
        destinationId = null;
        destinationPos = null;

        getDestinationPortal().ifPresent(IMagicEffect::setDead);
    }

    @Override
    public void onPlaced(ICaster<?> caster) {
        world = caster.getWorld();
        casterId = caster.getUniqueId();
        position = caster.getOrigin();

        if (sibling != null) {
            sibling.destinationId = casterId;
            sibling.destinationPos = position;
        }
    }

    @Override
    public SpellCastResult onUse(ItemStack stack, SpellAffinity affinity, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        position = pos.offset(side);
        axis = EnumFacing.getDirectionFromEntityLiving(position, player).getAxis();

        IPlayer prop = PlayerSpeciesList.instance().getPlayer(player);

        IMagicEffect other = prop.getEffect();
        if (other instanceof SpellPortal && other != this && !other.getDead()) {
            ((SpellPortal)other).getActualInstance().setDestinationPortal(this);

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

    public void setDestinationPortal(SpellPortal other) {
        if (!getDestinationPortal().isPresent()) {
            sibling = other;
            other.sibling = this;

            destinationId = other.casterId;
            other.destinationId = casterId;

            sibling.destinationPos = position;
            destinationPos = sibling.position;
        }
    }

    @Override
    public boolean updateOnPerson(ICaster<?> caster) {
        return true;
    }

    @Override
    public boolean update(ICaster<?> source) {
        if (!source.getWorld().isRemote) {
            getDestinationPortal().ifPresent(dest ->
                source.getWorld().getEntitiesWithinAABB(Entity.class, getTeleportBounds().offset(source.getOrigin())).stream()
                    .filter(this::canTeleport)
                    .forEach(i -> teleportEntity(source, dest, i)));
        }

        return true;
    }

    @Override
    public void renderOnPerson(ICaster<?> source) {
        /*noop*/
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(getPortalZone(), 10, pos -> {
            Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos, 0, 0, 0);
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

    public AxisAlignedBB getTeleportBounds() {
        if (axis == EnumFacing.Axis.Y) {
            return TELEPORT_BOUNDS_VERT;
        }

        return TELEPORT_BOUNDS;
    }

    protected boolean canTeleport(Entity i) {
        return !(i instanceof IMagicals) && i.timeUntilPortal == 0;
    }

    protected void teleportEntity(ICaster<?> source, SpellPortal dest, Entity i) {
        EnumFacing.Axis xi = i.getHorizontalFacing().getAxis();

        if (axis != EnumFacing.Axis.Y && xi != axis) {
            return;
        }

        EnumFacing offset = i.getHorizontalFacing();

        double destX = dest.position.getX() + (i.posX - source.getOrigin().getX());
        double destY = dest.position.getY() + (i.posY - source.getOrigin().getY());
        double destZ = dest.position.getZ() + (i.posZ - source.getOrigin().getZ());

        if (axis != EnumFacing.Axis.Y) {
            destX += offset.getXOffset();
            destY++;
            destZ += offset.getZOffset();
        }

        i.timeUntilPortal = i.getPortalCooldown();

        i.getEntityWorld().playSound(null, i.getPosition(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1, 1);

        if (dest.axis != axis) {
            if (xi != dest.axis) {
                i.setPositionAndRotation(i.posX, i.posY, i.posZ, i.rotationYaw + 90, i.rotationPitch);
            }
        }

        i.setPositionAndUpdate(destX, destY, destZ);
        i.getEntityWorld().playSound(null, i.getPosition(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1, 1);
    }

    /**
     * Converts a possibly dead portal effect into a live one by forcing the owner entity to load.
     */
    protected SpellPortal getActualInstance() {
        if (world instanceof WorldServer) {
            Entity i = ((WorldServer)world).getEntityFromUuid(casterId);

            if (i == null) {
                world.getChunk(position);
                i = ((WorldServer)world).getEntityFromUuid(casterId);
            }

            if (i instanceof EntitySpell) {
                IMagicEffect effect = ((EntitySpell) i).getEffect();

                if (effect instanceof SpellPortal) {
                    return (SpellPortal)effect;
                }
            }
        }

        return this;
    }

    /**
     * Gets or loads the destination portal.
     */
    public Optional<SpellPortal> getDestinationPortal() {

        if (getDead()) {
            return Optional.empty();
        }

        Entity i = null;

        if (destinationId != null) {
            i = ((WorldServer)world).getEntityFromUuid(destinationId);

            if (i == null && destinationPos != null) {
                world.getChunk(destinationPos);
                i = ((WorldServer)world).getEntityFromUuid(destinationId);

                if (i == null) {
                    setDead();

                    return Optional.empty();
                }
            }
        }

        if (i instanceof EntitySpell) {
            IMagicEffect effect = ((EntitySpell) i).getEffect();

            if (effect instanceof SpellPortal) {
                sibling = (SpellPortal)effect;
            }
        }

        if (sibling != null && sibling.getDead()) {
            setDead();
        }

        if (sibling != null && destinationPos == null) {
            destinationPos = sibling.position;
        }

        return Optional.ofNullable(sibling);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (destinationPos != null) {
            compound.setTag("destination", InbtSerialisable.writeBlockPos(destinationPos));
        }

        if (casterId != null) {
            compound.setUniqueId("casterId", casterId);
        }

        if (destinationId != null) {
            compound.setUniqueId("destinationId", destinationId);
        }

        compound.setString("axis", axis.getName2());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("destination")) {
            destinationPos = InbtSerialisable.readBlockPos(compound.getCompoundTag("destination"));
        }

        if (compound.hasUniqueId("casterId")) {
            casterId = compound.getUniqueId("casterId");
        }

        if (compound.hasUniqueId("destinationId")) {
            destinationId = compound.getUniqueId("destinationId");
        }

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
