package com.minelittlepony.unicopia.diet;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public record Effect(
        TagKey<Item> tag,
        Optional<FoodComponent> foodComponent,
        Ailment ailment
) implements Predicate<ItemStack> {
    public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.unprefixedCodec(RegistryKeys.ITEM).fieldOf("tag").forGetter(Effect::tag),
            FoodComponent.CODEC.optionalFieldOf("food_component").forGetter(Effect::foodComponent),
            Ailment.CODEC.fieldOf("ailment").forGetter(Effect::ailment)
    ).apply(instance, Effect::new));

    public Effect(PacketByteBuf buffer) {
        this(TagKey.of(RegistryKeys.ITEM, buffer.readIdentifier()), buffer.readOptional(FoodComponent::new), new Ailment(buffer));
    }

    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeIdentifier(tag.id());
        buffer.writeOptional(foodComponent, (b, f) -> f.toBuffer(b));
        ailment.toBuffer(buffer);
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.isIn(tag);
    }

    public record FoodComponent (float hunger, float saturation) {
        public static final Codec<FoodComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("hunger").forGetter(FoodComponent::hunger),
                Codec.FLOAT.fieldOf("saturation").forGetter(FoodComponent::saturation)
        ).apply(instance, FoodComponent::new));

        public FoodComponent(PacketByteBuf buffer) {
            this(buffer.readFloat(), buffer.readFloat());
        }

        public void toBuffer(PacketByteBuf buffer) {
            buffer.writeFloat(hunger);
            buffer.writeFloat(saturation);
        }
    }
}