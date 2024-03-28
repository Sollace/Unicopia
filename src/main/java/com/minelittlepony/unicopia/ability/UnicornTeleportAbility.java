package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.Trace;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

/**
 * Unicorn teleport ability
 */
public class UnicornTeleportAbility implements Ability<Pos> {

    @Override
    public Identifier getIcon(Pony player) {
        return getId().withPath(p -> "textures/gui/ability/" + p + (player.asEntity().isSneaking() ? "_far" : "_near") + ".png");
    }

    @Override
    public Text getName(Pony player) {
        if (player.asEntity().isSneaking()) {
            return Text.translatable(getTranslationKey() + ".far");
        }
        return Ability.super.getName(player);
    }

    @Override
    public int getWarmupTime(Pony player) {
        return (int)(20 - Math.min(17F, player.getLevel().get() * 0.75F));
    }

    @Override
    public int getCooldownTime(Pony player) {
        return (int)(50 - Math.min(45F, player.getLevel().get() * 0.75F));
    }

    @Override
    public boolean canUse(Race race) {
        return race.canCast() && race != Race.KIRIN;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return prepare(player).map(pos -> pos.distanceTo(player) / 10).orElse(0D);
    }

    @Override
    public int getColor(Pony player) {
        return SpellType.PORTAL.getColor();
    }

    @Override
    public Optional<Pos> prepare(Pony player) {

        if (!player.canCast()) {
            return Optional.empty();
        }

        int maxDistance = (int)((player.asEntity().isCreative() ? 1000 : 100) + (player.getLevel().get() * 0.25F));

        World w = player.asWorld();

        Trace trace = Trace.create(player.asEntity(), maxDistance, 1, EntityPredicates.EXCEPT_SPECTATOR);
        return trace.getBlockOrEntityPos().map(pos -> {
            final BlockPos originalPos = pos;

            Direction globalUp = player.getPhysics().isGravityNegative() ? Direction.DOWN : Direction.UP;

            boolean originalPosHasSupport = exception(w, pos, player.asEntity());
            boolean originalPosValid = enterable(w, pos.offset(globalUp)) && enterable(w, pos.offset(globalUp, 2));

            if (w.getBlockState(pos).isOf(Blocks.POWDER_SNOW) && !PowderSnowBlock.canWalkOnPowderSnow(player.asEntity())) {
                return null;
            }

            if (originalPosHasSupport) {
                final BlockPos p = pos;
                // offset to adjacent
                pos = trace.getSide().map(sideHit -> {
                    if (player.asEntity().isSneaking()) {
                        sideHit = sideHit.getOpposite();
                    }

                    return p.offset(sideHit);
                }).orElse(pos);
            }

            if (pos.getX() != originalPos.getX() || pos.getZ() != originalPos.getZ()) {
                // check support
                int steps = 0;
                while (enterable(w, pos.offset(globalUp.getOpposite()))) {
                    pos = pos.offset(globalUp.getOpposite());
                    if (++steps > 2) {
                        if (originalPosValid) {
                            pos = originalPos.offset(globalUp);
                            break;
                        } else {
                            return null;
                        }
                    }
                }
            }

            if ((!enterable(w, pos) && exception(w, pos, player.asEntity()))
             || (!enterable(w, pos.offset(globalUp)) && exception(w, pos.offset(globalUp), player.asEntity()))) {
                return null;
            }

            return new Pos(pos);
        });
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public boolean apply(Pony iplayer, Pos data) {
        return teleport(iplayer, iplayer, data);
    }

    protected boolean teleport(Pony teleporter, Caster<?> teleportee, Pos destination) {

        if (!teleporter.canCast()) {
            return false;
        }

        Entity participant = teleportee.asEntity();

        if (participant == null) {
            return false;
        }

        teleportee.asWorld().playSound(null, teleportee.getOrigin(), USounds.ENTITY_PLAYER_UNICORN_TELEPORT, SoundCategory.PLAYERS, 1, 1);

        double distance = destination.distanceTo(teleportee) / 10;

        if (participant.hasVehicle()) {
            Entity mount = participant.getVehicle();

            participant.stopRiding();
            Living.transmitPassengers(mount);
        }

        Vec3d offset = teleportee.getOriginVector()
                .subtract(teleporter.getOriginVector())
                .add(
                    participant.getX() - Math.floor(participant.getX()),
                    0,
                    participant.getZ() - Math.floor(participant.getZ())
                );

        double yPos = getTargetYPosition(participant.getEntityWorld(), destination.pos(), ShapeContext.of(participant));
        Vec3d dest = new Vec3d(
                destination.x() + offset.getX(),
                yPos,
                destination.z() + offset.getZ()
        );
        participant.teleport(dest.x, dest.y, dest.z);
        if (participant.getWorld().getBlockCollisions(participant, participant.getBoundingBox()).iterator().hasNext()) {
            dest = destination.vec();
            participant.teleport(dest.x, participant.getY(), dest.z);
        }
        teleporter.subtractEnergyCost(distance);

        participant.fallDistance /= distance;

        BlockPos blockPos = BlockPos.ofFloored(dest);

        participant.getWorld().playSound(null, blockPos, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
        if (!participant.getEntityWorld().isInBuildLimit(blockPos)) {
            UCriteria.TELEPORT_ABOVE_WORLD.trigger(teleporter.asEntity());
        }

        return true;
    }

    private boolean enterable(World w, BlockPos pos) {
        BlockState state = w.getBlockState(pos);
        if (StatePredicate.isFluid(state) || state.getBlock() instanceof LeavesBlock) {
            return false;
        }
        return w.isAir(pos) || !state.isOpaque() || !state.shouldSuffocate(w, pos);
    }

    private boolean exception(World w, BlockPos pos, PlayerEntity player) {
        BlockState state = w.getBlockState(pos);
        VoxelShape shape;

        return state.hasSolidTopSurface(w, pos, player)
                || StatePredicate.isFluid(state)
                || (shape = state.getCollisionShape(w, pos, ShapeContext.of(player))).isEmpty()
                || shape.getBoundingBox().getYLength() > 1;
    }

    private double getTargetYPosition(World world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos, context);
        return pos.getY() + (shape.isEmpty() ? 0 : shape.getBoundingBox().getYLength());
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(30);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
