package com.minelittlepony.unicopia.magic.spell;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.CastResult;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.magic.Useable;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortalSpell extends AbstractSpell.RangedAreaSpell implements Useable {

    private static final Shape portalZone_X = new Sphere(true, 1, 0, 2, 1);
    private static final Shape portalZone_Y = new Sphere(true, 1, 2, 0, 2);
    private static final Shape portalZone_Z = new Sphere(true, 1, 1, 2, 0);

    private static final Box TELEPORT_BOUNDS_VERT = new Box(-1, -0.5, -1, 1, 0.5, 1);
    private static final Box TELEPORT_BOUNDS = new Box(-0.5, -0.5, -0.5, 0.5, 3, 0.5);

    @Nullable
    private PortalSpell sibling = null;

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

        getDestinationPortal().ifPresent(MagicEffect::setDead);
    }

    private PortalSpell bridge;

    @Override
    public void onPlaced(Caster<?> caster) {
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
    public CastResult onUse(ItemUsageContext context, Affinity affinity) {
        position = context.getBlockPos().offset(context.getSide());
        axis = context.getPlayerFacing().getAxis();

        Pony prop = Pony.of(context.getPlayer());

        PortalSpell other = prop.getEffect(PortalSpell.class, true);
        if (other != null) {
            other.getActualInstance().setDestinationPortal(this);

            if (!world.isClient) {
                prop.setEffect(null);
            }
        } else {
            if (!world.isClient) {
                bridge = (PortalSpell)copy();

                prop.setEffect(bridge);
            }
        }

        return CastResult.PLACE;
    }

    @Override
    public CastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, @Nullable Entity hitEntity) {
        return CastResult.NONE;
    }

    public void setDestinationPortal(PortalSpell other) {
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
    public boolean update(Caster<?> source) {
        if (!source.getWorld().isClient) {
            getDestinationPortal().ifPresent(dest ->
                source.getWorld().getEntities(Entity.class, getTeleportBounds().offset(source.getOrigin())).stream()
                    .filter(this::canTeleport)
                    .forEach(i -> teleportEntity(source, dest, i)));
        }

        return true;
    }

    @Override
    public void render(Caster<?> source) {
        // TODO: ParticleTypeRegistry
        /*source.spawnParticles(getPortalZone(), 10, pos -> {
            ParticleTypeRegistry.getTnstance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos, 0, 0, 0, getTint());
        });*/
    }

    public Shape getPortalZone() {
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
        return !(i instanceof IMagicals) && i.portalCooldown == 0;
    }

    protected void teleportEntity(Caster<?> source, PortalSpell dest, Entity i) {
        Direction.Axis xi = i.getHorizontalFacing().getAxis();

        if (axis != Direction.Axis.Y && xi != axis) {
            return;
        }

        Direction offset = i.getHorizontalFacing();

        double destX = dest.position.getX() + (i.x - source.getOrigin().getX());
        double destY = dest.position.getY() + (i.y - source.getOrigin().getY());
        double destZ = dest.position.getZ() + (i.z - source.getOrigin().getZ());

        if (axis != Direction.Axis.Y) {
            destX += offset.getOffsetX();
            destY++;
            destZ += offset.getOffsetZ();
        }

        i.portalCooldown = i.getDefaultPortalCooldown();

        i.getEntityWorld().playSound(null, i.getBlockPos(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1, 1);

        if (dest.axis != axis) {
            if (xi != dest.axis) {
                i.setPositionAndAngles(i.x, i.y, i.z, i.yaw + 90, i.pitch);
            }
        }

        i.setPosition(destX, destY, destZ);
        i.getEntityWorld().playSound(null, i.getBlockPos(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1, 1);
    }

    /**
     * Converts a possibly dead portal effect into a live one by forcing the owner entity to load.
     */
    protected PortalSpell getActualInstance() {
        if (world instanceof ServerWorld) {
            Entity i = ((ServerWorld)world).getEntity(casterId);

            if (i == null) {
                world.getChunk(position);
                i = ((ServerWorld)world).getEntity(casterId);
            }

            if (i instanceof SpellcastEntity) {
                MagicEffect effect = ((SpellcastEntity) i).getEffect();

                if (effect instanceof PortalSpell) {
                    return (PortalSpell)effect;
                }
            }
        }

        return this;
    }

    /**
     * Gets or loads the destination portal.
     */
    public Optional<PortalSpell> getDestinationPortal() {

        if (isDead()) {
            return Optional.empty();
        }

        Entity i = null;

        if (destinationId != null) {
            i = ((ServerWorld)world).getEntity(destinationId);

            if (i == null && destinationPos != null) {
                world.getChunk(destinationPos);
                i = ((ServerWorld)world).getEntity(destinationId);

                if (i == null) {
                    setDead();

                    return Optional.empty();
                }
            }
        }

        if (i instanceof SpellcastEntity) {
            MagicEffect effect = ((SpellcastEntity) i).getEffect();

            if (effect instanceof PortalSpell) {
                sibling = (PortalSpell)effect;
            }
        }

        if (sibling != null && sibling.isDead()) {
            setDead();
        }

        if (sibling != null && destinationPos == null) {
            destinationPos = sibling.position;
        }

        return Optional.ofNullable(sibling);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (destinationPos != null) {
            compound.put("destination", NbtSerialisable.writeBlockPos(destinationPos));
        }

        if (casterId != null) {
            compound.putUuid("casterId", casterId);
        }

        if (destinationId != null) {
            compound.putUuid("destinationId", destinationId);
        }

        compound.putString("axis", axis.getName());
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        if (compound.containsKey("destination")) {
            destinationPos = NbtSerialisable.readBlockPos(compound.getCompound("destination"));
        }

        if (compound.containsKey("casterId")) {
            casterId = compound.getUuid("casterId");
        }

        if (compound.containsKey("destinationId")) {
            destinationId = compound.getUuid("destinationId");
        }

        if (compound.containsKey("axis")) {
            axis = Direction.Axis.fromName(compound.getString("axis").toLowerCase(Locale.ROOT));

            if (axis == null) {
                axis = Direction.Axis.Y;
            }
        } else {
            axis = Direction.Axis.Y;
        }
    }
}
