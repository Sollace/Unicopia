package com.minelittlepony.unicopia.ability;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.BlockDestructionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.TreeType;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.RayTraceHelper;
import com.minelittlepony.unicopia.util.WorldEvent;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Earth Pony kicking ability
 */
public class EarthPonyKickAbility implements Ability<Pos> {

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
        return 3;
    }

    @Nullable
    @Override
    public Pos tryActivate(Pony player) {
        Optional<BlockPos> p = RayTraceHelper.doTrace(player.getMaster(), 6, 1).getBlockPos();

        if (p.isPresent()) {
            BlockPos pos = p.get();
            TreeType tree = TreeType.get(player.getWorld().getBlockState(pos));

            if (tree != TreeType.NONE) {
                pos = tree.findBase(player.getWorld(), pos);
                if (tree.countBlocks(player.getWorld(), pos) > 0) {
                    return new Pos(pos);
                }
            }
        }

        return null;
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public void apply(Pony iplayer, Pos data) {
        PlayerEntity player = iplayer.getMaster();

        boolean harmed = player.getHealth() < player.getMaxHealth();

        if (harmed && player.world.random.nextInt(30) == 0) {
            iplayer.subtractEnergyCost(3);
            return;
        }

        BlockPos pos = data.pos();


        BlockDestructionManager destr = ((BlockDestructionManager.Source)player.world).getDestructionManager();

        if (destr.getBlockDestruction(pos) + 4 >= BlockDestructionManager.MAX_DAMAGE) {
            if (!harmed || player.world.random.nextInt(30) == 0) {
                TreeType.get(player.world.getBlockState(pos)).traverse(player.world, pos, (w, state, p, recurseLevel) -> {
                    if (recurseLevel < 5) {
                        w.breakBlock(p, true);
                    } else {
                        Block.dropStacks(w.getBlockState(p), w, p);
                        w.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                    }
                });
            }

            iplayer.subtractEnergyCost(3);
        } else {
            int cost = dropApples(player, pos);

            if (cost > 0) {
                iplayer.subtractEnergyCost(cost * 3);
            }
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().add(40);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        int timeDiff = getCooldownTime(player) - player.getAbilities().getStat(slot).getRemainingCooldown();

        if (player.getMaster().getEntityWorld().getTime() % 1 == 0 || timeDiff == 0) {
            spawnParticleRing(player.getMaster(), timeDiff, 1);
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

    private int dropApples(PlayerEntity player, BlockPos pos) {
        TreeType tree = TreeType.get(player.world.getBlockState(pos));

        if (tree.countBlocks(player.world, pos) > 0) {

            List<ItemEntity> capturedDrops = Lists.newArrayList();

            tree.traverse(player.world, pos, (world, state, position, recurse) -> {
                affectBlockChange(player, position);
            }, (world, state, position, recurse) -> {
                affectBlockChange(player, position);

                if (world.getBlockState(position.down()).isAir()) {
                    WorldEvent.play(WorldEvent.DESTROY_BLOCK, world, position, state);
                    capturedDrops.add(new ItemEntity(world,
                            position.getX() + world.random.nextFloat(),
                            position.getY() - 0.5,
                            position.getZ() + world.random.nextFloat(),
                            tree.pickRandomStack()
                        ));
                }
            });

            capturedDrops.forEach(item -> {
                item.setToDefaultPickupDelay();
                player.world.spawnEntity(item);
            });

            return capturedDrops.size() / 3;
        }

        return 0;
    }

    private void affectBlockChange(PlayerEntity player, BlockPos position) {
        BlockDestructionManager destr = ((BlockDestructionManager.Source)player.world).getDestructionManager();

        destr.damageBlock(position, 4);

    }
}
