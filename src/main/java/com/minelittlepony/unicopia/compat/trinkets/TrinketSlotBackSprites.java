package com.minelittlepony.unicopia.compat.trinkets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class TrinketSlotBackSprites {
    private static final Identifier BLANK_FACE = new Identifier("trinkets", "textures/gui/blank_back.png");

    private static final Map<Identifier, Identifier> CACHE = new HashMap<>();

    public static Identifier getBackSprite(Identifier originalSprite) {
        return CACHE.computeIfAbsent(originalSprite, TrinketSlotBackSprites::generateTransparentSprite);
    }

    private static Identifier generateTransparentSprite(Identifier originalId) {
        return readImage(BLANK_FACE, blank -> {
           return readImage(originalId, original -> {
               NativeImage generatedImage = new NativeImage(original.getWidth(), original.getHeight(), false);
               final float widthScale = original.getWidth() / (float)blank.getWidth();
               final float heightScale = original.getHeight() / (float)blank.getHeight();

               for (int x = 0; x < original.getWidth(); x++) {
                   for (int y = 0; y < original.getHeight(); y++) {
                       int blankColor = blank.getColor((int)(x * widthScale), (int)(y * heightScale));
                       int originalColor = original.getColor(x, y);

                       generatedImage.setColor(x, y, blankColor == originalColor ? 0 : originalColor);
                   }
               }

               return MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("trinket_slot" + originalId.getPath(), new NativeImageBackedTexture(generatedImage));
           }).orElse(originalId);
        }).orElse(originalId);
    }

    private static <T> Optional<T> readImage(Identifier id, Function<NativeImage, T> consumer) {
        return MinecraftClient.getInstance().getResourceManager().getResource(id).map(input -> {
            try (NativeImage image = NativeImage.read(input.getInputStream())) {
                return consumer.apply(image);
            } catch (Exception e) {
                Unicopia.LOGGER.error("Error whilst reading slot background resource " + id, e);
            }
            return null;
        });
    }
}
