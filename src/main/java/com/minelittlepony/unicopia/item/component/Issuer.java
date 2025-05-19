package com.minelittlepony.unicopia.item.component;

import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;

public record Issuer(String name, UUID id) implements TooltipAppender {
    public static final Codec<Issuer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Issuer::name),
            Uuids.CODEC.fieldOf("id").forGetter(Issuer::id)
    ).apply(instance, Issuer::new));
    public static final PacketCodec<ByteBuf, Issuer> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, Issuer::name,
            Uuids.PACKET_CODEC, Issuer::id,
            Issuer::new
    );

    public static ItemStack set(ItemStack stack, Entity signer) {
        stack.set(UDataComponentTypes.ISSUER, new Issuer(signer.getDisplayName().getString(), signer.getUuid()));
        return stack;
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        tooltip.accept(Text.translatable("item.unicopia.friendship_bracelet.issuer", name()));
    }

    public boolean isSignedBy(PlayerEntity player) {
        return isSignedBy(player.getUuid());
    }

    public boolean isSignedBy(UUID player) {
        return player.equals(id());
    }

    @Nullable
    public static String getSignatorName(ItemStack stack) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer == null ? null : issuer.name();
    }

    @Nullable
    public static UUID getSignatorId(ItemStack stack) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer == null ? null : issuer.id();
    }

    public static boolean isSigned(ItemStack stack) {
        return stack.contains(UDataComponentTypes.ISSUER);
    }

    public static boolean isSignedBy(ItemStack stack, PlayerEntity player) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer != null && issuer.isSignedBy(player);
    }

    public static boolean isSignedBy(ItemStack stack, UUID player) {
        Issuer issuer = stack.get(UDataComponentTypes.ISSUER);
        return issuer != null && issuer.isSignedBy(player);
    }
}
