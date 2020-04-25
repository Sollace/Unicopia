package com.minelittlepony.unicopia.enchanting.recipe;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

class AffineIngredient implements SpellIngredient {

    static final Serializer<AffineIngredient> SERIALIZER = new Serializer<AffineIngredient>() {
        @Override
        public AffineIngredient read(JsonElement json) {
            return new AffineIngredient(new Identifier(json.getAsJsonObject().get("id").getAsString()));
        }

        @Override
        public AffineIngredient read(PacketByteBuf buff) {
            return new AffineIngredient(new Identifier(buff.readString()));
        }

        @Override
        public void write(PacketByteBuf buff, AffineIngredient ingredient) {
            buff.writeString(ingredient.res.toString());
        }
    };

    private final Identifier res;

    AffineIngredient(Identifier res) {
        this.res = res;
    }

    @Override
    public ItemStack getStack() {
        return AffineIngredients.getInstance().getIngredient(res).getStack();
    }

    @Override
    public Stream<ItemStack> getStacks() {
        return AffineIngredients.getInstance().getIngredient(res).getStacks();
    }

    @Override
    public boolean matches(ItemStack other, int materialMult) {
        return AffineIngredients.getInstance().getIngredient(res).matches(other, materialMult);
    }

    @Nonnull
    static SpellIngredient parse(JsonObject json) {
        return SERIALIZER.read(json);
    }

    @Override
    public Serializer<?> getSerializer() {
        return SERIALIZER;
    }
}