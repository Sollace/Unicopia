package com.minelittlepony.unicopia.ability.data.tree;

import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.Weighted;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public final class TreeType {
    public static final TreeType NONE = new TreeType(
            new Identifier("unicopia", "none"),
            false,
            new Weighted<Supplier<ItemStack>>(),
            Set.of(),
            Set.of()
    );
    private static final Direction[] WIDE_DIRS = new Direction[] { Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

    private final Identifier name;
    private final boolean wideTrunk;
    private final Set<Identifier> logs;
    private final Set<Identifier> leaves;
    private final Weighted<Supplier<ItemStack>> pool;

    TreeType(Identifier name, boolean wideTrunk, Weighted<Supplier<ItemStack>> pool, Set<Identifier> logs, Set<Identifier> leaves) {
        this.name = name;
        this.wideTrunk = wideTrunk;
        this.pool = pool;
        this.logs = logs;
        this.leaves = leaves;
    }

    public void traverse(World w, BlockPos start, Reactor consumer) {
        traverse(w, start, consumer, consumer);
    }

    public void traverse(World w, BlockPos start, Reactor logConsumer, Reactor leavesConsumer) {
        traverse(new HashSet<>(), new HashSet<>(), w, start, 0, 50, logConsumer, leavesConsumer);
    }

    public void traverse(Set<BlockPos> logs, Set<BlockPos> leaves, World w, BlockPos start, int recurseLevel, int maxRecurse, Reactor logConsumer, Reactor leavesConsumer) {
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
    public Optional<BlockPos> findBase(World w, BlockPos pos) {
        return findBase(new HashSet<BlockPos>(), w, new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ()));
    }

    private Optional<BlockPos> findBase(Set<BlockPos> done, World w, BlockPos.Mutable pos) {
        if (done.contains(pos) || !isLog(w.getBlockState(pos))) {
            return Optional.empty();
        }

        done.add(pos.toImmutable());
        while (isLog(w.getBlockState(pos.down()))) {
            done.add(pos.move(Direction.DOWN).toImmutable());
        }

        if (wideTrunk) {
            PosHelper.all(pos.toImmutable(), p -> findBase(done, w, new BlockPos.Mutable(p.getX(), p.getY(), p.getZ()))
                    .filter(a -> a.getY() < pos.getY())
                    .ifPresent(pos::set), PosHelper.HORIZONTAL);
        }

        done.add(pos.toImmutable());
        return Optional.of(pos.toImmutable());
    }

    /**
     * Counts the number of logs and leaves present in the targeted tree.
     */
    public int countBlocks(World w, BlockPos pos) {
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

        return logCount <= (leaves.size() / 2) ? logCount + leaves.size() : 0;
    }

    /**
     * Locates the top of the tree's trunk. Usually the point where wood meets leaves.
     */
    public BlockPos findCanopy(World w, BlockPos pos) {
        while (isLog(w.getBlockState(pos.up()))) {
            if (PosHelper.any(pos, p -> isLeaves(w.getBlockState(p)), PosHelper.HORIZONTAL)) {
                break;
            }

            pos = pos.up();
        }
        return pos;
    }

    public boolean isLeaves(BlockState state) {
        return findMatch(leaves, state) && (!state.contains(LeavesBlock.PERSISTENT) || !state.get(LeavesBlock.PERSISTENT));
    }

    public boolean isLog(BlockState state) {
        return findMatch(logs, state);
    }

    public boolean matches(BlockState state) {
        return isLeaves(state) || isLog(state);
    }

    public ItemStack pickRandomStack() {
        return pool.get().map(Supplier::get).orElse(ItemStack.EMPTY);
    }

    public static TreeType get(BlockState state) {
        return TreeTypeLoader.INSTANCE.get(state);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TreeType && name.compareTo(((TreeType)o).name) == 0;
    }

    private static boolean findMatch(Set<Identifier> ids, BlockState state) {
        return ids.contains(Registry.BLOCK.getId(state.getBlock()));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public interface Reactor {
        void react(World w, BlockState state, BlockPos pos, int recurseLevel);
    }
}
