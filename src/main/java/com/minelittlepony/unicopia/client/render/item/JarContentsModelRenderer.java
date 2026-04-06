package com.minelittlepony.unicopia.client.render.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.component.Appearance;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

public class JarContentsModelRenderer implements SpecialModelRenderer<ItemStack> {

    private final ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

    @Override
    public void render(@Nullable ItemStack appearance, ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, boolean glint) {

        if (appearance == null) {
            return;
        }

        if (displayContext == ItemDisplayContext.GUI) {
            DiffuseLighting.disableGuiDepthLighting();
        }

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        ClientWorld world = MinecraftClient.getInstance().world;

        matrices.push();
        if (displayContext.isFirstPerson()) {
            matrices.translate(0.05, 0.06, 0.06);
        } else if (displayContext == ItemDisplayContext.HEAD) {
            matrices.translate(0, 0.4, 0);
        } else if (displayContext == ItemDisplayContext.GROUND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            matrices.translate(0, 0.06, 0);
        }
        // GUI, FIXED, NONE - translate(0, 0, 0)

        float scale = 0.5F;
        matrices.scale(scale, scale, scale);
        renderer.renderItem(appearance, displayContext, light, overlay, matrices, immediate, world, 0);
        matrices.pop();

        if (displayContext == ItemDisplayContext.GUI) {
            if (vertices instanceof Immediate i) {
                i.draw();
            }

            DiffuseLighting.enableGuiDepthLighting();
        }
    }

    @Override
    public ItemStack getData(ItemStack stack) {
        if (!Appearance.hasAppearance(stack)) {
            return null;
        }
        return Appearance.upwrapAppearance(stack);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels models) {
            return new JarContentsModelRenderer();
        }
    }
}
