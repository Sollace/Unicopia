package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.LandingEventHandler;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.ModificationType;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
    public Identifier getIcon(Pony player) {
        Identifier id = Abilities.REGISTRY.getId(this);
        Race race = player.getObservedSpecies();
        return id.withPath(p -> "textures/gui/ability/" + p
            + "_" + (race.isHuman() ? Race.EARTH : race).getId().getPath()
            + ".png");
    }

    @Override
    public double getCostEstimate(Pony player) {
        return rad;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        if (player.asEntity().getVelocity().y * player.getPhysics().getGravitySignum() < 0
                && !player.asEntity().getAbilities().flying
                && !player.asEntity().isGliding()
                && !player.asEntity().isUsingRiptide()) {
            thrustDownwards(player);
            return Hit.INSTANCE;
        }

        return Optional.empty();
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    private void thrustDownwards(Pony player) {
        BlockPos ppos = player.getOrigin();
        BlockPos pos = PosHelper.findSolidGroundAt(player.asWorld(), ppos, player.getPhysics().getGravitySignum());

        double downV = Math.sqrt(ppos.getSquaredDistance(pos)) * player.getPhysics().getGravitySignum();
        player.asEntity().addVelocity(0, -downV, 0);
        player.updateVelocity();
    }

    @Override
    public boolean apply(Pony iplayer, Hit data) {
        final PlayerEntity player = iplayer.asEntity();
        final double initialY = player.getY() + 5;

        var r = new LandingEventHandler.Callback() {
            @Override
            public float dispatch(float fallDistance) {
                // fail if landing above the starting position
                if (player.getY() > initialY) {
                    return fallDistance;
                }

                player.fallDistance = 0;
                BlockPos center = PosHelper.findSolidGroundAt(player.getEntityWorld(), player.getBlockPos(), iplayer.getPhysics().getGravitySignum());

                float heavyness = EnchantmentUtil.getWeight(player);

                iplayer.asWorld().getOtherEntities(player, areaOfEffect.offset(iplayer.getOriginVector())).forEach(i -> {
                    double dist = Math.sqrt(center.getSquaredDistance(i.getBlockPos()));

                    if (dist <= rad + 3) {
                        double inertia = 2 / dist;

                        if (i instanceof LivingEntity l) {
                            inertia *= EnchantmentUtil.getWeight(l);
                        }
                        inertia /= heavyness;

                        double liftAmount = Math.sin(Math.PI * dist / rad) * 12 * iplayer.getPhysics().getGravitySignum();

                        i.addVelocity(
                                -(player.getX() - i.getX()) / inertia,
                                -(player.getY() - i.getY() - liftAmount) / inertia + (dist < 1 ? dist : 0),
                                -(player.getZ() - i.getZ()) / inertia);

                        double amount = (1.5F * player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue() + heavyness * 0.4) / (float)(dist * 1.3F);

                        if (i instanceof PlayerEntity) {
                            Race.Composite race = Pony.of((PlayerEntity)i).getCompositeRace();
                            if (race.canUseEarth()) {
                                amount /= 3;
                            }

                            if (race.canFly()) {
                                amount *= 4;
                            }
                        }

                        if (i instanceof LivingEntity l) {
                            amount /= EnchantmentUtil.getImpactReduction(l);
                        }

                        i.damage((ServerWorld)iplayer.asWorld(), iplayer.damageOf(UDamageTypes.SMASH, iplayer), (float)amount);
                        Living.updateVelocity(i);
                    }
                });

                double radius = rad + heavyness * 0.3;

                spawnEffectAround(iplayer, player, center, radius, rad);

                ParticleUtils.spawnParticle(player.getWorld(), UParticles.GROUND_POUND, player.getX(), player.getY() - 1, player.getZ(), 0, 0, 0);
                BlockState steppingState = player.getSteppingBlockState();
                if (steppingState.isIn(UTags.Blocks.KICKS_UP_DUST)) {
                    ParticleUtils.spawnParticle(player.getWorld(), new BlockStateParticleEffect(UParticles.DUST_CLOUD, steppingState), player.getBlockPos().down().toCenterPos(), Vec3d.ZERO);
                }

                iplayer.subtractEnergyCost(rad);
                iplayer.asEntity().addExhaustion(3);
                return 0F;
            }

            @Override
            public void onCancelled() {
                iplayer.playSound(USounds.GUI_ABILITY_FAIL, 1F);
            }
        };

        if (iplayer.asEntity().isOnGround()) {
            iplayer.setAnimation(Animation.STOMP, Animation.Recipient.ANYONE, 10);
            iplayer.asEntity().jump();
            iplayer.updateVelocity();
            AwaitTickQueue.scheduleTask(iplayer.asWorld(), w -> r.dispatch(0F), 5);
        } else {
            thrustDownwards(iplayer);
            iplayer.waitForFall(r);
        }

        return true;
    }

    public static void spawnEffectAround(Pony pony, Entity source, BlockPos center, double radius, double range) {
        BlockPos.stream(new BlockBox(center).expand(MathHelper.ceil(radius))).forEach(i -> {
            double dist = Math.sqrt(i.getSquaredDistance(source.getX(), source.getY(), source.getZ()));

            if (dist <= radius) {
                spawnEffect(pony, source.getWorld(), i, dist, range);
            }
        });
    }

    public static void spawnEffect(Pony pony, World w, BlockPos pos, double dist, double rad) {
        if (w.getBlockState(pos.up()).isAir()) {
            BlockState state = w.getBlockState(pos);

            float hardness = state.getHardness(w, pos);
            float scaledHardness = (1 - hardness / 70);
            float damage = hardness < 0 ? 0 : MathHelper.clamp((int)((1 - dist / rad) * 9 * scaledHardness), 0, BlockDestructionManager.MAX_DAMAGE - 1);

            stompBlock(pony, w, pos, damage);
        }
    }

    public static void stompBlock(Pony pony, World w, BlockPos pos, float damage) {
        BlockState state = w.getBlockState(pos);

        if (state.isAir() || damage <= 0) {
            return;
        }

        if (BlockDestructionManager.of(w).damageBlock(pos, damage) >= BlockDestructionManager.MAX_DAMAGE && pony.canModifyAt(pos, ModificationType.PHYSICAL)) {
            w.breakBlock(pos, true);

            if (w instanceof ServerWorld) {
                if (state.isIn(BlockTags.BASE_STONE_OVERWORLD) && w.getRandom().nextInt(4) == 0) {
                    ItemStack stack = UItems.PEBBLES.getDefaultStack();
                    stack.setCount(1 + w.getRandom().nextInt(2));
                    Block.dropStack(w, pos, stack);
                    state.onStacksDropped((ServerWorld)w, pos, stack, true);
                }
            }
        } else {
            w.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
        }

        if (state.isIn(UTags.Blocks.KICKS_UP_DUST)) {
            if (w.random.nextInt(4) == 0 && w.isAir(pos.up()) && w.getFluidState(pos.up()).isEmpty()) {
                ParticleUtils.spawnParticle(w, new BlockStateParticleEffect(UParticles.DUST_CLOUD, state), pos.up().toCenterPos(), VecHelper.supply(() -> w.random.nextTriangular(0, 0.1F)));
            }
        }
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(40);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
