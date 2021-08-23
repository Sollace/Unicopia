package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.FloatingArtefactEntity;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;

@SuppressWarnings("deprecation")
public interface UEntities {

    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, ButterflyEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<MagicProjectileEntity> THROWN_ITEM = register("thrown_item", FabricEntityTypeBuilder.<MagicProjectileEntity>create(SpawnGroup.MISC, MagicProjectileEntity::new)
            .trackRangeBlocks(100)
            .trackedUpdateRate(2)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));
    EntityType<FloatingArtefactEntity> FLOATING_ARTEFACT = register("floating_artefact", FabricEntityTypeBuilder.create(SpawnGroup.MISC, FloatingArtefactEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.fixed(1, 1)));
    EntityType<CastSpellEntity> CAST_SPELL = register("cast_spell", FabricEntityTypeBuilder.create(SpawnGroup.MISC, CastSpellEntity::new)
            .trackRangeBlocks(200)
            .dimensions(EntityDimensions.fixed(1, 1)));

    static <T extends Entity> EntityType<T> register(String name, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        return Registry.register(Registry.ENTITY_TYPE, new Identifier("unicopia", name), type);
    }

    static void bootstrap() {
        FabricDefaultAttributeRegistry.register(BUTTERFLY, ButterflyEntity.createButterflyAttributes());

        final Predicate<BiomeSelectionContext> butterflySpawnable = BiomeSelectors.foundInOverworld()
                .and(ctx -> ctx.getBiome().getPrecipitation() == Biome.Precipitation.RAIN);

        BiomeModifications.addSpawn(butterflySpawnable.and(BiomeSelectors.categories(Category.RIVER, Category.FOREST, Category.EXTREME_HILLS)), SpawnGroup.AMBIENT, BUTTERFLY, 3, 3, 12);
        BiomeModifications.addSpawn(butterflySpawnable.and(BiomeSelectors.categories(Category.PLAINS, Category.JUNGLE)), SpawnGroup.AMBIENT, BUTTERFLY, 7, 5, 19);

        TradeOfferHelper.registerVillagerOffers(VillagerProfession.MASON, 1, factories -> {
            factories.add((e, rng) -> new TradeOffer(UItems.GEMSTONE.getDefaultStack(), Items.EMERALD.getDefaultStack(), 30, 2, 0.05F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 1, factories -> {
            factories.add((e, rng) -> new TradeOffer(new ItemStack(UItems.GEMSTONE, 2), Items.EMERALD.getDefaultStack(), 20, 1, 0.05F));
            factories.add((e, rng) -> new TradeOffer(new ItemStack(UItems.GEMSTONE, 30), UItems.GOLDEN_FEATHER.getDefaultStack(), UItems.GOLDEN_WING.getDefaultStack(), 30, 2, 0.05F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, 1, factories -> {
            factories.add((e, rng) -> new TradeOffer(new ItemStack(UItems.GEMSTONE, 3), Items.EMERALD.getDefaultStack(), 20, 1, 0.05F));
        });
    }
}
