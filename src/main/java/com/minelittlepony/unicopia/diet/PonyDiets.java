package com.minelittlepony.unicopia.diet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record PonyDiets (Map<Race, DietProfile> diets, Map<Identifier, FoodGroup> effects) {
    private static PonyDiets INSTANCE = new PonyDiets(Map.of(), Map.of());

    public static final PacketCodec<RegistryByteBuf, PonyDiets> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, PacketCodecs.registryValue(Race.REGISTRY_KEY), DietProfile.PACKET_CODEC), diets -> diets.diets,
            FoodGroup.PACKET_CODEC.collect(PacketCodecUtils.toMap(FoodGroup::id)), diets -> diets.effects,
            PonyDiets::new
    );

    public static PonyDiets getInstance() {
        return INSTANCE;
    }

    @Nullable
    static Effect getEffect(Identifier id) {
        return INSTANCE.effects.get(id);
    }

    public static void load(PonyDiets diets) {
        INSTANCE = diets;
    }

    public DietProfile getDiet(Pony pony) {
        return Optional.ofNullable(diets.get(pony.getObservedSpecies())).orElse(DietProfile.EMPTY);
    }

    public Effect getEffects(ItemStack stack) {
        return effects.values().stream().filter(effect -> effect.test(stack)).findFirst().map(Effect.class::cast).orElse(Effect.EMPTY);
    }

    public Effect getEffects(ItemStack stack, Pony pony) {
        return getDiet(pony).findEffect(stack).orElseGet(() -> getEffects(stack));
    }
}
