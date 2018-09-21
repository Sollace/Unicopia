package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.entity.EntityConstructionCloud;
import com.minelittlepony.unicopia.entity.EntityRacingCloud;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.EntityWildCloud;
import com.minelittlepony.unicopia.render.RenderCloud;
import com.minelittlepony.unicopia.render.RenderGem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

public class UEntities {
    private static final int BRUSHES_ROYALBLUE = 0x4169E1;
    private static final int BRUSHES_CHARTREUSE = 0x7FFF00;

    static void init(IForgeRegistry<EntityEntry> registry) {
        addEntity(registry, EntityCloud.class, "cloud", true, BRUSHES_ROYALBLUE, BRUSHES_CHARTREUSE);
        addEntity(registry, EntityWildCloud.class, "wild_cloud", false, 0, 0);
        addEntity(registry, EntityRacingCloud.class, "racing_cloud", false, 0, 0);
        addEntity(registry, EntityConstructionCloud.class, "construction_cloud", false, 0, 0);
        addEntity(registry, EntitySpell.class, "magic_spell", false, 0, 0);
    }

    static <T extends Entity> void addEntity(IForgeRegistry<EntityEntry> registry, Class<T> type, String name, boolean egg, int a, int b) {
        EntityEntry entry = new EntityEntry(type, name).setRegistryName(Unicopia.MODID, name);

        if (egg) {
            entry.setEgg(new EntityEggInfo(new ResourceLocation("unicopia", "cloud"), a, a));
        }

        registry.register(entry);
    }

    static void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCloud.class, manager -> new RenderCloud(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpell.class, manager -> new RenderGem(manager));
    }
}
