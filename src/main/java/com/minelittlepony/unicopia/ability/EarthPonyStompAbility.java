package com.minelittlepony.unicopia.ability;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.BlockDestructionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.WorldEvent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Earth Pony stomping ability
 */
public class EarthPonyStompAbility implements Ability<Hit> {

    private final double rad = 4;

    private final Box areaOfEffect = new Box(
            -rad, -rad, -rad,
             rad,  rad,  rad
     );

    @Override
    public int getWarmupTime(Pony player) {
        return 3;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 50;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canUseEarth();
    }

    @Override
    public double getCostEstimate(Pony player) {
        return rad;
    }

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {
        if (!player.getMaster().isOnGround() && !player.getMaster().abilities.flying) {
            player.getMaster().addVelocity(0, -6, 0);
            return Hit.INSTANCE;
        }

        return null;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.getMaster();

        BlockPos ppos = player.getBlockPos();
        BlockPos pos = PosHelper.findSolidGroundAt(player.getEntityWorld(), ppos);

        player.addVelocity(0, -(ppos.getSquaredDistance(pos)), 0);

        iplayer.waitForFall(() -> {
            BlockPos center = PosHelper.findSolidGroundAt(player.getEntityWorld(), player.getBlockPos());

            iplayer.getWorld().getOtherEntities(player, areaOfEffect.offset(iplayer.getOriginVector())).forEach(i -> {
                double dist = Math.sqrt(center.getSquaredDistance(i.getBlockPos()));

                if (dist <= rad + 3) {
                    double force = dist / 5;
                    i.addVelocity(
                            -(player.getX() - i.getX()) / force,
                            -(player.getY() - i.getY() - 2) / force + (dist < 1 ? dist : 0),
                            -(player.getZ() - i.getZ()) / force);

                    DamageSource damage = MagicalDamageSource.create("smash", player);

                    double amount = (4 * player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).getValue()) / (float)dist;

                    if (i instanceof PlayerEntity) {
                        Race race = Pony.of((PlayerEntity)i).getSpecies();
                        if (race.canUseEarth()) {
                            amount /= 3;
                        }

                        if (race.canFly()) {
                            amount *= 4;
                        }
                    }

                    i.damage(damage, (float)amount);
                }
            });

            BlockPos.iterate(center.add(-rad, -rad, -rad), center.add(rad, rad, rad)).forEach(i -> {
                double dist = Math.sqrt(i.getSquaredDistance(player.getX(), player.getY(), player.getZ(), true));

                if (dist <= rad) {
                    spawnEffect(player.world, i, dist);
                }
            });

            ParticleUtils.spawnParticle(player.world, UParticles.GROUND_POUND, player.getX(), player.getY() - 1, player.getZ(), 0, 0, 0);

            iplayer.subtractEnergyCost(rad);
        });
    }

    private void spawnEffect(World w, BlockPos pos, double dist) {
        BlockState state = w.getBlockState(pos);
        BlockDestructionManager destr = ((BlockDestructionManager.Source)w).getDestructionManager();

        if (!state.isAir() && w.getBlockState(pos.up()).isAir()) {

            double amount = (1 - dist / rad) * 9;
            float hardness = state.getHardness(w, pos);
            float scaledHardness = (1 - hardness / 70);

            int damage = hardness < 0 ? 0 : MathHelper.clamp((int)(amount * scaledHardness), 2, 9);

            if (destr.damageBlock(pos, damage) >= BlockDestructionManager.MAX_DAMAGE) {
                w.breakBlock(pos, true);
            } else {
                WorldEvent.play(WorldEvent.DESTROY_BLOCK, w, pos, state);
            }
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().add(40);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}
