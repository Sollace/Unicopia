package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.client.render.ModelQuads;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.util.Color;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.MathHelper;

public class GemEntityModel extends EntityModel<SpellcastEntity> {

    private Cuboid body;

    public GemEntityModel() {
        textureWidth = 256;
        textureHeight = 256;

        body = new Cuboid(this);
        body.y = 1.2f;

        int size = 1;

        body.boxes.add(new ModelQuads(body).addFace(
            new Vertex( size, 0,     size, 0,     0.5f),
            new Vertex(-size, 0,     size, 0.25f, 0.25f),
            new Vertex( 0, size * 2, 0, 0,     0.25f),
            new Vertex( 0, size * 2, 0, 0,     0.25f)
        ).addFace(
            new Vertex( size,  0,     size, 0,     0.25f),
            new Vertex(-size,  0,     size, 0.25f, 0),
            new Vertex( 0, -size * 2, 0, 0.25f, 0.25f),
            new Vertex( 0, -size * 2, 0, 0.25f, 0.25f)
        ).addFace(
            new Vertex(size, 0,    -size, 0.25f, 0.5f),
            new Vertex(size, 0,     size, 0.5f,  0.25f),
            new Vertex(0, size * 2, 0, 0.25f, 0.25f),
            new Vertex(0, size * 2, 0, 0.25f, 0.25f)
        ).addFace(
            new Vertex(size,  0,    -size, 0.25f, 0.25f),
            new Vertex(size,  0,     size, 0.5f,  0),
            new Vertex(0, -size * 2, 0, 0.5f,  0.25f),
            new Vertex(0, -size * 2, 0, 0.5f,  0.25f)
        ).addFace(
            new Vertex(-size, 0,    -size, 0.5f,  0.5f),
            new Vertex( size, 0,    -size, 0.75f, 0.25f),
            new Vertex( 0, size * 2, 0, 0.5f,  0.25f),
            new Vertex( 0, size * 2, 0, 0.5f,  0.25f)
        ).addFace(
            new Vertex(-size,  0,    -size, 0.5f,  0.25f),
            new Vertex( size,  0,    -size, 0.75f, 0),
            new Vertex( 0, -size * 2, 0, 0.75f, 0.25f),
            new Vertex( 0, -size * 2, 0, 0.75f, 0.25f)
        ).addFace(
            new Vertex(-size, 0,     size, 0.75f, 0.5f),
            new Vertex(-size, 0,    -size, 1,     0.25f),
            new Vertex( 0, size * 2, 0, 0.75f, 0.25f),
            new Vertex( 0, size * 2, 0, 0.75f, 0.25f)
        ).addFace(
            new Vertex(-size,  0,     size, 0.75f, 0.25f),
            new Vertex(-size,  0,    -size, 1,     0),
            new Vertex( 0, -size * 2, 0, 1,     0.25f),
            new Vertex( 0, -size * 2, 0, 1,     0.25f)
        ));
    }

    @Override
    public void render(SpellcastEntity entity, float time, float walkSpeed, float stutter, float yaw, float pitch, float scale) {

        GlStateManager.pushMatrix();

        float floatOffset = MathHelper.sin((entity.age + stutter) / 10 + entity.hoverStart) / 10 + 0.1F;
        GlStateManager.translated(0, floatOffset - entity.getEyeHeight(EntityPose.STANDING), 0);

        floatOffset = (entity.age + stutter) / 20;
        if (entity.getCurrentLevel() > 0) {
            floatOffset *= entity.getCurrentLevel() + 1;
        }

        floatOffset += entity.hoverStart;
        floatOffset *= 180 / (float)Math.PI;

        GlStateManager.pushMatrix();

        if (entity.overLevelCap()) {
            GlStateManager.translated(Math.sin(stutter) / 5, 0, Math.cos(stutter) / 5);

            GlStateManager.rotatef((float)Math.sin(stutter), 0, 1, 0);
        }

        GlStateManager.rotated(floatOffset, 0, 1, 0);

        body.render(scale);

        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFunc(1, 1);

        setLightingConditionsBrightness(0xF0F0);

        if (entity.hasEffect()) {
            int tint = entity.getEffect().getTint();
            GlStateManager.color4f(Color.r(tint), Color.g(tint), Color.b(tint), 1);
        }

        int tiers = Math.min(entity.getCurrentLevel(), 5);

        for (int i = 0; i <= tiers; i++) {
            float grow = (1 + i) * 0.2F;

            GlStateManager.scalef(1 + grow, 1 + grow, 1 + grow);
            GlStateManager.translatef(0, -grow, 0);
            renderOverlay(grow, scale);

            if (i == 5) {
                GlStateManager.pushMatrix();
                GlStateManager.rotatef(-floatOffset * 0.9F, 0, 1, 0);
                GlStateManager.translatef(0.6F, 0.8F, 0);
                GlStateManager.scalef(0.4F, 0.4F, 0.4F);
                renderOverlay(grow, scale);
                GlStateManager.popMatrix();
            }

        }
        GlStateManager.popMatrix();

        for (int i = entity.getCurrentLevel(); i > 0; i--) {
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(floatOffset / i, 0, 1, 0);
            GlStateManager.translatef(0.6F, 0, 0);
            renderOverlay(0.6F, scale);
            GlStateManager.popMatrix();
        }

        setLightingConditionsBrightness(entity.getLightmapCoordinates());

        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();

        GlStateManager.popMatrix();
    }

    protected void renderOverlay(float grow, float scale) {
        body.render(scale);
    }

    private void setLightingConditionsBrightness(int brightness) {
        int texX = brightness % 0x10000;
        int texY = brightness / 0x10000;

        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, texX, texY);
    }
}
