package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UEntities {

    EntityType<MagicProjectileEntity> THROWN_ITEM = register("thrown_item", FabricEntityTypeBuilder.<MagicProjectileEntity>create(SpawnGroup.MISC, MagicProjectileEntity::new)
            .trackable(100, 2)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));

    static <T extends Entity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        return Registry.register(Registry.ENTITY_TYPE, new Identifier("unicopia", name), type);
    }

    static void bootstrap() {}
}
