package com.minelittlepony.unicopia.power;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.item.ItemApple;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.data.Location;
import com.minelittlepony.unicopia.world.UWorld;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.PosHelper;
import com.minelittlepony.util.WorldEvent;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static net.minecraft.util.EnumFacing.*;

public class PowerStomp implements IPower<PowerStomp.Data> {

    private final double rad = 4;

    private final AxisAlignedBB areaOfEffect = new AxisAlignedBB(
            -rad, -rad, -rad,
             rad,  rad,  rad
     );

    @Override
    public String getKeyName() {
        return "unicopia.power.earth";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_M;
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
    public PowerStomp.Data tryActivate(IPlayer player) {
        RayTraceResult mop = VecHelper.getObjectMouseOver(player.getOwner(), 6, 1);

        if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = mop.getBlockPos();
            IBlockState state = player.getWorld().getBlockState(pos);
            if (state.getBlock() instanceof BlockLog) {
                pos = getBaseOfTree(player.getWorld(), state, pos);
                if (measureTree(player.getWorld(), state, pos) > 0) {
                    return new Data(pos.getX(), pos.getY(), pos.getZ(), 1);
                }
            }
        }

        if (!player.getOwner().onGround && !player.getOwner().capabilities.isFlying) {
            player.getOwner().addVelocity(0, -6, 0);
            return new Data(0, 0, 0, 0);
        }
        return null;
    }

    @Override
    public Class<PowerStomp.Data> getPackageType() {
        return PowerStomp.Data.class;
    }

    public static BlockPos getSolidBlockBelow(BlockPos pos, World w) {
        while (w.isValid(pos)) {
            pos = pos.down();
            if (w.getBlockState(pos).isSideSolid(w, pos, EnumFacing.UP)) {
                return pos;
            }
        }

        return pos;
    }

    @Override
    public void apply(IPlayer iplayer, Data data) {

        EntityPlayer player = iplayer.getOwner();

        if (data.hitType == 0) {
            BlockPos ppos = player.getPosition();
            BlockPos pos = getSolidBlockBelow(ppos, player.getEntityWorld());

            player.addVelocity(0, -(ppos.distanceSq(pos)), 0);

            iplayer.getWorld().getEntitiesWithinAABBExcludingEntity(player, areaOfEffect.offset(iplayer.getOriginVector())).forEach(i -> {
                double dist = Math.sqrt(i.getDistanceSq(pos));

                if (dist <= rad + 3) {
                    double force = dist / 5;
                    i.addVelocity(
                            -(player.posX - i.posX) / force,
                            -(player.posY - i.posY - 2) / force + (dist < 1 ? dist : 0),
                            -(player.posZ - i.posZ) / force);

                    DamageSource damage = MagicalDamageSource.causePlayerDamage("smash", player);

                    double amount = (4 * player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()) / (float)dist;

                    if (i instanceof EntityPlayer) {
                        Race race = PlayerSpeciesList.instance().getPlayer((EntityPlayer)i).getPlayerSpecies();
                        if (race.canUseEarth()) {
                            amount /= 3;
                        }

                        if (race.canFly()) {
                            amount *= 4;
                        }
                    }

                    i.attackEntityFrom(damage, (float)amount);
                }
            });

            BlockPos.getAllInBoxMutable(pos.add(-rad, -rad, -rad), pos.add(rad, rad, rad)).forEach(i -> {
                if (i.distanceSqToCenter(player.posX, player.posY, player.posZ) <= rad*rad) {
                    spawnEffect(player.world, i);
                }
            });

            for (int i = 1; i < 202; i+= 2) {
                spawnParticleRing(player, i);
            }

            IPower.takeFromPlayer(player, rad);
        } else if (data.hitType == 1) {

            boolean harmed = player.getHealth() < player.getMaxHealth();

            if (harmed && player.world.rand.nextInt(30) == 0) {
                IPower.takeFromPlayer(player, 3);
                return;
            }

            if (harmed || player.world.rand.nextInt(5) == 0) {

                if (!harmed || player.world.rand.nextInt(30) == 0) {
                    UWorld.enqueueTask(() -> {
                        removeTree(player.world, data.pos());
                    });
                }

                IPower.takeFromPlayer(player, 3);
            } else {
                int cost = dropApples(player.world, data.pos());

                if (cost > 0) {
                    IPower.takeFromPlayer(player, cost * 3);
                }
            }
        }
    }

    private void spawnEffect(World w, BlockPos pos) {
        IBlockState state = w.getBlockState(pos);

        if (state.getBlock() != Blocks.AIR) {
            if (w.getBlockState(pos.up()).getBlock() == Blocks.AIR) {
                WorldEvent.DESTROY_BLOCK.play(w, pos, state);
            }
        }
    }

    @Override
    public void preApply(IPlayer player) {
        player.addExertion(40);
        player.getOwner().spawnRunningParticles();
    }

    @Override
    public void postApply(IPlayer player) {
        int timeDiff = getCooldownTime(player) - player.getAbilities().getRemainingCooldown();

        if (player.getOwner().getEntityWorld().getWorldTime() % 1 == 0 || timeDiff == 0) {
            spawnParticleRing(player.getOwner(), timeDiff, 1);
        }
    }

    private void spawnParticleRing(EntityPlayer player, int timeDiff) {
        spawnParticleRing(player, timeDiff, 0);
    }

    private void spawnParticleRing(EntityPlayer player, int timeDiff, double yVel) {
        int animationTicks = (int)(timeDiff / 10);
        if (animationTicks < 6) {
            IShape shape = new Sphere(true, animationTicks, 1, 0, 1);

            double y = 0.5 + (Math.sin(animationTicks) * 1.5);

            yVel *= y * 5;

            for (int i = 0; i < shape.getVolumeOfSpawnableSpace(); i++) {
                Vec3d point = shape.computePoint(player.getEntityWorld().rand);
                Particles.instance().spawnParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), false,
                        player.posX + point.x,
                        player.posY + y + point.y,
                        player.posZ + point.z,
                        0, yVel, 0,
                        Block.getStateId(Blocks.DIRT.getDefaultState()));
            }
        }
    }

    private void removeTree(World w, BlockPos pos) {
        IBlockState log = w.getBlockState(pos);
        int size = measureTree(w, log, pos);
        if (size > 0) {
            pos = ascendTrunk(new ArrayList<BlockPos>(), w, pos, log, 0);

            removeTreePart( w, log, pos, 0);
        }
    }

    private BlockPos ascendTrunk(List<BlockPos> done, World w, BlockPos pos, IBlockState log, int level) {
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

    private void removeTreePart(World w, IBlockState log, BlockPos pos, int level) {
        if (level < 10 && isWoodOrLeaf(w, log, pos)) {
            if (level < 5) {
                w.destroyBlock(pos, true);
            } else {
                IBlockState state = w.getBlockState(pos);
                state.getBlock().dropBlockAsItem(w, pos, state, 0);
                w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            }

            PosHelper.all(pos, p -> {
                removeTreePart(w, log, p, level + 1);
            }, UP, NORTH, SOUTH, EAST, WEST);

        }
    }

    private BlockPos ascendTree(World w, IBlockState log, BlockPos pos, boolean remove) {
        int breaks = 0;
        IBlockState state;
        while (variantAndBlockEquals(w.getBlockState(pos.up()), log)) {
            if (PosHelper.some(pos, p -> isLeaves(w.getBlockState(p), log), HORIZONTALS)) {
                break;
            }

            if (remove) {
                if (breaks < 10) {
                    w.destroyBlock(pos, true);
                } else {
                    state = w.getBlockState(pos);
                    state.getBlock().dropBlockAsItem(w, pos, state, 0);
                    w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
                breaks++;
            }
            pos = pos.up();
        }
        return pos;
    }

    private int dropApples(World w, BlockPos pos) {
        IBlockState log = w.getBlockState(pos);
        int size = measureTree(w, log, pos);
        if (size > 0) {

            List<EntityItem> capturedDrops = Lists.newArrayList();

            dropApplesPart(capturedDrops, new ArrayList<BlockPos>(), w, log, pos, 0);

            UWorld.enqueueTask(() -> {
                capturedDrops.forEach(item -> {
                    item.setNoPickupDelay();
                    w.spawnEntity(item);
                });
            });

            return capturedDrops.size() / 3;
        }

        return 0;
    }

    private void dropApplesPart(List<EntityItem> drops, List<BlockPos> done, World w, IBlockState log, BlockPos pos, int level) {
        if (!done.contains(pos)) {
            done.add(pos);
            pos = ascendTree(w, log, pos, false);
            if (level < 10 && isWoodOrLeaf(w, log, pos)) {
                IBlockState state = w.getBlockState(pos);

                if (state.getBlock() instanceof BlockLeaves && w.getBlockState(pos.down()).getMaterial() == Material.AIR) {
                    WorldEvent.DESTROY_BLOCK.play(w, pos, state);

                    EntityItem item = new EntityItem(w);
                    item.setPosition(pos.getX() + w.rand.nextFloat(), pos.getY() - 0.5, pos.getZ() + w.rand.nextFloat());
                    item.setItem(getApple(w, log));

                    drops.add(item);
                }

                PosHelper.all(pos, p -> {
                    dropApplesPart(drops, done, w, log, p, level + 1);
                }, UP, NORTH, SOUTH, EAST, WEST);
            }
        }
    }

    private ItemStack getApple(World w, IBlockState log) {
        return ItemApple.getRandomItemStack(getVariant(log));
    }

    private int measureTree(World w, IBlockState log, BlockPos pos) {
        List<BlockPos> logs = new ArrayList<BlockPos>();
        List<BlockPos> leaves = new ArrayList<BlockPos>();

        countParts(logs, leaves, w, log, pos);

        return logs.size() <= (leaves.size() / 2) ? logs.size() + leaves.size() : 0;
    }

    private BlockPos getBaseOfTree(World w, IBlockState log, BlockPos pos) {
        return getBaseOfTreePart(new ArrayList<BlockPos>(), w, log, pos);
    }

    private BlockPos getBaseOfTreePart(List<BlockPos> done, World w, IBlockState log, BlockPos pos) {
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

    private boolean isWoodOrLeaf(World w, IBlockState log, BlockPos pos) {
        IBlockState state = w.getBlockState(pos);
        return variantAndBlockEquals(state, log) || (isLeaves(state, log) && ((Boolean)state.getValue(BlockLeaves.DECAYABLE)).booleanValue());
    }

    private void countParts(List<BlockPos> logs, List<BlockPos> leaves, World w, IBlockState log, BlockPos pos) {
        if (logs.contains(pos) || leaves.contains(pos)) {
            return;
        }

        IBlockState state = w.getBlockState(pos);
        boolean yay = false;

        if (state.getBlock() instanceof BlockLeaves && ((Boolean)state.getValue(BlockLeaves.DECAYABLE)).booleanValue() && variantEquals(state, log)) {
            leaves.add(pos);
            yay = true;
        } else if (variantAndBlockEquals(state, log)) {
            logs.add(pos);
            yay = true;
        }

        if (yay) {
            PosHelper.all(pos, p -> {
                countParts(logs, leaves, w, log, p);
            }, UP, NORTH, SOUTH, EAST, WEST);
        }
    }

    private boolean isLeaves(IBlockState state, IBlockState log) {
        return state.getBlock() instanceof BlockLeaves && variantEquals(state, log);
    }

    private boolean variantAndBlockEquals(IBlockState one, IBlockState two) {
        return (one.getBlock() == two.getBlock()) && variantEquals(one, two);
    }

    private boolean variantEquals(IBlockState one, IBlockState two) {
        return getVariant(one) == getVariant(two);
    }

    private Object getVariant(IBlockState state) {
        if (state.getBlock() instanceof BlockLeaves) {
            return ((BlockLeaves)state.getBlock()).getWoodType(state.getBlock().getMetaFromState(state));
        }

        for (Entry<IProperty<?>, ?> i : state.getProperties().entrySet()) {
            if (i.getKey().getName().contentEquals("variant")) {
                return i.getValue();
            }
        }
        return null;
    }

    protected static class Data extends Location {
        @Expose
        public int hitType;

        public Data(int x, int y, int z, int hit) {
            super(x, y, z);
            hitType = hit;
        }
    }
}
