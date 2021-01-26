package com.minelittlepony.unicopia.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.BlockDestructionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.TreeTraverser;
import com.minelittlepony.unicopia.TreeType;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.RayTraceHelper;
import com.minelittlepony.unicopia.util.WorldEvent;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
            BlockState state = player.getWorld().getBlockState(pos);

            if (state.getBlock().isIn(BlockTags.LOGS)) {
                pos = TreeTraverser.Descender.descendTree(player.getWorld(), state, pos).get();
                if (TreeTraverser.Measurer.measureTree(player.getWorld(), state, pos) > 0) {
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
                TreeTraverser.Remover.removeTree(player.world, pos);
            }

            iplayer.subtractEnergyCost(3);
        } else {
            int cost = dropApples(player.world, pos);

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

    private int dropApples(World w, BlockPos pos) {
        BlockState log = w.getBlockState(pos);
        int size = TreeTraverser.Measurer.measureTree(w, log, pos);

        if (size > 0) {
            BlockDestructionManager destr = ((BlockDestructionManager.Source)w).getDestructionManager();
            TreeTraverser.Measurer.getParts(w, log, pos).forEach(position -> {
                destr.damageBlock(position, 4);
            });

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
            pos = TreeTraverser.Ascender.ascendTree(w, log, pos, false);
            if (level < 10 && TreeTraverser.isWoodOrLeaf(w, log, pos)) {
                BlockState state = w.getBlockState(pos);

                if (state.getBlock() instanceof LeavesBlock && w.getBlockState(pos.down()).isAir()) {
                    WorldEvent.play(WorldEvent.DESTROY_BLOCK, w, pos, state);
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
}
