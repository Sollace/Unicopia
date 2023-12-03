package com.minelittlepony.unicopia.diet.affliction;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.PacketByteBuf.PacketReader;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

public record AfflictionType<T extends Affliction>(Codec<T> codec, Identifier id, PacketReader<T> reader) {
    public static final String DEFAULT_ID = "unicopia:apply_status_effect";
    public static final Registry<AfflictionType<?>> REGISTRY = RegistryUtils.createDefaulted(Unicopia.id("affliction_type"), DEFAULT_ID);
    @SuppressWarnings("unchecked")
    public static final Codec<Affliction> CODEC = Codecs.JSON_ELEMENT.<Affliction>flatXmap(json -> {
        if (!json.isJsonObject()) {
            return DataResult.error(() -> "Not a JSON object");
        }
        return Identifier.validate(JsonHelper.getString(JsonHelper.asObject(json, "affliction"), "type", AfflictionType.DEFAULT_ID))
                .flatMap(type -> AfflictionType.REGISTRY.get(type).codec().parse(JsonOps.INSTANCE, json));
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
    public static final AfflictionType<LoseHungerAffliction> LOSE_HUNGER = register("lose_hunger", LoseHungerAffliction.CODEC, LoseHungerAffliction::new);
    public static final AfflictionType<HealingAffliction> HEALING = register("healing", HealingAffliction.CODEC, HealingAffliction::new);
    public static final AfflictionType<ClearLoveSicknessAffliction> CURE_LOVE_SICKNESS = register("cure_love_sickness", ClearLoveSicknessAffliction.CODEC, buffer -> ClearLoveSicknessAffliction.INSTANCE);

    static <T extends Affliction> AfflictionType<T> register(String name, Codec<T> codec, PacketReader<T> reader) {
        return Registry.register(REGISTRY, Unicopia.id(name), new AfflictionType<>(codec, Unicopia.id(name), reader));
    }

    public String getTranslationKey() {
        return Util.createTranslationKey("affliction", id());
    }

    public static void bootstrap() { }
}
