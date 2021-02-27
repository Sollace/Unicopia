package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class WingsFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    private static final Identifier ICARUS_WINGS = new Identifier("unicopia", "textures/models/wings/icarus.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = new Identifier("unicopia", "textures/models/wings/icarus_corrupted.png");
    private static final Identifier PEGASUS_WINGS = new Identifier("unicopia", "textures/models/wings/pegasus.png");

    private final WingsModel model = new WingsModel();

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public WingsFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {

        boolean pegasus = (entity instanceof PlayerEntity && Pony.of((PlayerEntity)entity).getSpecies().canInteractWithClouds());
        boolean icarus = UItems.PEGASUS_AMULET.isApplicable(entity);

        if (icarus || pegasus) {
            Identifier texture = pegasus ? PEGASUS_WINGS : entity.world.getDimension().isUltrawarm() ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getEntityTranslucent(texture), false, false);

            model.setAngles(entity, context.getModel());
            model.render(matrices, consumer, lightUv, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        }
    }

    static class WingsModel extends Model {

        private final ModelPart root;
        private final Wing[] wings;

        public WingsModel() {
            super(RenderLayer::getEntityTranslucent);
            this.textureHeight = 23;
            this.textureWidth = 24;
            root = new ModelPart(this, 0, 0);
            wings = new Wing[] {
                    new Wing(this, root, -1),
                    new Wing(this, root, 1)
            };
        }

        public void setAngles(LivingEntity entity, BipedEntityModel<?> biped) {
            root.copyPositionAndRotation(biped.torso);
            for (Wing wing : wings) {
                wing.setAngles(entity);
            }
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            root.render(matrices, vertexConsumer, i, j, f, g, h, k);
        }

        static class Wing {
            ModelPart base;

            ModelPart[] feathers;

            int k;

            Wing(Model model, ModelPart torso, int k) {
                this.k = k;
                base = new ModelPart(model, 0, 0);

                base.setPivot(k * 2, 2, 2 + k * 0.5F);
                base.addCuboid(0, 0, 0, 2, 10, 2);

                feathers = new ModelPart[8];

                for (int i = 0; i < feathers.length; i++) {
                    int texX = (i % 2) * 8;

                    ModelPart feather = new ModelPart(model, 8 + texX, 0);
                    feather.setPivot(0, 9, 0);

                    int featherLength = 21 - i * 2;

                    feather.addCuboid(-k * (i % 2) / 90F, 0, 0, 2, featherLength, 2);

                    base.addChild(feather);
                    feathers[i] = feather;
                }

                torso.addChild(base);
            }

            void setAngles(LivingEntity entity) {
                if (entity instanceof PlayerEntity) {
                    Pony pony = Pony.of((PlayerEntity)entity);

                    float spreadAmount = pony.getMotion().getWingAngle();

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
}
