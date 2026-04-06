package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.CloudChestBlockEntityRenderer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;

public class CloudChestModelRenderer implements SimpleSpecialModelRenderer {
    public static final Identifier TEXTURE = Unicopia.id("textures/entity/chest/cloud.png");
    private final CloudChestBlockEntityRenderer.Model model = new CloudChestBlockEntityRenderer.Model(CloudChestBlockEntityRenderer.Model.getSingleChestModelData().createModel(), TEXTURE);

    private final Identifier texture;
    private final float openness;

    public CloudChestModelRenderer(Identifier texture, float openness) {
        this.texture = texture;
        this.openness = openness;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, boolean glint) {
        VertexConsumer vertexConsumer = vertices.getBuffer(RenderLayer.getEntityTranslucent(texture));
        model.setAngles(openness);
        model.render(matrices, vertexConsumer, light, overlay);
    }

    public record Unbaked(Identifier texture, float openness) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture),
                Codec.FLOAT.optionalFieldOf("openness", 0F).forGetter(Unbaked::openness)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels models) {
            return new CloudChestModelRenderer(texture, openness);
        }
    }
}
