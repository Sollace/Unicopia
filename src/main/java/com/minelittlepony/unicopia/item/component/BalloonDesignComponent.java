package com.minelittlepony.unicopia.item.component;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
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

public record BalloonDesignComponent(AirBalloonEntity.BalloonDesign design, boolean showInTooltip) implements TooltipAppender {
    public static final BalloonDesignComponent DEFAULT = new BalloonDesignComponent(AirBalloonEntity.BalloonDesign.NONE, false);
    public static final Codec<BalloonDesignComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AirBalloonEntity.BalloonDesign.CODEC.fieldOf("design").forGetter(BalloonDesignComponent::design),
            Codec.BOOL.fieldOf("show_in_tooltip").forGetter(BalloonDesignComponent::showInTooltip)
    ).apply(instance, BalloonDesignComponent::new));
    public static final PacketCodec<ByteBuf, BalloonDesignComponent> PACKET_CODEC = PacketCodec.tuple(
            AirBalloonEntity.BalloonDesign.PACKET_CODEC, BalloonDesignComponent::design,
            PacketCodecs.BOOL, BalloonDesignComponent::showInTooltip,
            BalloonDesignComponent::new
    );

    public static BalloonDesignComponent get(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.BALLOON_DESIGN, DEFAULT);
    }

    public static ItemStack set(ItemStack stack, BalloonDesignComponent variant) {
        stack.set(UDataComponentTypes.BALLOON_DESIGN, variant);
        return stack;
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (showInTooltip() && design != AirBalloonEntity.BalloonDesign.NONE) {
            tooltip.accept(Text.literal(design().name()).formatted(Formatting.LIGHT_PURPLE));
        }
    }
}
