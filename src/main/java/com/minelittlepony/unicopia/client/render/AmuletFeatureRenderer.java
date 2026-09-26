package com.minelittlepony.unicopia.client.render;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
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
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;

public class AmuletFeatureRenderer<E extends BipedEntityRenderState> implements AccessoryFeatureRenderer.Feature<E> {

    private final AmuletModel model;

    private final Map<RegistryKey<Item>, Identifier> textures = new HashMap<>();

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public AmuletFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
        this.model = new AmuletModel(AmuletModel.getData(new Dilation(0.3F)).createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle) {

        CasterState casterState = CasterState.of(entity);

        if (!casterState.amulet.empty) {
            Identifier texture = textures.computeIfAbsent(casterState.amulet.key, key -> key.getValue().withPath(p -> "textures/models/armor/" + p + ".png"));

            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(texture), false);

            model.getRootPart().resetTransform();
            if (context.getModel() instanceof BipedEntityModel) {
                model.setAngles(context.getModel());
            }
            model.render(matrices, consumer, lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);
        }
    }

    public static class AmuletModel extends Model {

        public AmuletModel(ModelPart tree) {
            super(tree.getChild("amulet"), RenderLayer::getEntityTranslucent);
        }

        public static TexturedModelData getData(Dilation dilation) {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();

            root.addChild("amulet", ModelPartBuilder.create().cuboid(-4, 0, -2, 8, 12, 4, dilation), ModelTransform.NONE);

            return TexturedModelData.of(data, 64, 32);
        }

        public void setAngles(BipedEntityModel<?> biped) {
            getRootPart().copyTransform(biped.body);
        }
    }
}
