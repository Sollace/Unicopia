package com.minelittlepony.unicopia.item.enchantment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.util.Resources;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class PoisonedJokeEnchantment extends SimpleEnchantment implements IdentifiableResourceReloadListener {
    private static final Identifier ID = new Identifier("unicopia", "data/poisoned_joke_sounds");
    private static final Identifier FILE = new Identifier("unicopia", "poisoned_joke_sounds.json");
    private static final Type TYPE = TypeUtils.parameterize(List.class, Identifier.class);
    private List<SoundEvent> sounds = new ArrayList<>();

    protected PoisonedJokeEnchantment() {
        super(Rarity.VERY_RARE, true, 1, EquipmentSlot.values());
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (sounds.isEmpty() || user.getReferenceWorld().isClient) {
            return;
        }

        int light = user.getReferenceWorld().getLightLevel(user.getEntity().getRootVehicle().getBlockPos());
        Random rng = user.getReferenceWorld().random;
        Data data = user.getEnchants().computeIfAbsent(this, Data::new);

        data.level -= rng.nextFloat() * 0.8F;
        if (rng.nextInt(Math.max(1, (light * 9) + (int)data.level)) == 0) {
            data.level = rng.nextInt(5000);

            user.getReferenceWorld().playSoundFromEntity(
                    null,
                    user.getEntity(),
                    sounds.get(rng.nextInt(sounds.size())), SoundCategory.HOSTILE,
                    0.5F + rng.nextFloat() * 0.5F,
                    0.5F + rng.nextFloat() * 0.5F
            );
        }
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager manager,
            Profiler serverProfiler, Profiler clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {

        return sync.whenPrepared(null).thenRunAsync(() -> {
            clientProfiler.startTick();
            clientProfiler.push("Loading poisoned joke sound options");

            sounds = Resources.getResources(manager, FILE)
                .flatMap(r -> Resources.loadFile(r, TYPE, "Failed to load sounds file at "))
                .distinct()
                .flatMap(this::findSound)
                .collect(Collectors.toList());

            clientProfiler.pop();
            clientProfiler.endTick();
        }, clientExecutor);
    }

    private Stream<SoundEvent> findSound(Identifier id) {
        return Registry.SOUND_EVENT.getOrEmpty(id).map(Stream::of).orElseGet(() -> {
            Unicopia.LOGGER.warn("Could not find sound with id {}", id);
            return Stream.empty();
        });
    }
}
