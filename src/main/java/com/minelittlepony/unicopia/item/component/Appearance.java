package com.minelittlepony.unicopia.item.component;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;

public record Appearance(ItemStack item, boolean replaceFully) {
    public static final Appearance DEFAULT = new Appearance(ItemStack.EMPTY, false);
    public static final Appearance DEFAULT_FULLY_DISGUISED = new Appearance(ItemStack.EMPTY, true);
    public static final Codec<Appearance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtils.xor(
                    ItemStack.CODEC,
                    Registries.ITEM.getCodec().xmap(Item::getDefaultStack, ItemStack::getItem)
            ).fieldOf("item").forGetter(Appearance::item),
            Codec.BOOL.fieldOf("replace_fully").forGetter(Appearance::replaceFully)
    ).apply(instance, Appearance::of));
    public static final PacketCodec<RegistryByteBuf, Appearance> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, Appearance::item,
            PacketCodecs.BOOL, Appearance::replaceFully,
            Appearance::of
    );

    public Appearance {
        item.remove(UDataComponentTypes.APPEARANCE);
    }

    public ItemStack unwrap(ItemStack stack) {
        return item().isEmpty() ? stack : item();
    }

    public static Appearance of(ItemStack stack, boolean complete) {
        return stack.isEmpty() ? (complete ? DEFAULT_FULLY_DISGUISED : DEFAULT) : new Appearance(stack, complete);
    }

    public static Appearance get(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.APPEARANCE, DEFAULT);
    }

    public static boolean hasAppearance(ItemStack stack) {
        return !get(stack).item().isEmpty();
    }

    public static ItemStack upwrapAppearance(ItemStack stack) {
        return get(stack).unwrap(stack);
    }

    public static ItemStack set(ItemStack stack, ItemStack appearance) {
        Appearance component = createAppearanceFor(stack, appearance);
        stack.set(UDataComponentTypes.APPEARANCE, component);
        if (stack.getItem() instanceof AppearanceChangeCallback callback) {
            callback.onAppearanceSet(stack, component);
        }
        return stack;
    }

    private static Appearance createAppearanceFor(ItemStack stack, ItemStack appearance) {
        if (appearance.isEmpty()) {
            return stack.getDefaultComponents().getOrDefault(UDataComponentTypes.APPEARANCE, DEFAULT);
        }

        appearance = appearance.copy();
        appearance.setCount(stack.getCount());
        appearance.setDamage(stack.getDamage());
        return of(appearance, stack.getOrDefault(UDataComponentTypes.APPEARANCE, DEFAULT).replaceFully());
    }

    public interface AppearanceChangeCallback {
        void onAppearanceSet(ItemStack stack, Appearance appearance);
    }
}
