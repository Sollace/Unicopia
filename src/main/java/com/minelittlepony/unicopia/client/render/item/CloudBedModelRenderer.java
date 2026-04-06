package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.block.FancyBedBlock.SheetPattern;
import com.minelittlepony.unicopia.client.render.entity.CloudBedBlockEntityRenderer;
import com.minelittlepony.unicopia.item.FancyBedItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CloudBedModelRenderer implements SpecialModelRenderer<SheetPattern> {
    private final CloudBedBlockEntityRenderer renderer;
    private final Identifier texture;
    private final boolean translucent;

    public CloudBedModelRenderer(CloudBedBlockEntityRenderer renderer, Identifier texture, boolean translucent) {
        this.renderer = renderer;
        this.texture = texture;
        this.translucent = translucent;
    }

    @Override
    public void render(SheetPattern pattern, ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, boolean glint) {
        renderer.renderAsItem(pattern, texture, translucent, matrices, vertices, light, overlay);
    }

    @Override
    public SheetPattern getData(ItemStack stack) {
        return FancyBedItem.getPattern(stack);
    }

    public record Unbaked(Identifier texture, boolean translucent) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture),
                Codec.BOOL.fieldOf("translucent").forGetter(Unbaked::translucent)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels models) {
            return new CloudBedModelRenderer(new CloudBedBlockEntityRenderer(), texture, translucent);
        }
    }
}
