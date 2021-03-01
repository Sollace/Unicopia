package com.minelittlepony.unicopia.ability;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.BlockDestructionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.WorldEvent;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
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
        if (!player.getMaster().isOnGround() && player.getMaster().getVelocity().y * player.getPhysics().getGravitySignum() < 0 && !player.getMaster().abilities.flying) {
            thrustDownwards(player);
            return Hit.INSTANCE;
        }

        return null;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    private void thrustDownwards(Pony player) {
        BlockPos ppos = player.getOrigin();
        BlockPos pos = PosHelper.findSolidGroundAt(player.getWorld(), ppos, player.getPhysics().getGravitySignum());

        double downV = Math.sqrt(ppos.getSquaredDistance(pos)) * player.getPhysics().getGravitySignum();
        player.getMaster().addVelocity(0, -downV, 0);
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.getMaster();

        thrustDownwards(iplayer);

        iplayer.waitForFall(() -> {
            BlockPos center = PosHelper.findSolidGroundAt(player.getEntityWorld(), player.getBlockPos(), iplayer.getPhysics().getGravitySignum());

            float heavyness = 1 + EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, player);

            iplayer.getWorld().getOtherEntities(player, areaOfEffect.offset(iplayer.getOriginVector())).forEach(i -> {
                double dist = Math.sqrt(center.getSquaredDistance(i.getBlockPos()));

                if (dist <= rad + 3) {
                    double inertia = 2 / dist;

                    if (i instanceof LivingEntity) {
                        inertia *= 1 + EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, (LivingEntity)i);
                    }
                    inertia /= heavyness;

                    double liftAmount = Math.sin(Math.PI * dist / rad) * 12 * iplayer.getPhysics().getGravitySignum();

                    i.addVelocity(
                            -(player.getX() - i.getX()) / inertia,
                            -(player.getY() - i.getY() - liftAmount) / inertia + (dist < 1 ? dist : 0),
                            -(player.getZ() - i.getZ()) / inertia);

                    DamageSource damage = MagicalDamageSource.create("smash", player);

                    double amount = (1.5F * player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).getValue() + heavyness * 0.4) / (float)(dist * 1.3F);

                    if (i instanceof PlayerEntity) {
                        Race race = Pony.of((PlayerEntity)i).getSpecies();
                        if (race.canUseEarth()) {
                            amount /= 3;
                        }

                        if (race.canFly()) {
                            amount *= 4;
                        }
                    }

                    if (i instanceof LivingEntity) {
                        amount /= 1 + (EnchantmentHelper.getEquipmentLevel(UEnchantments.PADDED, (LivingEntity)i) / 6F);
                    }

                    i.damage(damage, (float)amount);
                }
            });

            double radius = rad + heavyness * 0.3;

            BlockPos.iterate(center.add(-radius, -radius, -radius), center.add(radius, radius, radius)).forEach(i -> {
                double dist = Math.sqrt(i.getSquaredDistance(player.getX(), player.getY(), player.getZ(), true));

                if (dist <= radius) {
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
