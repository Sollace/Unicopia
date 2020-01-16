package com.minelittlepony.unicopia.magic.spells;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IMagicEffect;
import com.minelittlepony.unicopia.magic.IUseable;
import com.minelittlepony.util.InbtSerialisable;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.fabricmc.fabric.api.particles.ParticleTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class SpellPortal extends AbstractSpell.RangedAreaSpell implements IUseable {

    private static final IShape portalZone_X = new Sphere(true, 1, 0, 2, 1);
    private static final IShape portalZone_Y = new Sphere(true, 1, 2, 0, 2);
    private static final IShape portalZone_Z = new Sphere(true, 1, 1, 2, 0);

    private static final Box TELEPORT_BOUNDS_VERT = new Box(-1, -0.5, -1, 1, 0.5, 1);
    private static final Box TELEPORT_BOUNDS = new Box(-0.5, -0.5, -0.5, 0.5, 3, 0.5);

    @Nullable
    private SpellPortal sibling = null;

    @Nullable
    private BlockPos position = null;

    @Nullable
    private BlockPos destinationPos = null;

    private Direction.Axis axis = Direction.Axis.Y;

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
        return 0x384CA8;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

    @Override
    public void setDead() {
        super.setDead();

        sibling = null;
        destinationId = null;
        destinationPos = null;

        getDestinationPortal().ifPresent(IMagicEffect::setDead);
    }

    private SpellPortal bridge;

    @Override
    public void onPlaced(ICaster<?> caster) {
        world = caster.getWorld();
        casterId = caster.getUniqueId();
        position = caster.getOrigin();

        if (sibling != null) {
            sibling.destinationId = casterId;
            sibling.destinationPos = position;
        }

        if (bridge != null) {
            bridge.onPlaced(caster);
        }
    }

    @Override
    public SpellCastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ) {
        position = pos.offset(side);
        axis = Direction.getDirectionFromEntityLiving(position, player).getAxis();

        IPlayer prop = SpeciesList.instance().getPlayer(player);

        SpellPortal other = prop.getEffect(SpellPortal.class, true);
        if (other != null) {
            other.getActualInstance().setDestinationPortal(this);

            if (!world.isClient) {
                prop.setEffect(null);
            }
        } else {
            if (!world.isClient) {
                bridge = (SpellPortal)copy();

                prop.setEffect(bridge);
            }
        }

        return SpellCastResult.PLACE;
    }

    @Override
    public SpellCastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, @Nullable Entity hitEntity) {
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
    public boolean update(ICaster<?> source) {
        if (!source.getWorld().isClient) {
            getDestinationPortal().ifPresent(dest ->
                source.getWorld().getEntitiesWithinAABB(Entity.class, getTeleportBounds().offset(source.getOrigin())).stream()
                    .filter(this::canTeleport)
                    .forEach(i -> teleportEntity(source, dest, i)));
        }

        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(getPortalZone(), 10, pos -> {
            ParticleTypeRegistry.getTnstance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos, 0, 0, 0, getTint());
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

    public Box getTeleportBounds() {
        if (axis == Direction.Axis.Y) {
            return TELEPORT_BOUNDS_VERT;
        }

        return TELEPORT_BOUNDS;
    }

    protected boolean canTeleport(Entity i) {
        return !(i instanceof IMagicals) && i.timeUntilPortal == 0;
    }

    protected void teleportEntity(ICaster<?> source, SpellPortal dest, Entity i) {
        Direction.Axis xi = i.getHorizontalFacing().getAxis();

        if (axis != Direction.Axis.Y && xi != axis) {
            return;
        }

        Direction offset = i.getHorizontalFacing();

        double destX = dest.position.getX() + (i.posX - source.getOrigin().getX());
        double destY = dest.position.getY() + (i.posY - source.getOrigin().getY());
        double destZ = dest.position.getZ() + (i.posZ - source.getOrigin().getZ());

        if (axis != Direction.Axis.Y) {
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

            if (i instanceof SpellcastEntity) {
                IMagicEffect effect = ((SpellcastEntity) i).getEffect();

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

        if (i instanceof SpellcastEntity) {
            IMagicEffect effect = ((SpellcastEntity) i).getEffect();

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
        super.toNBT(compound);

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
        super.fromNBT(compound);

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
            axis = Direction.Axis.byName(compound.getString("axis").toLowerCase(Locale.ROOT));

            if (axis == null) {
                axis = Direction.Axis.Y;
            }
        } else {
            axis = Direction.Axis.Y;
        }
    }
}
