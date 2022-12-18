package com.minelittlepony.unicopia.ability.data.tree;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.minelittlepony.unicopia.util.PosHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class TreeTypes {
    private static Set<TreeType> entries = new HashSet<>();

    private static final TreeType any1x = createDynamic(false);
    private static final TreeType any2x = createDynamic(true);

    public static void load(Map<Identifier, TreeTypeLoader.TreeTypeDef> types) {
        entries = types.entrySet().stream().map(e -> e.getValue().toTreeType(e.getKey())).collect(Collectors.toSet());
    }

    static TreeType get(BlockState state, BlockPos pos, World world) {
        return entries.stream()
                .filter(type -> type.matches(state))
                .findFirst()
                .map(type -> TreeType.of(type, type.findLeavesType(world, pos)))
                .orElseGet(() -> {
                    if (any1x.matches(state)) {
                        if (PosHelper.any(pos, p -> world.getBlockState(p).isOf(state.getBlock()), PosHelper.HORIZONTAL)) {
                            return any2x;
                        }

                        return any1x;
                    }

                    return TreeType.NONE;
                });
    }

    static TreeType get(BlockState state) {
        return entries.stream()
                .filter(type -> type.matches(state))
                .findFirst()
                .orElse(TreeType.NONE);
    }

    private static TreeType createDynamic(boolean wide) {
        return new TreeType() {
            @Override
            public boolean isLeaves(BlockState state) {
                return (state.isIn(BlockTags.LEAVES) || state.getBlock() instanceof LeavesBlock || entries.stream().anyMatch(t -> t.isLeaves(state))) && TreeTypeImpl.isNonPersistent(state);
            }

            @Override
            public boolean isLog(BlockState state) {
                return state.isIn(BlockTags.LOGS_THAT_BURN) || entries.stream().anyMatch(t -> t.isLog(state));
            }

            @Override
            public ItemStack pickRandomStack(Random random, BlockState state) {
                TreeType type = get(state);
                if (type == TreeType.NONE) {
                    type = get(Blocks.OAK_LOG.getDefaultState());
                }
                return type.pickRandomStack(random, state);
            }

            @Override
            public boolean isWide() {
                return wide;
            }
        };
    }
}
