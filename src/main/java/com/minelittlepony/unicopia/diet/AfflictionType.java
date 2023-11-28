package com.minelittlepony.unicopia.diet;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.PacketByteBuf.PacketReader;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record AfflictionType<T extends Affliction>(Codec<T> codec, Identifier id, PacketReader<T> reader) {
    public static final String DEFAULT_ID = "unicopia:apply_status_effect";
    public static final Registry<AfflictionType<?>> REGISTRY = RegistryUtils.createDefaulted(Unicopia.id("affliction_type"), DEFAULT_ID);
    @SuppressWarnings("unchecked")
    public static final Codec<Affliction> CODEC = Codecs.JSON_ELEMENT.<Affliction>flatXmap(json -> {
        JsonObject obj = json.getAsJsonObject();
        return Identifier.validate(obj.has("type") ? obj.get("type").getAsString() : AfflictionType.DEFAULT_ID).flatMap(type -> {
            return AfflictionType.REGISTRY.get(type).codec().parse(JsonOps.INSTANCE, json);
        });
    }, thing -> {
        AfflictionType<?> type = thing.getType();
        return ((Codec<Affliction>)type.codec()).encodeStart(JsonOps.INSTANCE, thing).map(json -> {
            if (json.isJsonObject()) {
                json.getAsJsonObject().addProperty("type", type.id().toString());
            }
            return json;
        });
    });

    public static final AfflictionType<Affliction> EMPTY = register("empty", Codec.unit(Affliction.EMPTY), buffer -> Affliction.EMPTY);
    public static final AfflictionType<Affliction> MANY = register("many", CompoundAffliction.CODEC, CompoundAffliction::new);
    public static final AfflictionType<StatusEffectAffliction> APPLY_STATUS_EFFECT = register("apply_status_effect", StatusEffectAffliction.CODEC, StatusEffectAffliction::new);
    public static final AfflictionType<MultiplyHungerAffliction> MULTIPLY_HUNGER = register("multiply_hunger", MultiplyHungerAffliction.CODEC, MultiplyHungerAffliction::new);
    public static final AfflictionType<ClearLoveSicknessAffliction> CLEAR_LOVE_SICKNESS = register("clear_love_sickness", ClearLoveSicknessAffliction.CODEC, buffer -> ClearLoveSicknessAffliction.INSTANCE);

    static <T extends Affliction> AfflictionType<T> register(String name, Codec<T> codec, PacketReader<T> reader) {
        return Registry.register(REGISTRY, Unicopia.id(name), new AfflictionType<>(codec, Unicopia.id(name), reader));
    }

    public static void bootstrap() { }
}
