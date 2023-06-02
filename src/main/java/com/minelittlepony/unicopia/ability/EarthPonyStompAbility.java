package com.minelittlepony.unicopia.ability;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

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
        if (!player.asEntity().isOnGround()
                && player.asEntity().getVelocity().y * player.getPhysics().getGravitySignum() < 0
                && !player.asEntity().getAbilities().flying) {
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
        BlockPos pos = PosHelper.findSolidGroundAt(player.asWorld(), ppos, player.getPhysics().getGravitySignum());

        double downV = Math.sqrt(ppos.getSquaredDistance(pos)) * player.getPhysics().getGravitySignum();
        player.asEntity().addVelocity(0, -downV, 0);
        player.updateVelocity();
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        PlayerEntity player = iplayer.asEntity();

        iplayer.setAnimation(Animation.STOMP, 10);

        thrustDownwards(iplayer);

        iplayer.waitForFall(() -> {
            BlockPos center = PosHelper.findSolidGroundAt(player.getEntityWorld(), player.getBlockPos(), iplayer.getPhysics().getGravitySignum());

            float heavyness = 1 + EnchantmentHelper.getEquipmentLevel(UEnchantments.HEAVY, player);

            iplayer.asWorld().getOtherEntities(player, areaOfEffect.offset(iplayer.getOriginVector())).forEach(i -> {
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

                    i.damage(iplayer.damageOf(UDamageTypes.SMASH, iplayer), (float)amount);
                    Living.updateVelocity(i);
                }
            });

            double radius = rad + heavyness * 0.3;

            spawnEffectAround(player, center, radius, rad);

            ParticleUtils.spawnParticle(player.world, UParticles.GROUND_POUND, player.getX(), player.getY() - 1, player.getZ(), 0, 0, 0);

            iplayer.subtractEnergyCost(rad);
        });
    }

    public static void spawnEffectAround(Entity source, BlockPos center, double radius, double range) {
        BlockPos.stream(new BlockBox(center).expand(MathHelper.ceil(radius))).forEach(i -> {
            double dist = Math.sqrt(i.getSquaredDistance(source.getX(), source.getY(), source.getZ()));

            if (dist <= radius) {
                spawnEffect(source.world, i, dist, range);
            }
        });
    }

    public static void spawnEffect(World w, BlockPos pos, double dist, double rad) {
        if (w.getBlockState(pos.up()).isAir()) {
            BlockState state = w.getBlockState(pos);

            float hardness = state.getHardness(w, pos);
            float scaledHardness = (1 - hardness / 70);
            float damage = hardness < 0 ? 0 : MathHelper.clamp((int)((1 - dist / rad) * 9 * scaledHardness), 0, BlockDestructionManager.MAX_DAMAGE - 1);

            stompBlock(w, pos, damage);
        }
    }

    public static void stompBlock(World w, BlockPos pos, float damage) {
        BlockState state = w.getBlockState(pos);

        if (state.isAir() || damage <= 0) {
            return;
        }

        if (BlockDestructionManager.of(w).damageBlock(pos, damage) >= BlockDestructionManager.MAX_DAMAGE) {
            w.breakBlock(pos, true);

            if (w instanceof ServerWorld) {
                if (state.getMaterial() == Material.STONE && w.getRandom().nextInt(4) == 0) {
                    ItemStack stack = UItems.PEBBLES.getDefaultStack();
                    stack.setCount(1 + w.getRandom().nextInt(2));
                    Block.dropStack(w, pos, stack);
                    state.onStacksDropped((ServerWorld)w, pos, stack, true);
                }
            }
        } else {
            w.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
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
