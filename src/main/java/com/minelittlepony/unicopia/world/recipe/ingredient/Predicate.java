package com.minelittlepony.unicopia.world.recipe.ingredient;

import java.util.Random;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

public interface Predicate {
    Predicate EMPTY = new Predicate() {
        @Override
        public Stream<ItemStack> getMatchingStacks() {
            return Stream.empty();
        }
        @Override
        public boolean matches(ItemStack stack, int materialMult) {
            return true;
        }

        @Override
        public PredicateSerializer<? super Predicate> getSerializer() {
            return PredicateSerializer.EMPTY;
        }
    };

    Stream<ItemStack> getMatchingStacks();

    boolean matches(ItemStack stack, int materialMult);

    PredicateSerializer<?> getSerializer();

    default ItemStack applyModifiers(ItemStack output, Random random) {
        return output;
    }

    default void write(PacketByteBuf buf) {
        @SuppressWarnings("unchecked")
        PredicateSerializer<Predicate> serializer = (PredicateSerializer<Predicate>)getSerializer();
        buf.writeIdentifier(PredicateSerializer.REGISTRY.getId(serializer));
        serializer.write(buf, this);
    }

    static Predicate read(PacketByteBuf buf) {
        return PredicateSerializer.REGISTRY.get(buf.readIdentifier()).read(buf);
    }
}