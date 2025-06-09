package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public interface UIngredients {
    CustomIngredientSerializer<IngredientWithSpell> ENCHANTED_ITEM = register("enchanted_item", IngredientWithSpell.CODEC, IngredientWithSpell.PACKET_CODEC);

    private static <T extends CustomIngredient> CustomIngredientSerializer<T> register(String name, Codec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec) {
        var serializer = new Serializer<>(Unicopia.id(name), MapCodec.assumeMapUnsafe(codec), packetCodec);
        CustomIngredientSerializer.register(serializer);
        return serializer;
    }

    static void bootstrap() {}

    record Serializer<T extends CustomIngredient>(Identifier id, MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec) implements CustomIngredientSerializer<T> {

        @Override
        public Identifier getIdentifier() {
            return id;
        }

        @Override
        public MapCodec<T> getCodec() {
            return codec;
        }

        @Override
        public PacketCodec<RegistryByteBuf, T> getPacketCodec() {
            return packetCodec;
        }
    }
}
