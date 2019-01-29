package com.minelittlepony.unicopia;

import java.util.List;

import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.entity.EntityConstructionCloud;
import com.minelittlepony.unicopia.entity.EntityRacingCloud;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.EntitySpellbook;
import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.entity.EntityWildCloud;
import com.minelittlepony.unicopia.render.RenderCloud;
import com.minelittlepony.unicopia.render.RenderGem;
import com.minelittlepony.unicopia.render.RenderProjectile;
import com.minelittlepony.unicopia.render.RenderSpellbook;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraft.world.biome.BiomeHell;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

public class UEntities {
    private static final int BRUSHES_ROYALBLUE = 0x4169E1;
    private static final int BRUSHES_CHARTREUSE = 0x7FFF00;

    static void init(IForgeRegistry<EntityEntry> registry) {
        registry.registerAll(
            new Entry(EntityCloud.class, "cloud").withEgg(BRUSHES_ROYALBLUE, BRUSHES_CHARTREUSE),
            new Entry(EntityWildCloud.class, "wild_cloud"),
            new Entry(EntityRacingCloud.class, "racing_cloud"),
            new Entry(EntityConstructionCloud.class, "construction_cloud"),
            new Entry(EntitySpell.class, "magic_spell"),
            new Entry(EntitySpellbook.class, "spellbook"),
            EntityEntryBuilder.<EntityProjectile>create().entity(EntityProjectile.class).name("thrown_item").id(new ResourceLocation(Unicopia.MODID, "thrown_item"), 0).tracker(10, 5, true).build()
        );
    }

    static void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCloud.class, RenderCloud::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpell.class, RenderGem::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectile.class, RenderProjectile::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellbook.class, RenderSpellbook::new);
    }

    static void registerSpawnEntries(Biome biome) {

        if (!(biome instanceof BiomeHell || biome instanceof BiomeEnd)) {
            List<SpawnListEntry> entries = biome.getSpawnableList(EnumCreatureType.AMBIENT);
            entries.stream().filter(p -> p.entityClass == EntityWildCloud.class).findFirst().orElseGet(() -> {
                entries.add(
                        BiomeManager.oceanBiomes.contains(biome) ?
                                EntityWildCloud.SPAWN_ENTRY_LAND : EntityWildCloud.SPAWN_ENTRY_OCEAN
                );
                return null;
            });
        }
    }

    static class Entry extends EntityEntry {

        public Entry(Class<? extends Entity> cls, String name) {
            super(cls, name);
            setRegistryName(Unicopia.MODID, name);
        }

        Entry withEgg(int a, int b) {
            setEgg(new EntityEggInfo(getRegistryName(), a, b));

            return this;
        }
    }
}
