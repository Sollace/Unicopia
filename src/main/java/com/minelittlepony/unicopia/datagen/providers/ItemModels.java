package com.minelittlepony.unicopia.datagen.providers;

import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

interface ItemModels {
    Model GENERATED = net.minecraft.data.client.Models.GENERATED;
    Model TEMPLATE_AMULET = item("template_amulet", TextureKey.LAYER0);
    Model TEMPLATE_SPAWN_EGG = item(new Identifier("template_spawn_egg"));
    Model TEMPLATE_MUG = item("template_mug", TextureKey.LAYER0);
    Model HANDHELD_STAFF = item("handheld_staff", TextureKey.LAYER0);
    Model TRIDENT_THROWING = item(new Identifier("trident_throwing"), TextureKey.LAYER0);
    Model TRIDENT_IN_HAND = item(new Identifier("trident_in_hand"), TextureKey.LAYER0);

    static Model item(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("item/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static Model item(Identifier parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("item/")), Optional.empty(), requiredTextureKeys);
    }

    static void register(ItemModelGenerator itemModelGenerator, Item... items) {
        register(itemModelGenerator, GENERATED, items);
    }

    static void register(ItemModelGenerator itemModelGenerator, Model model, Item... items) {
        for (Item item : items) {
            itemModelGenerator.register(item, model);
        }
    }

    static void registerPolearm(ItemModelGenerator itemModelGenerator, Item item) {
        TextureMap textures = TextureMap.layer0(TextureMap.getId(item));
        Identifier throwingId = ModelIds.getItemSubModelId(item, "_throwing");
        GENERATED.upload(ModelIds.getItemSubModelId(item, "_in_inventory"), textures, itemModelGenerator.writer);
        TRIDENT_THROWING.upload(throwingId, textures, itemModelGenerator.writer);
        TRIDENT_IN_HAND.upload(ModelIds.getItemModelId(item), textures, (id, jsonSupplier) -> {
            itemModelGenerator.writer.accept(id, () -> Util.make(jsonSupplier.get(), json -> {
                json.getAsJsonObject().add("overrides", Util.make(new JsonArray(), overrides -> {
                    overrides.add(Util.make(new JsonObject(), override -> {
                        override.addProperty("model", throwingId.toString());
                        override.add("predicate", Util.make(new JsonObject(), predicate -> {
                            predicate.addProperty("throwing", 1);
                        }));
                    }));
                }));
            }));
        });
    }
}
