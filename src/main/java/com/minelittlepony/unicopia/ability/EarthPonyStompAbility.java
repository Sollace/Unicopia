package com.minelittlepony.unicopia.ability;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.TreeTraverser;
import com.minelittlepony.unicopia.TreeType;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Multi;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.WorldEvent;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * Earth Pony stomping ability
 */
public class EarthPonyStompAbility implements Ability<Multi> {

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

    @Nullable
    @Override
    public Multi tryActivate(Pony player) {
        HitResult mop = VecHelper.getObjectMouseOver(player.getOwner(), 6, 1);

        if (mop instanceof BlockHitResult && mop.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)mop).getBlockPos();
            BlockState state = player.getWorld().getBlockState(pos);

            if (state.getBlock() instanceof LogBlock) {
                pos = TreeTraverser.descendTree(player.getWorld(), state, pos).get();
                if (TreeTraverser.measureTree(player.getWorld(), state, pos) > 0) {
                    return new Multi(pos, 1);
                }
            }
        }

        if (!player.getOwner().onGround && !player.getOwner().abilities.flying) {
            player.getOwner().addVelocity(0, -6, 0);
            return new Multi(Vec3i.ZERO, 0);
        }

        return null;
    }


    @Override
    public Hit.Serializer<Multi> getSerializer() {
        return Multi.SERIALIZER;
    }


    @Override
    public void apply(Pony iplayer, Multi data) {
        PlayerEntity player = iplayer.getOwner();

        if (data.hitType == 0) {
            BlockPos ppos = player.getBlockPos();
            BlockPos pos = getSolidBlockBelow(ppos, player.getEntityWorld());

            player.addVelocity(0, -(ppos.getSquaredDistance(pos)), 0);

            iplayer.getWorld().getEntities(player, areaOfEffect.offset(iplayer.getOriginVector())).forEach(i -> {
                double dist = Math.sqrt(pos.getSquaredDistance(i.getBlockPos()));

                if (dist <= rad + 3) {
                    double force = dist / 5;
                    i.addVelocity(
                            -(player.getX() - i.getX()) / force,
                            -(player.getY() - i.getY() - 2) / force + (dist < 1 ? dist : 0),
                            -(player.getZ() - i.getZ()) / force);

                    DamageSource damage = MagicalDamageSource.causePlayerDamage("smash", player);

                    double amount = (4 * player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue()) / (float)dist;

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

            BlockPos.iterate(pos.add(-rad, -rad, -rad), pos.add(rad, rad, rad)).forEach(i -> {
                if (i.getSquaredDistance(player.getX(), player.getY(), player.getZ(), true) <= rad*rad) {
                    spawnEffect(player.world, i);
                }
            });

            for (int i = 1; i < 202; i+= 2) {
                spawnParticleRing(player, i, 0);
            }

            iplayer.subtractEnergyCost(rad);
        } else if (data.hitType == 1) {

            boolean harmed = player.getHealth() < player.getMaximumHealth();

            if (harmed && player.world.random.nextInt(30) == 0) {
                iplayer.subtractEnergyCost(3);
                return;
            }

            if (harmed || player.world.random.nextInt(5) == 0) {

                if (!harmed || player.world.random.nextInt(30) == 0) {
                    TreeTraverser.removeTree(player.world, data.pos());
                }

                iplayer.subtractEnergyCost(3);
            } else {
                int cost = dropApples(player.world, data.pos());

                if (cost > 0) {
                    iplayer.subtractEnergyCost(cost * 3);
                }
            }
        }
    }

    private void spawnEffect(World w, BlockPos pos) {
        BlockState state = w.getBlockState(pos);

        if (!state.isAir() && w.getBlockState(pos.up()).isAir()) {
            WorldEvent.DESTROY_BLOCK.play(w, pos, state);
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().addExertion(40);
        player.getOwner().attemptSprintingParticles();
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        int timeDiff = getCooldownTime(player) - player.getAbilities().getStat(slot).getRemainingCooldown();

        if (player.getOwner().getEntityWorld().getTime() % 1 == 0 || timeDiff == 0) {
            spawnParticleRing(player.getOwner(), timeDiff, 1);
        }
    }

    private void spawnParticleRing(PlayerEntity player, int timeDiff, double yVel) {
        int animationTicks = timeDiff / 10;
        if (animationTicks < 6) {
            Shape shape = new Sphere(true, animationTicks, 1, 0, 1);

            double y = 0.5 + (Math.sin(animationTicks) * 1.5);

            yVel *= y * 5;

            for (int i = 0; i < shape.getVolumeOfSpawnableSpace(); i++) {
                Vec3d point = shape.computePoint(player.getEntityWorld().random).add(player.getPos());
                player.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                        point.x,
                        point.y + y,
                        point.z,
                        0, yVel, 0
                );
            }
        }
    }

    private int dropApples(World w, BlockPos pos) {
        BlockState log = w.getBlockState(pos);
        int size = TreeTraverser.measureTree(w, log, pos);
        if (size > 0) {

            List<ItemEntity> capturedDrops = Lists.newArrayList();

            dropApplesPart(capturedDrops, new ArrayList<BlockPos>(), w, log, pos, 0);

            capturedDrops.forEach(item -> {
                item.setToDefaultPickupDelay();
                w.spawnEntity(item);
            });

            return capturedDrops.size() / 3;
        }

        return 0;
    }

    private static void dropApplesPart(List<ItemEntity> drops, List<BlockPos> done, World w, BlockState log, BlockPos pos, int level) {
        if (!done.contains(pos)) {
            done.add(pos);
            pos = TreeTraverser.ascendTree(w, log, pos, false);
            if (level < 10 && TreeTraverser.isWoodOrLeaf(w, log, pos)) {
                BlockState state = w.getBlockState(pos);

                if (state.getBlock() instanceof LeavesBlock && w.getBlockState(pos.down()).isAir()) {
                    WorldEvent.DESTROY_BLOCK.play(w, pos, state);
                    drops.add(new ItemEntity(w,
                            pos.getX() + w.random.nextFloat(),
                            pos.getY() - 0.5,
                            pos.getZ() + w.random.nextFloat(),
                            TreeType.get(log).pickRandomStack()
                        ));
                }

                PosHelper.all(pos, p -> {
                    dropApplesPart(drops, done, w, log, p, level + 1);
                }, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
            }
        }
    }

    private static BlockPos getSolidBlockBelow(BlockPos pos, World w) {
        while (World.isValid(pos)) {
            pos = pos.down();

            if (Block.isFaceFullSquare(w.getBlockState(pos).getCollisionShape(w, pos, EntityContext.absent()), Direction.UP)) {
                return pos;
            }
        }

        return pos;
    }
}
