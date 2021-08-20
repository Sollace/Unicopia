package com.minelittlepony.unicopia.item.enchantment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.entity.Living;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class PoisonedJokeEnchantment extends SimpleEnchantment implements IdentifiableResourceReloadListener {
    private static final Identifier ID = new Identifier("unicopia", "data/poisoned_joke_sounds");
    private static final Identifier FILE = new Identifier("unicopia", "poisoned_joke_sounds.json");
    private static final Type TYPE = TypeUtils.parameterize(List.class, Identifier.class);
    private List<SoundEvent> sounds = new ArrayList<>();

    protected PoisonedJokeEnchantment() {
        super(Rarity.COMMON, true, 1, EquipmentSlot.values());
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (sounds.isEmpty() || user.getWorld().isClient) {
            return;
        }

        int light = user.getWorld().getLightLevel(user.getEntity().getRootVehicle().getBlockPos());
        Random rng = user.getWorld().random;
        Data data = user.getEnchants().computeIfAbsent(this, Data::new);

        data.level -= rng.nextFloat() * 0.8F;
        if (rng.nextInt(Math.max(1, (light * 9) + (int)data.level)) == 0) {
            data.level = rng.nextInt(5000);

            Unicopia.LOGGER.info("Boo!");
            user.getWorld().playSoundFromEntity(
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


            sounds = getResources(manager, FILE)
                .flatMap(this::loadFile)
                .distinct()
                .flatMap(this::findSound)
                .collect(Collectors.toList());

            clientProfiler.pop();
            clientProfiler.endTick();
        }, clientExecutor);
    }

    private Stream<Resource> getResources(ResourceManager manager, Identifier id) {
        try {
            return manager.getAllResources(id).stream();
        } catch (IOException ignored) { }
        return Stream.empty();
    }

    private Stream<Identifier> loadFile(Resource res) throws JsonParseException {
        try (Resource resource = res) {
            return (TreeTypeLoader.GSON.<List<Identifier>>fromJson(new InputStreamReader(resource.getInputStream()), TYPE)).stream();
        } catch (JsonParseException e) {
            Unicopia.LOGGER.warn("Failed to load sounds file at " + res.getResourcePackName(), e);
        } catch (IOException ignored) {}

        return Stream.empty();
    }

    private Stream<SoundEvent> findSound(Identifier id) {
        SoundEvent value = Registry.SOUND_EVENT.getOrEmpty(id).orElse(null);
        if (value == null) {
            Unicopia.LOGGER.warn("Could not find sound with id {}", id);
            return Stream.empty();
        }
        return Stream.of(value);
    }
}
