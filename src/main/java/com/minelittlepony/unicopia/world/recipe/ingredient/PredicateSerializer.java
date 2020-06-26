package com.minelittlepony.unicopia.world.recipe.ingredient;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public interface PredicateSerializer<T extends Predicate> {
    Registry<PredicateSerializer<?>> REGISTRY = Registries.createSimple(new Identifier("unicopia", "recipe_predicate"));
    Registry<JsonReader> JSON_READERS = Registries.createSimple(new Identifier("unicopia", "recipe_predicate_reader"));

    PredicateSerializer<Predicate> EMPTY = register("empty", new PredicateSerializer<Predicate>() {
        @Override
        public Predicate read(PacketByteBuf buf) {
            return Predicate.EMPTY;
        }

        @Override
        public void write(PacketByteBuf buf, Predicate predicate) {
        }
    });
    PredicateSerializer<StackPredicate> STACK = register("stack", new StackPredicate.Serializer());
    PredicateSerializer<TagPredicate> TAG = register("tag", new TagPredicate.Serializer());
    PredicateSerializer<ChoicePredicate> CHOICE = register("choice", new ChoicePredicate.Serializer());
    PredicateSerializer<SpellPredicate> SPELL = register("spell", new SpellPredicate.Serializer());
    PredicateSerializer<EnchantmentPredicate> ENCHANTMENT = register("enchantment", new EnchantmentPredicate.Serializer());
    PredicateSerializer<PotionPredicate> POTION = register("potion", new PotionPredicate.Serializer());
    PredicateSerializer<DamagePredicate> DAMAGE = register("damage", new DamagePredicate.Serializer());
    PredicateSerializer<ToxicityPredicate> TOXICITY = register("toxicity", new ToxicityPredicate.Serializer());

    static <T extends Predicate> PredicateSerializer<T> register(String name, PredicateSerializer<T> entry) {
        Identifier id = new Identifier("unicopia", name);
        if (entry instanceof JsonReader) {
            Registry.register(JSON_READERS, id, (JsonReader)entry);
        }
        return Registry.register(REGISTRY, id, entry);
    }

    Predicate read(PacketByteBuf buf);

    void write(PacketByteBuf buf, T predicate);

    @FunctionalInterface
    interface JsonReader {
        Predicate read(JsonObject json);
    }
}