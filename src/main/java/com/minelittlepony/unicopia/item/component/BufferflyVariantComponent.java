package com.minelittlepony.unicopia.item.component;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record BufferflyVariantComponent (ButterflyEntity.Variant variant, boolean showInTooltip) implements TooltipAppender {
    public static final BufferflyVariantComponent DEFAULT = new BufferflyVariantComponent(ButterflyEntity.Variant.BUTTERFLY, false);
    public static final Codec<BufferflyVariantComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ButterflyEntity.Variant.CODEC.fieldOf("variant").forGetter(BufferflyVariantComponent::variant),
            Codec.BOOL.fieldOf("show_in_tooltip").forGetter(BufferflyVariantComponent::showInTooltip)
    ).apply(instance, BufferflyVariantComponent::new));
    public static final PacketCodec<ByteBuf, BufferflyVariantComponent> PACKET_CODEC = PacketCodec.tuple(
            ButterflyEntity.Variant.PACKET_CODEC, BufferflyVariantComponent::variant,
            PacketCodecs.BOOL, BufferflyVariantComponent::showInTooltip,
            BufferflyVariantComponent::new
    );

    public static BufferflyVariantComponent get(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.BUTTERFLY_VARIANT, DEFAULT);
    }

    public static ItemStack set(ItemStack stack, BufferflyVariantComponent variant) {
        stack.set(UDataComponentTypes.BUTTERFLY_VARIANT, variant);
        return stack;
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (showInTooltip()) {
            tooltip.accept(Text.literal(variant().name()).formatted(Formatting.LIGHT_PURPLE));
        }
    }
}
