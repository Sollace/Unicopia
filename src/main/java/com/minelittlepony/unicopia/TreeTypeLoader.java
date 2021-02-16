package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minelittlepony.common.util.settings.ToStringAdapter;
import com.minelittlepony.unicopia.util.Weighted;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class TreeTypeLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Identifier ID = new Identifier("unicopia", "data/tree_type");
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new ToStringAdapter<>(Identifier::new))
            .create();

    static final TreeTypeLoader INSTANCE = new TreeTypeLoader();

    private final Set<TreeType> entries = new HashSet<>();

    TreeTypeLoader() {
        super(GSON, "tree_types");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public TreeType get(BlockState state) {
        return entries.stream().filter(type -> type.matches(state)).findFirst().orElse(TreeType.NONE);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager manager, Profiler profiler) {
        entries.clear();

        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            try {
                TreeTypeDef typeDef = GSON.fromJson(entry.getValue(), TreeTypeDef.class);

                if (typeDef != null) {
                    entries.add(new TreeType(
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
