package com.minelittlepony.unicopia.datagen.providers;

import java.util.Locale;
import java.util.Optional;

import com.google.common.base.Strings;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

interface ItemModels {
    Model GENERATED = net.minecraft.data.client.Models.GENERATED;
    Model CHEST = item(new Identifier("chest"), TextureKey.PARTICLE);
    Model BUILTIN_ENTITY = new Model(Optional.of(new Identifier("builtin/entity")), Optional.empty());
    Model TEMPLATE_AMULET = item("template_amulet", TextureKey.LAYER0);
    Model TEMPLATE_EYEWEAR = item("template_eyewear", TextureKey.LAYER0);
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
        GENERATED.upload(ModelIds.getItemSubModelId(item, "_in_inventory"), textures, itemModelGenerator.writer);
        ModelOverrides.of(TRIDENT_IN_HAND)
            .addOverride("throwing", 1, generator -> TRIDENT_THROWING.upload(ModelIds.getItemSubModelId(item, "_throwing"), textures, itemModelGenerator.writer))
            .upload(ModelIds.getItemModelId(item), textures, itemModelGenerator);
    }

    static void registerButterfly(ItemModelGenerator itemModelGenerator, Item item) {
        float step = 1F / ButterflyEntity.Variant.VALUES.length;
        ModelOverrides.of(GENERATED).addUniform("variant", step, 1 - step, step, (i, value) -> {
            String name = ButterflyEntity.Variant.byId(i + 1).name().toLowerCase(Locale.ROOT);
            Identifier subModelId = Registries.ITEM.getId(item).withPath(p -> "item/" + name + "_" + p);
            return GENERATED.upload(subModelId, TextureMap.layer0(subModelId), itemModelGenerator.writer);
        }).upload(item, itemModelGenerator);
    }

    static void registerSpectralBlock(ItemModelGenerator itemModelGenerator, Item item) {
        final float step = 0.025F;
        String[] suffexes = { "", "_greening", "_flowering", "_fruiting", "_ripe", "" };
        ModelOverrides.of(GENERATED).addUniform("unicopia:zap_cycle", 0, 1, step, (index, value) -> {
            if (value < 0.0001 || value > 0.999F) {
                return ModelIds.getItemModelId(item);
            }
            Identifier subModelId = ModelIds.getItemSubModelId(item, suffexes[index / 8] + "_" + Strings.padStart((index % 8) * 5 + "", 2, '0'));
            return GENERATED.upload(subModelId, TextureMap.layer0(subModelId), itemModelGenerator.writer);
        }).upload(item, "_00", itemModelGenerator);
    }
}
