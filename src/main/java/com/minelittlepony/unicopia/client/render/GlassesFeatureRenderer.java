package com.minelittlepony.unicopia.client.render;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.item.GlassesItem;

import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class GlassesFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    private final GlassesModel model;

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public GlassesFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
        this.model = new GlassesModel(GlassesModel.getData(Dilation.NONE, -8, -4).createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {

        ItemStack stack = GlassesItem.getForEntity(entity).stack();

        if (!stack.isEmpty()) {
            Identifier texture = textures.computeIfAbsent(Registries.ITEM.getId(stack.getItem()), id -> id.withPath(p -> "textures/models/armor/" + p + ".png"));

            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(texture), false);

            model.setAngles(entity, context.getModel());
            model.render(matrices, consumer, lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);
        }
    }

    public static class GlassesModel extends Model {

        private final ModelPart root;

        public GlassesModel(ModelPart tree) {
            super(RenderLayer::getEntityTranslucent);
            root = tree;
        }

        public static TexturedModelData getData(Dilation dilation, int y, int z) {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();

            root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4, y, z, 8, 8, 8, dilation.add(0.2F)), ModelTransform.NONE);
            root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4, y, z, 8, 8, 8, dilation.add(0.4F)), ModelTransform.NONE);

            return TexturedModelData.of(data, 64, 32);
        }

        public void setAngles(LivingEntity entity, BipedEntityModel<?> biped) {
            root.getChild(EntityModelPartNames.HEAD).copyTransform(biped.head);
            root.getChild(EntityModelPartNames.HAT).copyTransform(biped.hat);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
            root.render(matrices, vertices, light, overlay, color);
        }
    }
}
