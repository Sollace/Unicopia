package com.minelittlepony.unicopia.ability;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.data.tree.TreeType;
import com.minelittlepony.unicopia.block.data.BlockDestructionManager;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPConnector;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.RayTraceHelper;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

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
        double distance = MineLPConnector.getPlayerPonyRace(player.getMaster()).isDefault() ? 6 : -6;

        return RayTraceHelper.doTrace(player.getMaster(), distance, 1)
                .getBlockPos()
                .filter(pos -> TreeType.at(pos, player.getReferenceWorld()) != TreeType.NONE)
                .isPresent() ? 3 : 1;
    }

    @Nullable
    @Override
    public Pos tryActivate(Pony player) {
        double distance = MineLPConnector.getPlayerPonyRace(player.getMaster()).isDefault() ? 6 : -6;

        return RayTraceHelper.doTrace(player.getMaster(), distance, 1)
                .getBlockPos()
                .filter(pos -> TreeType.at(pos, player.getReferenceWorld()) != TreeType.NONE)
                .map(Pos::new)
                .orElseGet(() -> getDefaultKickLocation(player));
    }

    private Pos getDefaultKickLocation(Pony player) {
        Vec3d kickVector = player.getMaster().getRotationVector().multiply(1, 0, 1);
        if (!MineLPConnector.getPlayerPonyRace(player.getMaster()).isDefault()) {
            kickVector = kickVector.rotateY((float)Math.PI);
        }
        return new Pos(new BlockPos(player.getOriginVector().add(kickVector)));
    }

    @Override
    public boolean canApply(Pony player, Pos data) {
        BlockPos pos = data.pos();
        TreeType tree = TreeType.at(pos, player.getReferenceWorld());

        return tree == TreeType.NONE || tree.findBase(player.getReferenceWorld(), pos)
                .map(base -> tree.countBlocks(player.getReferenceWorld(), pos) > 0)
                .orElse(false);
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public void apply(Pony iplayer, Pos data) {
        BlockPos pos = data.pos();
        TreeType tree = TreeType.at(pos, iplayer.getReferenceWorld());

        iplayer.setAnimation(Animation.KICK);
        iplayer.subtractEnergyCost(tree == TreeType.NONE ? 1 : 3);

        if (tree == TreeType.NONE) {
            return;
        } else {
            ParticleUtils.spawnParticle(iplayer.getReferenceWorld(), UParticles.GROUND_POUND, data.vec(), Vec3d.ZERO);
        }

        PlayerEntity player = iplayer.getMaster();

        boolean harmed = player.getHealth() < player.getMaxHealth();

        BlockDestructionManager destr = ((BlockDestructionManager.Source)player.world).getDestructionManager();

        if (destr.getBlockDestruction(pos) + 4 >= BlockDestructionManager.MAX_DAMAGE) {
            if (!harmed || player.world.random.nextInt(30) == 0) {
                tree.traverse(player.world, pos, (w, state, p, recurseLevel) -> {
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
                iplayer.subtractEnergyCost(cost / 7F);
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

    private int dropApples(PlayerEntity player, BlockPos pos) {
        TreeType tree = TreeType.at(pos, player.world);

        if (tree.countBlocks(player.world, pos) > 0) {
            List<ItemEntity> capturedDrops = Lists.newArrayList();

            tree.traverse(player.world, pos, (world, state, position, recurse) -> {
                affectBlockChange(player, position);
            }, (world, state, position, recurse) -> {
                affectBlockChange(player, position);

                if (world.getBlockState(position.down()).isAir()) {
                    ItemStack stack = tree.pickRandomStack(state);
                    if (!stack.isEmpty()) {
                        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, position, Block.getRawIdFromState(state));

                        capturedDrops.add(new ItemEntity(world,
                            position.getX() + world.random.nextFloat(),
                            position.getY() - 0.5,
                            position.getZ() + world.random.nextFloat(),
                            stack
                        ));
                    }
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

        PosHelper.all(position, p -> {
            BlockState s = player.world.getBlockState(p);

            if (s.getBlock() instanceof BeehiveBlock) {
                BeehiveBlockEntity hive = (BeehiveBlockEntity)player.world.getBlockEntity(p);
                if (hive != null) {
                    hive.angerBees(player, s, BeehiveBlockEntity.BeeState.BEE_RELEASED);
                }

                Box area = new Box(position).expand(8, 6, 8);
                List<BeeEntity> nearbyBees = player.world.getNonSpectatingEntities(BeeEntity.class, area);

                if (!nearbyBees.isEmpty()) {
                   List<PlayerEntity> nearbyPlayers = player.world.getNonSpectatingEntities(PlayerEntity.class, area);
                   int i = nearbyPlayers.size();

                   for (BeeEntity bee : nearbyBees) {
                      if (bee.getTarget() == null) {
                         bee.setTarget(nearbyPlayers.get(player.world.random.nextInt(i)));
                      }
                   }
                }
            }
        }, PosHelper.HORIZONTAL);
    }
}
