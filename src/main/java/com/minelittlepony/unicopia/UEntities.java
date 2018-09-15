package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.entity.EntityConstructionCloud;
import com.minelittlepony.unicopia.entity.EntityRacingCloud;
import com.minelittlepony.unicopia.entity.EntityWildCloud;
import com.minelittlepony.unicopia.render.RenderCloud;

import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

public class UEntities {
    private static final int BRUSHES_ROYALBLUE = 4286945;
    private static final int BRUSHES_CHARTREUSE = 8388352;

    static void init(IForgeRegistry<EntityEntry> registry) {
        EntityEntry entry = new EntityEntry(EntityCloud.class, "cloud").setRegistryName(Unicopia.MODID, "cloud");

        entry.setEgg(new EntityEggInfo(new ResourceLocation("unicopia", "cloud"), BRUSHES_ROYALBLUE, BRUSHES_CHARTREUSE));

        registry.register(entry);

        registry.register(new EntityEntry(EntityWildCloud.class, "wild_cloud").setRegistryName(Unicopia.MODID, "wild_cloud"));
        registry.register(new EntityEntry(EntityRacingCloud.class, "racing_cloud").setRegistryName(Unicopia.MODID, "racing_cloud"));
        registry.register(new EntityEntry(EntityConstructionCloud.class, "construction_cloud").setRegistryName(Unicopia.MODID, "construction_cloud"));
    }

    static void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCloud.class, manager -> new RenderCloud(manager));
    }
}
