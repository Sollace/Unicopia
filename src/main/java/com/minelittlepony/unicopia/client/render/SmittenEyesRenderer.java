package com.minelittlepony.unicopia.client.render;

import java.util.Set;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.ModelPartHooks.EnqueudHeadRender;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class SmittenEyesRenderer {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/smitten_eyes.png");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ModelPart model;

    SmittenEyesRenderer() {
        ModelData data = new ModelData();
        data.getRoot().addChild("hearts", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-4, -4, -4, 8.0f, 8.0f, 8.0f, Dilation.NONE), ModelTransform.NONE);
        model = TexturedModelData.of(data, 32, 32).createModel();
    }

    public void render(Set<EnqueudHeadRender> headParts, Entity entity, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        for (var part : headParts) {
            matrices.push();
            part.transform(matrices, 0.95F);
            float scale = 1F + (1.3F + MathHelper.sin(entity.age / 3F) * 0.06F);
            matrices.scale(scale, scale, scale);
            matrices.translate(0, 0.05F, 0);
            model.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, Colors.WHITE);

            if (client.getEntityRenderDispatcher().shouldRenderHitboxes()) {
                VertexConsumer lines = vertices.getBuffer(RenderLayer.getLines());
                VertexRendering.drawBox(matrices, lines, new Box(-0.25, 0, -0.25, 0.25, 0.25, 0.25), 1, 1, 0, 1);
                VertexRendering.drawBox(matrices, lines, new Box(-0.25, -0.25, -0.25, 0.25, 0, 0.25), 1, 0, 1, 1);
            }

            matrices.pop();
        }
    }

    public boolean isSmitten(Creature pony) {
        return pony.isSmitten() || EnchantmentUtil.getWantItNeedItLevel(pony.asEntity()) > 0;
    }
}
