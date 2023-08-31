package com.minelittlepony.unicopia.ability.data.tree;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.Weighted;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public interface TreeType {
    TreeType NONE = new TreeTypeImpl(
            Unicopia.id("none"),
            false,
            Set.of(),
            Set.of(),
            Weighted.of(),
            0
    );
    Direction[] WIDE_DIRS = new Direction[] { Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

    default void traverse(World w, BlockPos start, Reactor consumer) {
        traverse(w, start, consumer, consumer);
    }

    default void traverse(World w, BlockPos start, Reactor logConsumer, Reactor leavesConsumer) {
        traverse(new HashSet<>(), new HashSet<>(), w, start, 0, 50, logConsumer, leavesConsumer);
    }

    default void traverse(Set<BlockPos> logs, Set<BlockPos> leaves, World w, BlockPos start, int recurseLevel, int maxRecurse, Reactor logConsumer, Reactor leavesConsumer) {
        if (this == NONE) {
            return;
        }

        findBase(w, start).ifPresent(base -> {
            traverseInner(logs, leaves, w, base, recurseLevel, maxRecurse, logConsumer, leavesConsumer);
        });
    }

    private void traverseInner(Set<BlockPos> logs, Set<BlockPos> leaves, World w, BlockPos pos, int recurseLevel, int maxRecurse, Reactor logConsumer, Reactor leavesConsumer) {

        if (this == NONE || (maxRecurse > 0 && recurseLevel >= maxRecurse) || logs.contains(pos) || leaves.contains(pos)) {
            return;
        }

        BlockState state = w.getBlockState(pos);
        boolean yay = false;

        if (isLeaves(state)) {
            leaves.add(pos);
            yay = true;
            if (leavesConsumer != null) {
                leavesConsumer.react(w, state, pos, recurseLevel);
            }
        } else if (isLog(state)) {
            logs.add(pos);
            yay = true;
            if (logConsumer != null) {
                logConsumer.react(w, state, pos, recurseLevel);
            }
        }

        if (yay) {
            PosHelper.all(pos, p -> traverseInner(logs, leaves, w, p, recurseLevel + 1, maxRecurse, logConsumer, leavesConsumer), WIDE_DIRS);
        }
    }

    /**
     * Recursively locates the base of the tree.
     */
    default Optional<BlockPos> findBase(World w, BlockPos pos) {
        return findBase(new HashSet<>(), w, new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ()));
    }

    private Optional<BlockPos> findBase(Set<BlockPos> done, World w, BlockPos.Mutable pos) {
        if (done.contains(pos) || !isLog(w.getBlockState(pos))) {
            return Optional.empty();
        }

        done.add(pos.toImmutable());
        while (isLog(w.getBlockState(pos.down()))) {
            done.add(pos.move(Direction.DOWN).toImmutable());
        }

        if (isWide()) {
            PosHelper.all(pos.toImmutable(), p -> findBase(done, w, p.mutableCopy())
                    .filter(a -> a.getY() < pos.getY())
                    .ifPresent(pos::set), PosHelper.HORIZONTAL);
        }

        done.add(pos.toImmutable());
        return Optional.of(pos.toImmutable());
    }

    /**
     * Counts the number of logs and leaves present in the targeted tree.
     */
    default int countBlocks(World w, BlockPos pos) {
        if (this == NONE) {
            return 0;
        }

        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();

        findBase(w, pos).ifPresent(base -> traverseInner(logs, leaves, w, base, 0, 50, null, null));

        int logCount = logs.size();

        logs.clear();
        leaves.clear();

        traverseInner(logs, leaves, w, findCanopy(w, pos), 0, 50, null, null);

        int leafCount = leaves.size();

        return logCount <= (leafCount / 2) ? logCount + leafCount : 0;
    }

    /**
     * Locates the top of the tree's trunk. Usually the point where wood meets leaves.
     */
    default BlockPos findCanopy(World w, BlockPos pos) {
        while (isLog(w.getBlockState(pos.up()))) {
            if (PosHelper.any(pos, p -> isLeaves(w.getBlockState(p)), PosHelper.HORIZONTAL)) {
                break;
            }

            pos = pos.up();
        }
        return pos;
    }

    /**
     * Finds the tree type of the leaves on top of this tree, independent of what this tree expects for its leaves.
     */
    default TreeType findLeavesType(World w, BlockPos pos) {
        while (isLog(w.getBlockState(pos.up()))) {
            if (PosHelper.any(pos, p -> isLeaves(w.getBlockState(p)), PosHelper.HORIZONTAL)) {
                return this;
            }

            pos = pos.up();
        }

        return of(w.getBlockState(pos.up()));
    }

    boolean isLeaves(BlockState state);

    boolean isLog(BlockState state);

    default boolean matches(BlockState state) {
        return isLeaves(state) || isLog(state);
    }

    ItemStack pickRandomStack(Random random, BlockState state);

    boolean isWide();

    static TreeType at(BlockPos pos, World world) {
        return TreeTypes.get(world.getBlockState(pos), pos, world);
    }

    static TreeType of(BlockState state) {
        return TreeTypes.get(state);
    }

    static TreeType of(TreeType logs, TreeType leaves) {
        if (logs == NONE || leaves == NONE || Objects.equals(logs, leaves)) {
            return logs;
        }
        return new TreeType() {
            @Override
            public boolean isLeaves(BlockState state) {
                return leaves.isLeaves(state);
            }

            @Override
            public boolean isLog(BlockState state) {
                return logs.isLog(state);
            }

            @Override
            public ItemStack pickRandomStack(Random random, BlockState state) {
                return (isLeaves(state) ? leaves : logs).pickRandomStack(random, state);
            }

            @Override
            public boolean isWide() {
                return logs.isWide();
            }
        };
    }

    public interface Reactor {
        void react(World w, BlockState state, BlockPos pos, int recurseLevel);
    }
}
