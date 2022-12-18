package com.minelittlepony.unicopia.client.render;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.item.AmuletItem;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class AmuletFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    private final AmuletModel model;

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public AmuletFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
        this.model = new AmuletModel(AmuletModel.getData(new Dilation(0.3F)).createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {

        ItemStack stack = AmuletItem.getForEntity(entity);

        if (!stack.isEmpty()) {
            Identifier texture = textures.computeIfAbsent(Registries.ITEM.getId(stack.getItem()), id -> new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + ".png"));

            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(texture), false, false);

            model.setAngles(entity, context.getModel());
            model.render(matrices, consumer, lightUv, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        }
    }

    public static class AmuletModel extends Model {

        private final ModelPart amulet;

        public AmuletModel(ModelPart tree) {
            super(RenderLayer::getEntityTranslucent);
            amulet = tree.getChild("amulet");
        }

        public static TexturedModelData getData(Dilation dilation) {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();

            root.addChild("amulet", ModelPartBuilder.create().cuboid(-4, 0, -2, 8, 12, 4, dilation), ModelTransform.NONE);

            return TexturedModelData.of(data, 64, 32);
        }

        public void setAngles(LivingEntity entity, BipedEntityModel<?> biped) {
            amulet.copyTransform(biped.body);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            amulet.render(matrices, vertexConsumer, i, j, f, g, h, k);
        }
    }
}
