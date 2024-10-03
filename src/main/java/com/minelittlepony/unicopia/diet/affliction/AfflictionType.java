package com.minelittlepony.unicopia.diet.affliction;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record AfflictionType<T extends Affliction>(Identifier id, MapCodec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
    public static final String DEFAULT_ID = "unicopia:apply_status_effect";
    public static final Registry<AfflictionType<?>> REGISTRY = RegistryUtils.createDefaulted(Unicopia.id("affliction_type"), DEFAULT_ID);
    public static final Codec<Affliction> CODEC = CodecUtils.apply(REGISTRY.getCodec()
            .dispatch("type", Affliction::getType, AfflictionType::codec), elementCodec -> Codec
            .withAlternative(elementCodec, Codec.list(elementCodec), afflictions -> {
                afflictions = afflictions.stream().filter(f -> !f.isEmpty()).toList();
                return switch (afflictions.size()) {
                    case 0 -> EmptyAffliction.INSTANCE;
                    case 1 -> afflictions.get(0);
                    default -> new CompoundAffliction(afflictions);
                };
            }));
    public static final PacketCodec<RegistryByteBuf, Affliction> PACKET_CODEC = PacketCodecs.registryValue(REGISTRY.getKey())
            .dispatch(Affliction::getType, AfflictionType::packetCodec);

    public static final AfflictionType<EmptyAffliction> EMPTY = register("empty", EmptyAffliction.CODEC, EmptyAffliction.PACKET_CODEC);
    public static final AfflictionType<CompoundAffliction> MANY = register("many", CompoundAffliction.CODEC, CompoundAffliction.PACKET_CODEC);
    public static final AfflictionType<StatusEffectAffliction> APPLY_STATUS_EFFECT = register("apply_status_effect", StatusEffectAffliction.CODEC, StatusEffectAffliction.PACKET_CODEC);
    public static final AfflictionType<LoseHungerAffliction> LOSE_HUNGER = register("lose_hunger", LoseHungerAffliction.CODEC, LoseHungerAffliction.PACKET_CODEC);
    public static final AfflictionType<HealingAffliction> HEALING = register("healing", HealingAffliction.CODEC, HealingAffliction.PACKET_CODEC);
    public static final AfflictionType<ClearLoveSicknessAffliction> CURE_LOVE_SICKNESS = register("cure_love_sickness", ClearLoveSicknessAffliction.CODEC, ClearLoveSicknessAffliction.PACKET_CODEC);

    static <T extends Affliction> AfflictionType<T> register(String name, MapCodec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
        return Registry.register(REGISTRY, Unicopia.id(name), new AfflictionType<>(Unicopia.id(name), codec, packetCodec));
    }

    public String getTranslationKey() {
        return Util.createTranslationKey("affliction", id());
    }

    public static void bootstrap() { }
}
