package com.minelittlepony.unicopia.server.world.gen;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.mixin.server.MixinStructurePool;
import com.minelittlepony.unicopia.mixin.server.MixinStructureProcessorList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.registry.RegistryKeys;

public interface StructureExtensions {
    static void bootstrap() {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.registerEntryAdded(RegistryKeys.TEMPLATE_POOL, new DynamicEntryMerger<>(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, biMapped(MixinStructurePool.class::cast, (source, target) -> {
                target.setElements(new ObjectArrayList<>(union(target.getElements(), source.getElements())));
                target.setElementCounts(union(target.getElementCounts(), source.getElementCounts()));
            })));

            registries.registerEntryAdded(RegistryKeys.PROCESSOR_LIST, new DynamicEntryMerger<>(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, (source, target) -> {
                ((MixinStructureProcessorList)target).setList(union(source.getList(), target.getList()));
            }));
        });
    }

    static <T> List<T> union(List<T> a, List<T> b) {
        return Stream.concat(a.stream(), b.stream()).toList();
    }

    static <T, X> BiConsumer<T, T> biMapped(Function<T, X> remapper, BiConsumer<X, X> consumer) {
        return (a, b) -> consumer.accept(remapper.apply(a), remapper.apply(b));
    }
}