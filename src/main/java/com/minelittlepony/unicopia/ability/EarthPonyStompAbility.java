package com.minelittlepony.unicopia.ability;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.item.AppleItem;
import com.minelittlepony.unicopia.util.AwaitTickQueue;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.WorldEvent;
import com.minelittlepony.unicopia.util.shape.IShape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Earth Pony stomping ability
 */
public class EarthPonyStompAbility implements Ability<EarthPonyStompAbility.Data> {

    private final double rad = 4;

    private final Box areaOfEffect = new Box(
            -rad, -rad, -rad,
             rad,  rad,  rad
     );

    @Override
    public String getKeyName() {
        return "unicopia.power.earth";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_M;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 3;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 50;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies.canUseEarth();
    }

    @Override
    public EarthPonyStompAbility.Data tryActivate(IPlayer player) {
        HitResult mop = VecHelper.getObjectMouseOver(player.getOwner(), 6, 1);

        if (mop instanceof BlockHitResult && mop.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)mop).getBlockPos();
            BlockState state = player.getWorld().getBlockState(pos);

            if (state.getBlock() instanceof LogBlock) {
                pos = getBaseOfTree(player.getWorld(), state, pos);
                if (measureTree(player.getWorld(), state, pos) > 0) {
                    return new Data(pos.getX(), pos.getY(), pos.getZ(), 1);
                }
            }
        }

        if (!player.getOwner().onGround && !player.getOwner().abilities.flying) {
            player.getOwner().addVelocity(0, -6, 0);
            return new Data(0, 0, 0, 0);
        }
        return null;
    }

    @Override
    public Class<EarthPonyStompAbility.Data> getPackageType() {
        return EarthPonyStompAbility.Data.class;
    }

    public static BlockPos getSolidBlockBelow(BlockPos pos, World w) {
        while (World.isValid(pos)) {
            pos = pos.down();

            if (Block.isFaceFullSquare(w.getBlockState(pos).getCollisionShape(w, pos, EntityContext.absent()), Direction.UP)) {
                return pos;
            }
        }

        return pos;
    }

    @Override
    public void apply(IPlayer iplayer, Data data) {

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
                            -(player.x - i.x) / force,
                            -(player.y - i.y - 2) / force + (dist < 1 ? dist : 0),
                            -(player.z - i.z) / force);

                    DamageSource damage = MagicalDamageSource.causePlayerDamage("smash", player);

                    double amount = (4 * player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue()) / (float)dist;

                    if (i instanceof PlayerEntity) {
                        Race race = SpeciesList.instance().getPlayer((PlayerEntity)i).getSpecies();
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
                if (i.getSquaredDistance(player.x, player.y, player.z, true) <= rad*rad) {
                    spawnEffect(player.world, i);
                }
            });

            for (int i = 1; i < 202; i+= 2) {
                spawnParticleRing(player, i);
            }

            iplayer.subtractEnergyCost(rad);
        } else if (data.hitType == 1) {

            boolean harmed = player.getHealth() < player.getHealthMaximum();

            if (harmed && player.world.random.nextInt(30) == 0) {
                iplayer.subtractEnergyCost(3);
                return;
            }

            if (harmed || player.world.random.nextInt(5) == 0) {

                if (!harmed || player.world.random.nextInt(30) == 0) {
                    AwaitTickQueue.enqueueTask(w -> removeTree(w, data.pos()));
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
    public void preApply(IPlayer player) {
        player.addExertion(40);
        player.getOwner().attemptSprintingParticles();
    }

    @Override
    public void postApply(IPlayer player) {
        int timeDiff = getCooldownTime(player) - player.getAbilities().getRemainingCooldown();

        if (player.getOwner().getEntityWorld().getTime() % 1 == 0 || timeDiff == 0) {
            spawnParticleRing(player.getOwner(), timeDiff, 1);
        }
    }

    private void spawnParticleRing(PlayerEntity player, int timeDiff) {
        spawnParticleRing(player, timeDiff, 0);
    }

    private void spawnParticleRing(PlayerEntity player, int timeDiff, double yVel) {
        int animationTicks = timeDiff / 10;
        if (animationTicks < 6) {
            IShape shape = new Sphere(true, animationTicks, 1, 0, 1);

            double y = 0.5 + (Math.sin(animationTicks) * 1.5);

            yVel *= y * 5;

            for (int i = 0; i < shape.getVolumeOfSpawnableSpace(); i++) {
                Vec3d point = shape.computePoint(player.getEntityWorld().random);
                player.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                        player.x + point.x,
                        player.y + y + point.y,
                        player.z + point.z,
                        0, yVel, 0
                );
            }
        }
    }

    private void removeTree(World w, BlockPos pos) {
        BlockState log = w.getBlockState(pos);

        int size = measureTree(w, log, pos);

        if (size > 0) {
            removeTreePart( w, log, ascendTrunk(new ArrayList<BlockPos>(), w, pos, log, 0), 0);
        }
    }

    private BlockPos ascendTrunk(List<BlockPos> done, World w, BlockPos pos, BlockState log, int level) {
        if (level < 3 && !done.contains(pos)) {
            done.add(pos);

            BlockPos result = ascendTree(w, log, pos, true);

            if (variantAndBlockEquals(w.getBlockState(pos.east()), log)) {
                result = ascendTrunk(done, w, pos.east(), log, level + 1);
            }

            if (variantAndBlockEquals(w.getBlockState(pos.west()), log)) {
                result = ascendTrunk(done, w, pos.west(), log, level + 1);
            }

            if (variantAndBlockEquals(w.getBlockState(pos.north()), log)) {
                result = ascendTrunk(done, w, pos.north(), log, level + 1);
            }

            if (variantAndBlockEquals(w.getBlockState(pos.south()), log)) {
                result = ascendTrunk(done, w, pos.south(), log, level + 1);
            }

            return result;
        }
        return pos;
    }

    private void removeTreePart(World w, BlockState log, BlockPos pos, int level) {
        if (level < 10 && isWoodOrLeaf(w, log, pos)) {
            if (level < 5) {
                w.breakBlock(pos, true);
            } else {
                Block.dropStacks(w.getBlockState(pos), w, pos);
                w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            }

            PosHelper.all(pos, p -> {
                removeTreePart(w, log, p, level + 1);
            }, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

        }
    }

    private BlockPos ascendTree(World w, BlockState log, BlockPos pos, boolean remove) {
        int breaks = 0;
        BlockState state;
        while (variantAndBlockEquals(w.getBlockState(pos.up()), log)) {
            if (PosHelper.some(pos, p -> isLeaves(w.getBlockState(p), log), Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                break;
            }

            if (remove) {
                if (breaks < 10) {
                    w.breakBlock(pos, true);
                } else {
                    state = w.getBlockState(pos);
                    Block.dropStacks(state, w, pos);
                    w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
                breaks++;
            }
            pos = pos.up();
        }
        return pos;
    }

    private int dropApples(World w, BlockPos pos) {
        BlockState log = w.getBlockState(pos);
        int size = measureTree(w, log, pos);
        if (size > 0) {

            List<ItemEntity> capturedDrops = Lists.newArrayList();

            dropApplesPart(capturedDrops, new ArrayList<BlockPos>(), w, log, pos, 0);

            AwaitTickQueue.enqueueTask(wo -> {
                capturedDrops.forEach(item -> {
                    item.setToDefaultPickupDelay();
                    wo.spawnEntity(item);
                });
            });

            return capturedDrops.size() / 3;
        }

        return 0;
    }

    private void dropApplesPart(List<ItemEntity> drops, List<BlockPos> done, World w, BlockState log, BlockPos pos, int level) {
        if (!done.contains(pos)) {
            done.add(pos);
            pos = ascendTree(w, log, pos, false);
            if (level < 10 && isWoodOrLeaf(w, log, pos)) {
                BlockState state = w.getBlockState(pos);

                if (state.getBlock() instanceof LeavesBlock && w.getBlockState(pos.down()).isAir()) {
                    WorldEvent.DESTROY_BLOCK.play(w, pos, state);

                    ItemEntity item = new ItemEntity(EntityType.ITEM, w);
                    item.setPosition(pos.getX() + w.random.nextFloat(), pos.getY() - 0.5, pos.getZ() + w.random.nextFloat());
                    item.setStack(getApple(w, log));

                    drops.add(item);
                }

                PosHelper.all(pos, p -> {
                    dropApplesPart(drops, done, w, log, p, level + 1);
                }, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
            }
        }
    }

    private ItemStack getApple(World w, BlockState log) {
        return AppleItem.getRandomItemStack(getVariant(log));
    }

    private int measureTree(World w, BlockState log, BlockPos pos) {
        List<BlockPos> logs = new ArrayList<>();
        List<BlockPos> leaves = new ArrayList<>();

        countParts(logs, leaves, w, log, pos);

        return logs.size() <= (leaves.size() / 2) ? logs.size() + leaves.size() : 0;
    }

    private BlockPos getBaseOfTree(World w, BlockState log, BlockPos pos) {
        return getBaseOfTreePart(new ArrayList<BlockPos>(), w, log, pos);
    }

    private BlockPos getBaseOfTreePart(List<BlockPos> done, World w, BlockState log, BlockPos pos) {
        if (done.contains(pos) || !variantAndBlockEquals(w.getBlockState(pos), log)) {
            return null;
        }
        done.add(pos);

        while (variantAndBlockEquals(w.getBlockState(pos.down()), log)) {
            pos = pos.down();
            done.add(pos);
        }

        BlockPos adjacent = getBaseOfTreePart(done, w, log, pos.north());
        if (adjacent != null && adjacent.getY() < pos.getY()) {
            pos = adjacent;
        }

        adjacent = getBaseOfTreePart(done, w, log, pos.south());
        if (adjacent != null && adjacent.getY() < pos.getY()) {
            pos = adjacent;
        }

        adjacent = getBaseOfTreePart(done, w, log, pos.east());
        if (adjacent != null && adjacent.getY() < pos.getY()) {
            pos = adjacent;
        }

        adjacent = getBaseOfTreePart(done, w, log, pos.west());
        if (adjacent != null && adjacent.getY() < pos.getY()) {
            pos = adjacent;
        }

        if (!done.contains(pos)) {
            done.add(pos);
        }

        return pos;
    }

    private boolean isWoodOrLeaf(World w, BlockState log, BlockPos pos) {
        BlockState state = w.getBlockState(pos);
        return variantAndBlockEquals(state, log) || (isLeaves(state, log) && state.get(LeavesBlock.PERSISTENT));
    }

    private void countParts(List<BlockPos> logs, List<BlockPos> leaves, World w, BlockState log, BlockPos pos) {
        if (logs.contains(pos) || leaves.contains(pos)) {
            return;
        }

        BlockState state = w.getBlockState(pos);
        boolean yay = false;

        if (state.getBlock() instanceof LeavesBlock && state.get(LeavesBlock.PERSISTENT) && variantEquals(state, log)) {
            leaves.add(pos);
            yay = true;
        } else if (variantAndBlockEquals(state, log)) {
            logs.add(pos);
            yay = true;
        }

        if (yay) {
            PosHelper.all(pos, p -> {
                countParts(logs, leaves, w, log, p);
            }, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        }
    }

    private boolean isLeaves(BlockState state, BlockState log) {
        return state.getBlock() instanceof LeavesBlock && variantEquals(state, log);
    }

    private boolean variantAndBlockEquals(BlockState one, BlockState two) {
        return (one.getBlock() == two.getBlock()) && variantEquals(one, two);
    }

    private boolean variantEquals(BlockState one, BlockState two) {
        return getVariant(one) == getVariant(two);
    }

    private Object getVariant(BlockState state) {
        // TODO: Variants are gone
        /*if (state.getBlock() instanceof LeavesBlock) {
            return ((LeavesBlock)state.getBlock()).getWoodType(state);
        }

        return state.getEntries().entrySet().stream()
                .filter(i -> i.getKey().getName().contentEquals("variant"))
                .map(i -> i.getValue())
                .findFirst().orElse(null);*/
        return null;
    }

    protected static class Data extends Ability.Pos {
        @Expose
        public int hitType;

        public Data(int x, int y, int z, int hit) {
            super(x, y, z);
            hitType = hit;
        }
    }
}
