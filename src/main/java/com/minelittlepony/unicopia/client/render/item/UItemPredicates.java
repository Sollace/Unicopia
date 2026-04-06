package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;
import net.minecraft.client.render.item.property.bool.BooleanProperties;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.render.item.property.select.SelectProperties;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.item.tint.TintSourceTypes;

public interface UItemPredicates {

    private static <P extends SelectProperty<T>, T> void option(String name, SelectProperty.Type<P, T> type) {
        SelectProperties.ID_MAPPER.put(Unicopia.id(name), type);
    }

    private static void flag(String name, MapCodec<? extends BooleanProperty> codec) {
        BooleanProperties.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    private static void range(String name, MapCodec<? extends NumericProperty> codec) {
        NumericProperties.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    private static void tint(String name, MapCodec<? extends TintSource> codec) {
        TintSourceTypes.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    private static void model(String name, MapCodec<? extends ItemModel.Unbaked> codec) {
        ItemModelTypes.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    private static void specialModel(String name, MapCodec<? extends SpecialModelRenderer.Unbaked> codec) {
        SpecialModelTypes.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    static void bootstrap() {
        option("gem_shape", GemShapeProperty.TYPE);
        option("balloon_design", BalloonDesignProperty.TYPE);
        option("butterfly_variant", ButterflyVariantProperty.TYPE);
        option("affinity", AffinityProperty.TYPE);
        /* baited fishing rods ---> */ //FishingRodCastProperty.CODEC
        /* rock candy ---> */ // CountProperty.CODEC

        range("zap_apple_cycle", ZapAppleCycleProperty.CODEC);

        tint("spell", SpellTintSource.CODEC);
        tint("block", BlockTintSource.CODEC);

        specialModel("cloud_bed", CloudBedModelRenderer.Unbaked.CODEC);
        specialModel("cloud_chest", CloudChestModelRenderer.Unbaked.CODEC);
        specialModel("jar_contents", JarContentsModelRenderer.Unbaked.CODEC);
        specialModel("polearm", PolearmModelRenderer.Unbaked.CODEC);
        /* polearms ---> */ // UsingItemProperty.CODEC
    }
}
