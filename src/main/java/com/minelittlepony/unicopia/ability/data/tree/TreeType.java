package com.minelittlepony.unicopia.ability.data.tree;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.Weighted;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public interface TreeType {
    TreeType NONE = new TreeTypeImpl(Unicopia.id("none"), false, Set.of(), Set.of(), Weighted.of(), 0, 0.5F);

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

            @Override
            public float leavesRatio() {
                return logs.leavesRatio();
            }
        };
    }

    boolean isLeaves(BlockState state);

    boolean isLog(BlockState state);

    default boolean matches(BlockState state) {
        return isLeaves(state) || isLog(state);
    }

    ItemStack pickRandomStack(Random random, BlockState state);

    boolean isWide();

    /**
     * The minimum leaves to logs ratio for this tree.
     */
    float leavesRatio();

    /**
     * Counts the number of logs and leaves present in the targeted tree.
     */
    default Optional<Tree> collectBlocks(World w, BlockPos pos) {
        if (this == NONE) {
            return Optional.empty();
        }
        TreeTraverser traverser = new TreeTraverser(this);
        return traverser.findBase(w, pos).map(base -> {
            PosHelper.PositionRecord logs = traverser.collectLogs(w, base);
            PosHelper.PositionRecord leaves = traverser.collectLeaves(w, base);
            return logs.size() <= (leaves.size() * leavesRatio()) ? new Tree(logs, leaves) : null;
        });
    }

    record Tree(PosHelper.PositionRecord logs, PosHelper.PositionRecord leaves) { }
}
