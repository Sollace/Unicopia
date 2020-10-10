package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.minelittlepony.unicopia.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TreeTraverser {

    public static class Remover {
        /**
         * Removes the tree located at the given position.
         */
        public static void removeTree(World w, BlockPos pos) {
            BlockState log = w.getBlockState(pos);

            if (Measurer.measureTree(w, log, pos) > 0) {
                removeTreePart(w, log, Ascender.ascendTrunk(new HashSet<BlockPos>(), w, pos, log, 0).get(), 0);
            }
        }

        private static void removeTreePart(World w, BlockState log, BlockPos pos, int level) {
            if (level < 10 && isWoodOrLeaf(w, log, pos)) {
                breakBlock(w, pos, level < 5);

                PosHelper.all(pos, p -> {
                    removeTreePart(w, log, p, level + 1);
                }, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
            }
        }
    }

    public static class Ascender {
        /**
         * Locates the top of the tree's trunk. Usually the point where wood meets leaves.
         */
        public static BlockPos ascendTree(World w, BlockState log, BlockPos pos, boolean remove) {
            int breaks = 0;
            while (variantAndBlockEquals(w.getBlockState(pos.up()), log)) {
                if (PosHelper.any(pos, p -> isLeaves(w.getBlockState(p), log), Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                    break;
                }

                if (remove) {
                    breakBlock(w, pos, breaks++ < 10);
                }
                pos = pos.up();
            }
            return pos;
        }

        static Optional<BlockPos> ascendTrunk(Set<BlockPos> done, World w, BlockPos pos, BlockState log, int level) {
            if (level < 3 && !done.contains(pos)) {
                done.add(pos);

                BlockPos.Mutable result = new BlockPos.Mutable();
                result.set(ascendTree(w, log, pos, true));

                PosHelper.all(pos, p -> {
                    if (variantAndBlockEquals(w.getBlockState(pos.east()), log)) {
                        ascendTrunk(done, w, pos.east(), log, level + 1).filter(a -> a.getY() > result.getY()).ifPresent(result::set);
                    }
                }, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH);

                done.add(result.toImmutable());
                return Optional.of(result.toImmutable());
            }
            return Optional.of(pos);
        }
    }

    public static class Descender {
        /**
         * Recursively locates the base of the tree.
         */
        public static Optional<BlockPos> descendTree(World w, BlockState log, BlockPos pos) {
            return descendTreePart(new HashSet<BlockPos>(), w, log, new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ()));
        }

        private static Optional<BlockPos> descendTreePart(Set<BlockPos> done, World w, BlockState log, BlockPos.Mutable pos) {
            if (done.contains(pos) || !variantAndBlockEquals(w.getBlockState(pos), log)) {
                return Optional.empty();
            }

            done.add(pos.toImmutable());
            while (variantAndBlockEquals(w.getBlockState(pos.down()), log)) {
                done.add(pos.move(Direction.DOWN).toImmutable());
            }

            PosHelper.all(pos.toImmutable(), p -> {
                descendTreePart(done, w, log, new BlockPos.Mutable(p.getX(), p.getY(), p.getZ())).filter(a -> a.getY() < pos.getY()).ifPresent(pos::set);
            }, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

            done.add(pos.toImmutable());
            return Optional.of(pos.toImmutable());
        }
    }

    public static class Measurer {
        /**
         * Counts the number of logs and leaves present in the targeted tree.
         */
        public static int measureTree(World w, BlockState log, BlockPos pos) {
            Set<BlockPos> logs = new HashSet<>();
            Set<BlockPos> leaves = new HashSet<>();

            countParts(logs, leaves, w, log, pos);

            return logs.size() <= (leaves.size() / 2) ? logs.size() + leaves.size() : 0;
        }

        private static void countParts(Set<BlockPos> logs, Set<BlockPos> leaves, World w, BlockState log, BlockPos pos) {
            if (logs.contains(pos) || leaves.contains(pos)) {
                return;
            }

            BlockState state = w.getBlockState(pos);
            boolean yay = false;

            if (isLeaves(state, log) && !state.get(LeavesBlock.PERSISTENT)) {
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
    }

    private static void breakBlock(World w, BlockPos pos, boolean destroy) {
        if (destroy) {
            w.breakBlock(pos, true);
        } else {
            Block.dropStacks(w.getBlockState(pos), w, pos);
            w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    public static boolean isWoodOrLeaf(World w, BlockState log, BlockPos pos) {
        BlockState state = w.getBlockState(pos);
        return variantAndBlockEquals(state, log) || (isLeaves(state, log) && !state.get(LeavesBlock.PERSISTENT));
    }

    private static boolean isLeaves(BlockState state, BlockState log) {
        return state.getBlock() instanceof LeavesBlock && variantEquals(state, log);
    }

    private static boolean variantAndBlockEquals(BlockState one, BlockState two) {
        return (one.getBlock() == two.getBlock()) && variantEquals(one, two);
    }

    private static boolean variantEquals(BlockState one, BlockState two) {
        return TreeType.get(one).equals(TreeType.get(two));
    }
}
