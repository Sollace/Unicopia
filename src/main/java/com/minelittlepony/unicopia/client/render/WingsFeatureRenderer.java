package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
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
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class WingsFeatureRenderer<S extends BipedEntityRenderState> implements AccessoryFeatureRenderer.Feature<S> {

    protected static final int FEATHER_COUNT = 8;

    private static final Identifier PEGASUS_WINGS = Unicopia.id("textures/models/wings/pegasus.png");
    private static final Identifier PEGASUS_WINGS_OVERLAY = Unicopia.id("textures/models/wings/pegasus_overlay.png");

    private final WingsModel model;

    private final FeatureRendererContext<S, ? extends BipedEntityModel<S>> context;

    public WingsFeatureRenderer(FeatureRendererContext<S, ? extends BipedEntityModel<S>> context) {
        this.context = context;
        this.model = new WingsModel(createModel(Dilation.NONE).createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider renderContext, int lightUv, S entity, float limbDistance, float limbAngle) {
        if (canRender(entity)) {
            model.setAngles(CasterState.of(entity), context.getModel());
            model.render(matrices, getBuffer(renderContext, getTexture(entity)), lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);

            Identifier overlayTexture = getOverlayTexture(entity);
            if (overlayTexture != null) {
                model.render(matrices, getBuffer(renderContext, overlayTexture), lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);
            }
        }
    }

    protected VertexConsumer getBuffer(VertexConsumerProvider vertices, Identifier texture) {
        return vertices.getBuffer(RenderLayer.getEntityTranslucent(texture));
    }

    protected boolean canRender(S state) {
        return state instanceof PlayerEntityRenderState
                && CasterState.of(state).species.physical().flightType() == FlightType.AVIAN
                && CasterState.of(state).species.physical() != Race.BAT
                && CasterState.of(state).skinFeatures.showWings()
                && !CasterState.of(state).pegasusAmulet;
    }

    protected Identifier getTexture(S state) {
        return PEGASUS_WINGS;
    }

    @Nullable
    protected Identifier getOverlayTexture(S state) {
        return PEGASUS_WINGS_OVERLAY;
    }

    private TexturedModelData createModel(Dilation dilation) {
        ModelData data = new ModelData();
        createWing("left_wing", data.getRoot(), dilation, -1);
        createWing("right_wing", data.getRoot(), dilation, 1);
        return TexturedModelData.of(data, 24, 23);
    }

    protected void createWing(String name, ModelPartData parent, Dilation dilation, int k) {
        ModelPartData base = parent.addChild(name,
                ModelPartBuilder.create().cuboid(0, 0, 0, 2, 10, 2, dilation),
                ModelTransform.pivot(k * 2, 2, 2 + k * 0.5F));

        for (int i = 0; i < FEATHER_COUNT; i++) {
            int texX = (i % 2) * 8;
            int featherLength = 21 - i * 2;
            base.addChild("feather_" + i,
                    ModelPartBuilder.create()
                        .uv(8 + texX, 0)
                        .cuboid(-k * (i % 2) / 90F, 0, 0, 2, featherLength, 2, dilation),
                    ModelTransform.pivot(0, 9, 0));
        }
    }

    private static class WingsModel extends Model {
        private final Wing leftWing;
        private final Wing rightWing;

        public WingsModel(ModelPart tree) {
            super(tree, RenderLayer::getEntityTranslucent);
            leftWing = new Wing(tree.getChild("left_wing"), -1);
            rightWing = new Wing(tree.getChild("right_wing"), 1);
        }

        public void setAngles(CasterState entity, BipedEntityModel<?> biped) {
            root.copyTransform(biped.body);
            leftWing.setAngles(entity);
            rightWing.setAngles(entity);
        }

        static class Wing {
            final ModelPart base;

            final ModelPart[] feathers = new ModelPart[FEATHER_COUNT];

            final int k;

            Wing(ModelPart tree, int k) {
                this.k = k;
                base = tree;
                for (int i = 0; i < feathers.length; i++) {
                    feathers[i] = base.getChild("feather_" + i);
                }
            }

            void setAngles(CasterState entity) {
                float spreadAmount = entity.wingsAngle;

                base.pitch = 1.5F + 0.8F - spreadAmount / 9F;
                base.yaw = k * (0.8F + spreadAmount / 3F);

                spreadAmount /= 7F;

                final float ratio = 4F;

                for (int i = 0; i < feathers.length; i++) {

                    float spread = i/ratio + 1.5F;
                    spread -= spreadAmount * ratio;
                    spread += spreadAmount * i / ratio;

                    feathers[i].pitch = -spread;
                    feathers[i].yaw = k * 0.3F;
                }
            }
        }
    }
}
