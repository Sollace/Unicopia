package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public abstract class EntityBehaviour<T extends Entity> {

    private static final Registry<EntityBehaviour<?>> REGISTRY = Registries.createSimple(new Identifier("unicopia", "entity_behaviour"));

    public abstract void update(Caster<?> source, T entity);

    public void onCreate(T entity) {
        entity.extinguish();
    }

    public static <T extends Entity> void register(Supplier<EntityBehaviour<T>> behaviour, EntityType<?>... types) {
        for (EntityType<?> type : types) {
            Registry.register(REGISTRY, EntityType.getId(type), behaviour.get());
        }
    }

    public static Optional<EntityBehaviour<?>> forEntity(@Nullable Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        return REGISTRY.getOrEmpty(EntityType.getId(entity.getType()));
    }

    static {
        register(ShulkerBehaviour::new, EntityType.SHULKER);
        register(CreeperBehaviour::new, EntityType.CREEPER);
        register(MinecartBehaviour::new, EntityType.CHEST_MINECART, EntityType.COMMAND_BLOCK_MINECART, EntityType.FURNACE_MINECART, EntityType.HOPPER_MINECART, EntityType.MINECART, EntityType.SPAWNER_MINECART, EntityType.TNT_MINECART);
    }
}
