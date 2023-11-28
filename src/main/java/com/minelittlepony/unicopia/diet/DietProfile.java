package com.minelittlepony.unicopia.diet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public record DietProfile(
        float defaultMultiplier,
        float foragingMultiplier,
        List<Multiplier> multipliers,
        List<Effect> effects
    ) {
    public static final Codec<DietProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("default_multiplier").forGetter(DietProfile::defaultMultiplier),
                Codec.FLOAT.fieldOf("foraging_multiplier").forGetter(DietProfile::foragingMultiplier),
                Codec.list(Multiplier.CODEC).fieldOf("multipliers").forGetter(DietProfile::multipliers),
                Codec.list(Effect.CODEC).fieldOf("effects").forGetter(DietProfile::effects)
    ).apply(instance, DietProfile::new));

    public DietProfile(PacketByteBuf buffer) {
        this(buffer.readFloat(), buffer.readFloat(), buffer.readList(Multiplier::new), buffer.readList(Effect::new));
    }

    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeFloat(defaultMultiplier);
        buffer.writeFloat(foragingMultiplier);
        buffer.writeCollection(multipliers, (b, t) -> t.toBuffer(b));
        buffer.writeCollection(effects, (b, t) -> t.toBuffer(b));
    }

    public Optional<Multiplier> findMultiplier(ItemStack stack) {
        return multipliers.stream().filter(m -> m.test(stack)).findFirst();
    }

    public Optional<Effect> findEffect(ItemStack stack) {
        return effects.stream().filter(m -> m.test(stack)).findFirst();
    }

    public record Multiplier(
            Set<TagKey<Item>> tags,
            float hunger,
            float saturation
    ) implements Predicate<ItemStack> {
        public static final Codec<Set<TagKey<Item>>> TAGS_CODEC = Codec.list(TagKey.unprefixedCodec(RegistryKeys.ITEM)).xmap(
                l -> l.stream().distinct().collect(Collectors.toSet()),
                set -> new ArrayList<>(set)
        );
        public static final Codec<Multiplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TAGS_CODEC.fieldOf("tags").forGetter(Multiplier::tags),
                Codec.FLOAT.fieldOf("hunger").forGetter(Multiplier::hunger),
                Codec.FLOAT.fieldOf("saturation").forGetter(Multiplier::saturation)
        ).apply(instance, Multiplier::new));

        public Multiplier(PacketByteBuf buffer) {
            this(buffer.readCollection(HashSet::new, p -> TagKey.of(RegistryKeys.ITEM, p.readIdentifier())), buffer.readFloat(), buffer.readFloat());
        }

        @Override
        public boolean test(ItemStack stack) {
            return tags.stream().anyMatch(tag -> stack.isIn(tag));
        }

        public void toBuffer(PacketByteBuf buffer) {
            buffer.writeCollection(tags, (p, t) -> p.writeIdentifier(t.id()));
            buffer.writeFloat(hunger);
            buffer.writeFloat(saturation);
        }
    }
}
