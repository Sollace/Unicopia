package com.minelittlepony.unicopia.item.component;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public record MimicComponent(boolean mimic) {
    public static final Codec<MimicComponent> CODEC = Codec.BOOL.xmap(MimicComponent::new, MimicComponent::mimic);
    public static final PacketCodec<ByteBuf, MimicComponent> PACKET_CODEC = PacketCodecs.BOOLEAN.xmap(MimicComponent::new, MimicComponent::mimic);
    private static final NbtCompound TEMPLATE_NBT = Util.make(new NbtCompound(), nbt -> nbt.putBoolean("mimic", true));

    public static void appendTooltip(ItemStack stack, TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (isMimic(stack)) {
            tooltip.accept(Text.translatable("component.unicopia.mimic.is_mimic").formatted(Formatting.RED));
        }
    }

    static boolean isMimic(ItemStack stack) {
        @Nullable MimicComponent mimic = stack.get(UDataComponentTypes.MIMIC);
        if (mimic != null) {
            return mimic.mimic();
        }
        @Nullable NbtComponent nbt = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        return nbt != null && nbt.matches(TEMPLATE_NBT);
    }
}
