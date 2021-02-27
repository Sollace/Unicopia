package com.minelittlepony.unicopia.client.render;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AmuletFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    private final BraceletModel model = new BraceletModel(0.3F);

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public AmuletFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {

        ItemStack stack = entity.getEquippedStack(EquipmentSlot.CHEST);
        Item item = stack.getItem();

        boolean amulet = item instanceof AmuletItem;
        boolean wings =
                (amulet && ((AmuletItem)item).isApplicable(stack))
                || (entity instanceof PlayerEntity && Pony.of((PlayerEntity)entity).getSpecies().canInteractWithClouds());

        if (wings || amulet) {
            Identifier texture = textures.computeIfAbsent(Registry.ITEM.getId(amulet ? item : UItems.PEGASUS_AMULET), id -> new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + ".png"));

            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(texture), false, false);

            model.setVisible(amulet, wings);
            model.setAngles(entity, context.getModel());
            model.render(matrices, consumer, lightUv, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        }
    }

    static class BraceletModel extends Model {

        private final ModelPart torso;
        private final ModelPart amulet;

        private final Wing[] wings;

        public BraceletModel(float dilate) {
            super(RenderLayer::getEntityTranslucent);
            torso = new ModelPart(this, 0, 0);
            amulet = new ModelPart(this, 0, 0);
            amulet.addCuboid(-4, 0, -2, 8, 12, 4, dilate);
            amulet.setPivot(0, 0, 0);
            torso.addChild(amulet);
            wings = new Wing[] {
                    new Wing(this, torso, -1),
                    new Wing(this, torso, 1)
            };
        }

        public void setVisible(boolean amulet, boolean wings) {
            this.amulet.visible = amulet;
            for (Wing wing : this.wings) {
                wing.base.visible = wings;
            }
        }

        public void setAngles(LivingEntity entity, BipedEntityModel<?> biped) {
            torso.copyPositionAndRotation(biped.torso);
            for (Wing wing : wings) {
                wing.setAngles(entity);
            }
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            torso.render(matrices, vertexConsumer, i, j, f, g, h, k);
        }

        static class Wing {
            ModelPart base;

            ModelPart[] feathers;

            int k;

            Wing(Model model, ModelPart torso, int k) {
                this.k = k;
                base = new ModelPart(model, 0, 16);

                base.setPivot(k * 2, 2, 2 + k * 0.5F);
                base.addCuboid(0, 0, 0, 2, 10, 2);

                feathers = new ModelPart[8];

                for (int i = 0; i < feathers.length; i++) {
                    int texX = (i % 2) * 8;

                    ModelPart feather = new ModelPart(model, 24 + texX, 0);
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
