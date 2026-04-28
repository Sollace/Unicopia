package com.minelittlepony.unicopia.datagen.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import com.google.common.base.Strings;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.item.BalloonDesignProperty;
import com.minelittlepony.unicopia.client.render.item.ButterflyVariantProperty;
import com.minelittlepony.unicopia.client.render.item.GemShapeProperty;
import com.minelittlepony.unicopia.client.render.item.PolearmModelRenderer;
import com.minelittlepony.unicopia.client.render.item.SpellTintSource;
import com.minelittlepony.unicopia.client.render.item.ZapAppleCycleProperty;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.item.GemstoneItem;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.RangeDispatchItemModel;
import net.minecraft.client.render.item.model.SelectItemModel;
import net.minecraft.client.render.item.property.bool.FishingRodCastProperty;
import net.minecraft.client.render.item.property.numeric.DamageProperty;
import net.minecraft.client.render.item.property.numeric.TimeProperty;
import net.minecraft.client.render.item.property.select.DisplayContextProperty;
import net.minecraft.client.render.item.tint.DyeTintSource;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import static net.minecraft.client.data.ItemModels.*;

interface ItemModels {
    Model GENERATED = net.minecraft.client.data.Models.GENERATED;
    Model CHEST = item(Identifier.ofVanilla("chest"), TextureKey.PARTICLE);
    Model BUILTIN_ENTITY = new Model(Optional.of(Identifier.ofVanilla("builtin/entity")), Optional.empty());
    Model TEMPLATE_AMULET = item("template_amulet", TextureKey.LAYER0);
    Model TEMPLATE_EYEWEAR = item("template_eyewear", TextureKey.LAYER0);
    Model TEMPLATE_SPAWN_EGG = item(Identifier.ofVanilla("template_spawn_egg"));
    Model TEMPLATE_MUG = item("template_mug", TextureKey.LAYER0);
    Model TEMPLATE_PILLAR = item("template_pillar", TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.END);
    Model HANDHELD_STAFF = item("handheld_staff", TextureKey.LAYER0);
    Model TRIDENT_THROWING = item(Identifier.ofVanilla("trident_throwing"), TextureKey.LAYER0);
    Model TRIDENT_IN_HAND = item(Identifier.ofVanilla("trident_in_hand"), TextureKey.LAYER0);

    static Model item(String parent, TextureKey ... requiredTextureKeys) {
        return item(Unicopia.id(parent), requiredTextureKeys);
    }

    static Model item(Identifier parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("item/")), Optional.empty(), requiredTextureKeys);
    }

    static TintSource spellTintSource(int defaultColor) {
        return new SpellTintSource(defaultColor);
    }

    static void register(ItemModelGenerator itemModelGenerator, Item... items) {
        register(itemModelGenerator, GENERATED, items);
    }

    static void register(ItemModelGenerator itemModelGenerator, Model model, Item... items) {
        for (Item item : items) {
            itemModelGenerator.register(item, model);
        }
    }

    static void registerDyeable(ItemModelGenerator itemModelGenerator, Item item, int defaultColor) {
        itemModelGenerator.output.accept(item, tinted(itemModelGenerator.upload(item, GENERATED), new DyeTintSource(defaultColor)));
    }

    static void registerParented(ItemModelGenerator itemModelGenerator, Item item, ItemConvertible parent) {
        ItemModels.item(Registries.ITEM.getId(parent.asItem())).upload(ModelIds.getItemModelId(item), new TextureMap(), itemModelGenerator.modelCollector);
    }

    static void registerPolearm(ItemModelGenerator itemModelGenerator, Item item) {
        TextureMap textures = TextureMap.layer0(TextureMap.getId(item));

        var inInventory = basic(GENERATED.upload(ModelIds.getItemSubModelId(item, "_in_inventory"), textures, itemModelGenerator.modelCollector));
        var throwing = TRIDENT_THROWING.upload(ModelIds.getItemSubModelId(item, "_throwing"), textures, itemModelGenerator.modelCollector);
        var inHand = TRIDENT_IN_HAND.upload(ModelIds.getItemModelId(item), textures, itemModelGenerator.modelCollector);

        itemModelGenerator.output.accept(item, select(new DisplayContextProperty(), condition(usingItemProperty(),
                special(throwing, new PolearmModelRenderer.Unbaked(ModelIds.getItemModelId(item))),
                special(inHand, new PolearmModelRenderer.Unbaked(ModelIds.getItemModelId(item)))
        ), switchCase(List.of(ItemDisplayContext.GUI, ItemDisplayContext.GROUND, ItemDisplayContext.FIXED), inInventory)));
    }

    @SuppressWarnings("unchecked")
    static void registerButterfly(ItemModelGenerator itemModelGenerator, Item item) {
        itemModelGenerator.output.accept(item, select(
                ButterflyVariantProperty.INSTANCE,
                createVariantItemModel(item, itemModelGenerator, ButterflyEntity.Variant.BUTTERFLY),
                Arrays.stream(ButterflyEntity.Variant.VALUES)
                    .map(variant -> switchCase(variant, createVariantItemModel(item, itemModelGenerator, variant)))
                    .toArray(SelectItemModel.SwitchCase[]::new)));
    }

    static void registerStagedFoodItem(ItemModelGenerator itemModelGenerator, Item item, int minBites, int maxBites, String suffex) {
        itemModelGenerator.output.accept(item, rangeDispatch(new DamageProperty(true),
                basic(itemModelGenerator.upload(item, GENERATED)),
                IntStream.range(minBites, maxBites + 1)
                    .mapToObj(count -> rangeDispatchEntry(basic(itemModelGenerator.registerSubModel(item, "_" + suffex + count, GENERATED)), count / (maxBites + 1F)))
                    .toList()
        ));
    }

    static void registerMagicStaff(ItemModelGenerator itemModelGenerator, Item item) {
        var base = basic(itemModelGenerator.registerSubModel(item, "_base", HANDHELD_STAFF));
        var magicOverlay = tinted(itemModelGenerator.registerSubModel(item, "_magic", HANDHELD_STAFF), spellTintSource(Colors.WHITE));
        itemModelGenerator.output.accept(item, composite(base, magicOverlay));
    }

    static void registerGemstone(ItemModelGenerator itemModelGenerator, Item item) {
        itemModelGenerator.output.accept(item, select(new GemShapeProperty(),
            tinted(itemModelGenerator.upload(item, GENERATED), spellTintSource(Colors.WHITE)),
            Arrays.stream(GemstoneItem.Shape.values())
                .map(shape -> switchCase(shape, tinted(itemModelGenerator.registerSubModel(item, "_" + shape.asString(), GENERATED), spellTintSource(Colors.WHITE))))
                .toList()
        ));
    }

    @SuppressWarnings("unchecked")
    static void registerBalloonDesigns(ItemModelGenerator itemModelGenerator, Item item) {
        itemModelGenerator.output.accept(item, select(
                BalloonDesignProperty.INSTANCE,
                createVariantItemModel(item, itemModelGenerator, AirBalloonEntity.BalloonDesign.NONE),
                Arrays.stream(AirBalloonEntity.BalloonDesign.VALUES)
                    .map(variant -> switchCase(variant, createVariantItemModel(item, itemModelGenerator, variant)))
                    .toArray(SelectItemModel.SwitchCase[]::new)));
    }

    static void registerCustomFishingRod(ItemModelGenerator itemModelGenerator, Item item) {
        itemModelGenerator.registerCondition(item, new FishingRodCastProperty(),
                basic(ModelIds.getItemSubModelId(Items.FISHING_ROD, "_cast")),
                basic(itemModelGenerator.upload(item, Models.HANDHELD_ROD))
        );
    }

    private static <T extends StringIdentifiable> ItemModel.Unbaked createVariantItemModel(Item item, ItemModelGenerator itemModelGenerator, T variant) {
        String name = variant.asString();
        Identifier subModelId = Registries.ITEM.getId(item).withPath(p -> "item/" + name + "_" + p);
        return basic(GENERATED.upload(subModelId, TextureMap.layer0(subModelId), itemModelGenerator.modelCollector));
    }

    static void registerSpectralClock(ItemModelGenerator itemModelGenerator, Item clock) {
        List<RangeDispatchItemModel.Entry> list = new ArrayList<>();
        ItemModel.Unbaked defaultModel = basic(itemModelGenerator.registerSubModel(clock, "_00", Models.GENERATED));
        list.add(rangeDispatchEntry(defaultModel, 0F));

        String[] suffexes = { "", "_greening", "_flowering", "_fruiting", "_ripe", "" };

        for (int index = 1; index < 40; index++) {
            Identifier subModelId = itemModelGenerator.registerSubModel(clock, suffexes[index / 8] + "_" + Strings.padStart((index % 8) * 5 + "", 2, '0'), Models.GENERATED);
            list.add(rangeDispatchEntry(basic(subModelId), index / 40F - 0.5F));
        }

        list.add(rangeDispatchEntry(defaultModel, 1F));
        itemModelGenerator.output.accept(
                clock,
                overworldSelect(
                    rangeDispatch(new ZapAppleCycleProperty(true), 64.0F, list),
                    rangeDispatch(new TimeProperty(true, TimeProperty.Source.RANDOM), 1F, list)
                )
            );
    }
}
