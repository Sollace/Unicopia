package com.minelittlepony.unicopia.ability;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

/**
 * Earth Pony ability to grow crops
 */
public class EarthPonyGrowAbility implements Ability<Pos> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
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
    public Optional<Pos> prepare(Pony player) {
        return TraceHelper.findBlock(player.asEntity(), 3, 1).map(Pos::new);
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 10;
    }

    @Override
    public boolean apply(Pony player, Pos data) {
        int count = 0;

        if (!applyDirectly(player, data.pos())) {
            for (BlockPos pos : BlockPos.iterate(
                    data.pos().add(-2, -2, -2),
                    data.pos().add( 2,  2,  2))) {
                count += applySingle(player, player.asWorld(), player.asWorld().getBlockState(pos), pos);
            }
        } else {
            count = 1;
        }

        if (count > 0) {
            player.subtractEnergyCost(count / 5D);
        }
        return true;
    }

    protected int applySingle(Pony player, World w, BlockState state, BlockPos pos) {

        ItemStack stack = new ItemStack(Items.BONE_MEAL);

        if (state.getBlock() instanceof Growable growable) {
            return growable.grow(w, state, pos) ? 1 : 0;
        }

        if (state.isOf(Blocks.CARROTS)) {
            if (state.get(CarrotsBlock.AGE) == CarrotsBlock.MAX_AGE) {
                boolean transform = w.random.nextInt(3) == 0;
                spawnConversionParticles(w, pos, transform);
                if (transform) {
                    w.setBlockState(pos, UBlocks.GOLD_ROOT.getDefaultState().with(CarrotsBlock.AGE, CarrotsBlock.MAX_AGE));
                }

                return 5;
            }
        }

        if (w.getBlockState(pos).isIn(UTags.UNAFFECTED_BY_GROW_ABILITY)) {
            return 0;
        }

        if (BoneMealItem.useOnFertilizable(stack, w, pos)) {
            if (w.random.nextInt(350) == 0) {
                if (w.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
                    FarmlandBlock.setToDirt(null, state, w, pos.down());
                }
                w.setBlockState(pos, UBlocks.PLUNDER_VINE_BUD.getDefaultState());
            } else if (w.random.nextInt(5000) == 0) {
                if (w.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
                    FarmlandBlock.setToDirt(null, state, w, pos.down());
                }
                UBlocks.CURING_JOKE.grow(w, state, pos);
            }
            return 1;
        }

        if (BoneMealItem.useOnGround(stack, w, pos, Direction.UP)) {
            return 1;
        }

        return 0;
    }

    private boolean applyDirectly(Pony player, BlockPos pos) {
        return TransmutationRecipe.RECIPES.stream()
            .filter(recipe -> recipe.matches(player.asWorld(), pos))
            .map(recipe -> recipe.checkPattern(player.asWorld(), pos))
            .filter(result -> result.matchedLocations().size() + 1 >= TransmutationRecipe.MINIMUM_INPUT)
            .filter(result -> {
                boolean transform = result.shoudTransform(player.asWorld().random);

                player.playSound(USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, 1);

                result.matchedLocations().forEach(cell -> {
                    spawnConversionParticles(player.asWorld(), cell.up(), false);
                    BlockDestructionManager manager = BlockDestructionManager.of(player.asWorld());
                    if (transform) {
                        if (manager.damageBlock(cell, 8) >= BlockDestructionManager.MAX_DAMAGE || player.asWorld().random.nextInt(20) == 0) {
                            player.asWorld().setBlockState(cell, Blocks.DIRT.getDefaultState());
                            player.asWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, cell, Block.getRawIdFromState(player.asWorld().getBlockState(cell)));
                        }
                    } else {
                        if (manager.damageBlock(cell, 4) >= BlockDestructionManager.MAX_DAMAGE || player.asWorld().random.nextInt(20) == 0) {
                            player.asWorld().setBlockState(cell, Blocks.DIRT.getDefaultState());
                            player.asWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, cell, Block.getRawIdFromState(player.asWorld().getBlockState(cell)));
                        }
                    }
                });

                spawnConversionParticles(player.asWorld(), pos, transform);
                if (transform) {
                    player.asWorld().setBlockState(pos, result.recipe().getResult(player.asWorld(), pos));
                }

                return true;
            })
            .findFirst()
            .isPresent();
    }

    private static void spawnConversionParticles(World w, BlockPos pos, boolean success) {
        DoubleSupplier vecComponentFactory = () -> w.random.nextTriangular(0, 0.5);
        Supplier<Vec3d> posSupplier = () -> pos.toCenterPos().add(VecHelper.supply(vecComponentFactory));

        for (int i = 0; i < 25; i++) {
            ParticleUtils.spawnParticle(w, new MagicParticleEffect(0xFFFF00), posSupplier.get(), Vec3d.ZERO);
            if (success) {
                ParticleUtils.spawnParticle(w, ParticleTypes.CLOUD, posSupplier.get(), Vec3d.ZERO);
            }
        }
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(30);

        if (player.asWorld().isClient()) {
            player.spawnParticles(MagicParticleEffect.UNICORN, 1);
        }
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {

    }

    private static record TransmutationRecipe(Block input, BlockState output, BlockState material) {
        static final List<TransmutationRecipe> RECIPES = List.of(
                new TransmutationRecipe(Blocks.OAK_SAPLING, UTreeGen.GOLDEN_APPLE_TREE.sapling().get().getDefaultState(), Blocks.RAW_GOLD_BLOCK.getDefaultState()),
                new TransmutationRecipe(Blocks.CARROTS, UBlocks.GOLD_ROOT.getDefaultState(), Blocks.RAW_GOLD_BLOCK.getDefaultState()),
                new TransmutationRecipe(Blocks.CORNFLOWER, UBlocks.CURING_JOKE.getDefaultState(), Blocks.LAPIS_BLOCK.getDefaultState()),
                new TransmutationRecipe(Blocks.WITHER_ROSE, UBlocks.PLUNDER_VINE_BUD.getDefaultState(), Blocks.NETHERRACK.getDefaultState())
        );
        static final int RADIUS = 3;
        static final int SIDE_LENGTH = (2 * RADIUS) + 1;
        static final int AREA = (SIDE_LENGTH * SIDE_LENGTH) - 1;
        static final int MINIMUM_INPUT = 9;

        public boolean matches(World world, BlockPos pos) {
            return world.getBlockState(pos).isOf(input);
        }

        public Result checkPattern(World world, BlockPos pos) {
            BlockPos center = pos.down();
            Set<BlockPos> matches = new HashSet<>();
            for (BlockPos cell : BlockPos.iterateInSquare(center, RADIUS, Direction.EAST, Direction.NORTH)) {
                if (cell.equals(center)) {
                    continue;
                }
                if (!world.getBlockState(cell).equals(material)) {
                    break;
                }
                matches.add(cell.toImmutable());
            }
            return new Result(this, matches);
        }

        public BlockState getResult(World world, BlockPos pos) {
            return StateUtil.copyState(world.getBlockState(pos), output);
        }

        record Result (TransmutationRecipe recipe, Set<BlockPos> matchedLocations) {
            public boolean shoudTransform(Random random) {
                return random.nextInt(TransmutationRecipe.AREA) < matchedLocations().size();
            }
        }
    }

    public interface Growable {
        boolean grow(World world, BlockState state, BlockPos pos);
    }
}
