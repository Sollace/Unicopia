package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.block.state.StatePredicate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.Trace;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

/**
 * Unicorn teleport ability
 */
public class UnicornTeleportAbility implements Ability<Pos> {

    @Override
    public Identifier getIcon(Pony player) {
        Identifier id = Abilities.REGISTRY.getId(this);
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + (player.asEntity().isSneaking() ? "_far" : "_near") + ".png");
    }

    @Override
    public Text getName(Pony player) {
        if (player.asEntity().isSneaking()) {
            return Text.translatable(getTranslationKey() + ".far");
        }
        return getName();
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 20;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 50;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canCast();
    }

    @Override
    public double getCostEstimate(Pony player) {
        Pos pos = tryActivate(player);

        if (pos == null) {
            return 0;
        }
        return pos.distanceTo(player) / 10;
    }

    @Override
    public int getColor(Pony player) {
        return SpellType.PORTAL.getColor();
    }

    @Override
    public Pos tryActivate(Pony player) {

        if (!player.canCast()) {
            return null;
        }

        int maxDistance = player.asEntity().isCreative() ? 1000 : 100;


        World w = player.asWorld();

        Trace trace = Trace.create(player.asEntity(), maxDistance, 1, EntityPredicates.EXCEPT_SPECTATOR);
        return trace.getBlockOrEntityPos().map(pos -> {
            final BlockPos originalPos = pos;

            boolean airAbove = enterable(w, pos.up()) && enterable(w, pos.up(2));

            if (exception(w, pos, player.asEntity())) {
                final BlockPos p = pos;
                pos = trace.getSide().map(sideHit -> {
                    if (player.asEntity().isSneaking()) {
                        sideHit = sideHit.getOpposite();
                    }

                    return p.offset(sideHit);
                }).orElse(pos);
            }

            if (enterable(w, pos.down())) {
                pos = pos.down();

                if (enterable(w, pos.down())) {
                    if (!airAbove) {
                        return null;
                    }

                    pos = originalPos.up(2);
                }
            }

            if ((!enterable(w, pos) && exception(w, pos, player.asEntity()))
             || (!enterable(w, pos.up()) && exception(w, pos.up(), player.asEntity()))) {
                return null;
            }

            return new Pos(pos);
        }).orElse(null);
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public void apply(Pony iplayer, Pos data) {
        teleport(iplayer, iplayer, data);
    }

    protected void teleport(Pony teleporter, Caster<?> teleportee, Pos destination) {

        if (!teleporter.canCast()) {
            return;
        }

        Entity participant = teleportee.asEntity();

        if (participant == null) {
            return;
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

        Vec3d dest = destination.vec().add(offset);

        participant.teleport(
                dest.x,
                getTargetYPosition(participant.getEntityWorld(), BlockPos.ofFloored(dest), ShapeContext.of(participant)),
                dest.z
        );
        teleporter.subtractEnergyCost(distance);

        participant.fallDistance /= distance;

        participant.getWorld().playSound(null, destination.pos(), USounds.ENTITY_PLAYER_UNICORN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
    }

    private boolean enterable(World w, BlockPos pos) {
        BlockState state = w.getBlockState(pos);
        return w.isAir(pos) || !state.isOpaque();
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
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().add(30);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
