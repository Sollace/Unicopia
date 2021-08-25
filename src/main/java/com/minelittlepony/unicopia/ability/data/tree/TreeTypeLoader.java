package com.minelittlepony.unicopia.ability.data.tree;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.Resources;
import com.minelittlepony.unicopia.util.Weighted;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class TreeTypeLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = new Identifier("unicopia", "data/tree_type");

    public static final TreeTypeLoader INSTANCE = new TreeTypeLoader();

    private final Set<TreeType> entries = new HashSet<>();

    private final TreeType any1x = createDynamic(false);
    private final TreeType any2x = createDynamic(true);

    TreeTypeLoader() {
        super(Resources.GSON, "tree_types");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public TreeType get(BlockState state, BlockPos pos, World world) {
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

    public TreeType get(BlockState state) {
        return entries.stream()
                .filter(type -> type.matches(state))
                .findFirst()
                .orElse(TreeType.NONE);
    }

    private TreeType createDynamic(boolean wide) {
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
            public ItemStack pickRandomStack(BlockState state) {
                TreeType type = get(state);
                if (type == TreeType.NONE) {
                    type = get(Blocks.OAK_LOG.getDefaultState());
                }
                return type.pickRandomStack(state);
            }

            @Override
            public boolean isWide() {
                return wide;
            }
        };
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager manager, Profiler profiler) {
        entries.clear();

        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            try {
                TreeTypeDef typeDef = Resources.GSON.fromJson(entry.getValue(), TreeTypeDef.class);

                if (typeDef != null) {
                    entries.add(new TreeTypeImpl(
                            entry.getKey(),
                            typeDef.wideTrunk,
                            typeDef.getWeighted(new Weighted<Supplier<ItemStack>>()),
                            Objects.requireNonNull(typeDef.logs, "TreeType must have logs"),
                            Objects.requireNonNull(typeDef.leaves, "TreeType must have leaves")
                    ));
                }
            } catch (IllegalArgumentException | JsonParseException e) {

            }
        }
    }

    static class TreeTypeDef {
        Set<Identifier> logs;
        Set<Identifier> leaves;
        Set<Drop> drops;
        boolean wideTrunk;

        Weighted<Supplier<ItemStack>> getWeighted(Weighted<Supplier<ItemStack>> weighted) {
            drops.forEach(drop -> drop.appendDrop(weighted));
            return weighted;
        }

        static class Drop {
            int weight;
            Identifier item;

            void appendDrop(Weighted<Supplier<ItemStack>> weighted) {
                Registry.ITEM.getOrEmpty(item).ifPresent(item -> {
                    weighted.put(weight, item::getDefaultStack);
                });
            }
        }
    }
}
