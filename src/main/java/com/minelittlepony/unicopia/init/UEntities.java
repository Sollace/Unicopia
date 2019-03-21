package com.minelittlepony.unicopia.init;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.EntityButterfly;
import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.entity.EntityConstructionCloud;
import com.minelittlepony.unicopia.entity.EntityCuccoon;
import com.minelittlepony.unicopia.entity.EntityRacingCloud;
import com.minelittlepony.unicopia.entity.EntityRainbow;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.EntitySpellbook;
import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.entity.EntityWildCloud;
import com.minelittlepony.unicopia.forgebullshit.BiomeBS;
import com.minelittlepony.unicopia.forgebullshit.EntityType;
import com.minelittlepony.unicopia.render.RenderButterfly;
import com.minelittlepony.unicopia.render.RenderCloud;
import com.minelittlepony.unicopia.render.RenderCuccoon;
import com.minelittlepony.unicopia.render.RenderGem;
import com.minelittlepony.unicopia.render.RenderProjectile;
import com.minelittlepony.unicopia.render.RenderRainbow;
import com.minelittlepony.unicopia.render.RenderSpellbook;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraft.world.biome.BiomeForest;
import net.minecraft.world.biome.BiomeHell;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomePlains;
import net.minecraft.world.biome.BiomeRiver;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

public class UEntities {

    static void init(IForgeRegistry<EntityEntry> registry) {
        EntityType builder = EntityType.builder(Unicopia.MODID);
        registry.registerAll(
                builder.creature(EntityCloud.class, "cloud").withEgg(0x4169e1, 0x7fff00),
                builder.creature(EntityWildCloud.class, "wild_cloud"),
                builder.creature(EntityRacingCloud.class, "racing_cloud"),
                builder.creature(EntityConstructionCloud.class, "construction_cloud"),
                builder.creature(EntitySpell.class, "magic_spell"),
                builder.creature(EntitySpellbook.class, "spellbook"),
                builder.creature(EntityRainbow.Spawner.class, "rainbow_spawner"),
                builder.creature(EntityCuccoon.class, "cuccoon"),
                builder.creature(EntityButterfly.class, "butterfly").withEgg(0x222200, 0xaaeeff),
                builder.projectile(EntityRainbow.class, "rainbow", 500, 5),
                builder.projectile(EntityProjectile.class, "thrown_item", 100, 10)
        );
    }

    public static void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCloud.class, RenderCloud::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpell.class, RenderGem::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectile.class, RenderProjectile::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellbook.class, RenderSpellbook::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRainbow.class, RenderRainbow::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityButterfly.class, RenderButterfly::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCuccoon.class, RenderCuccoon::new);
    }

    public static void registerSpawnEntries(Biome biome) {
        if (!(biome instanceof BiomeHell || biome instanceof BiomeEnd)) {

            BiomeBS.addSpawnEntry(biome, EnumCreatureType.AMBIENT, EntityWildCloud.class, b ->
                BiomeManager.oceanBiomes.contains(b) ? EntityWildCloud.SPAWN_ENTRY_LAND : EntityWildCloud.SPAWN_ENTRY_OCEAN
            );

            BiomeBS.addSpawnEntry(biome, EnumCreatureType.CREATURE, EntityRainbow.Spawner.class, b -> EntityRainbow.SPAWN_ENTRY);
        }

        if (biome instanceof BiomePlains
            || biome instanceof BiomeRiver
            || biome instanceof BiomeHills
            || biome instanceof BiomeForest) {
            BiomeBS.addSpawnEntry(biome, EnumCreatureType.AMBIENT, EntityButterfly.class, b -> EntityButterfly.SPAWN_ENTRY);
        }
    }
}
