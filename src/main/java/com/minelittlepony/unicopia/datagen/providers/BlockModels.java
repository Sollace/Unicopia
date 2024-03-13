package com.minelittlepony.unicopia.datagen.providers;

import java.util.Optional;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.data.client.Model;
import net.minecraft.data.client.TextureKey;
import net.minecraft.util.Identifier;

public interface BlockModels {
    Model FRUIT = block("fruit", TextureKey.CROSS);

    static Model block(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("block/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static Model block(Identifier parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("block/")), Optional.empty(), requiredTextureKeys);
    }
}
