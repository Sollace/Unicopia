package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.render.model.SpellbookModel;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.util.math.MathHelper;
import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class RenderSpellbook extends LivingEntityRenderer<SpellbookEntity, SpellbookModel> {

    private static final Identifier BLUE = new Identifier("unicopia", "textures/entity/enchanting_table_book_blue.png");
    private static final Identifier NORMAL = new Identifier("unicopia", "textures/entity/enchanting_table_book.png");

    public RenderSpellbook(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new SpellbookModel(), 0);
    }

    @Override
    protected Identifier getTexture(SpellbookEntity entity) {
        return entity.getIsAltered() ? BLUE : NORMAL;
    }

    @Override
    protected float getLyingAngle(SpellbookEntity entity) {
        return 0;
    }

    @Override
    protected void render(SpellbookEntity entity, float time, float walkSpeed, float stutter, float yaw, float pitch, float increment) {

        float breath = MathHelper.sin((entity.age + stutter) / 20) * 0.01F + 0.1F;

        float first_page_rot = walkSpeed + (breath * 10);
        float second_page_rot = 1 - first_page_rot;
        float open_angle = 0.9f - walkSpeed;

        if (first_page_rot > 1) first_page_rot = 1;
        if (second_page_rot > 1) second_page_rot = 1;

        if (!entity.getIsOpen()) {
            GlStateManager.translatef(0, 1.44f, 0);
        } else {
            GlStateManager.translatef(0, 1.2f + breath, 0);
        }
        GlStateManager.pushMatrix();

        if (!entity.getIsOpen()) {
            first_page_rot = second_page_rot = open_angle = 0;
            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translatef(-0.25f, 0, 0);
        } else {
            GlStateManager.rotatef(-60.0F, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.enableCull();
        super.render(entity, 0, first_page_rot, second_page_rot, open_angle, 0, 0.0625F);

        GlStateManager.popMatrix();
    }

    @Override
    protected void setupTransforms(SpellbookEntity entity, float p_77043_2_, float p_77043_3_, float partialTicks) {
        GlStateManager.rotatef(-MathHelper.lerp(entity.prevYaw, entity.yaw, partialTicks), 0, 1, 0);
    }

    @Override
    protected boolean hasLabel(SpellbookEntity targetEntity) {
        return super.hasLabel(targetEntity)
                && (targetEntity.isCustomNameVisible()
                        || targetEntity.hasCustomName()
                        && targetEntity == renderManager.targetedEntity);
    }
}