package com.minelittlepony.unicopia.redux.client.render;

import com.minelittlepony.unicopia.redux.client.render.model.SpellbookModel;
import com.minelittlepony.unicopia.redux.entity.SpellbookEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class RenderSpellbook extends LivingEntityRenderer<SpellbookEntity, SpellbookModel> {

    private static final Identifier BLUE = new Identifier("unicopia", "textures/entity/enchanting_table_book_blue.png");
    private static final Identifier NORMAL = new Identifier("unicopia", "textures/entity/enchanting_table_book.png");

    public RenderSpellbook(EntityRenderDispatcher rendermanagerIn) {
        super(rendermanagerIn, new SpellbookModel(), 0);
    }

    @Override
    protected Identifier getEntityTexture(SpellbookEntity entity) {
        return entity.getIsAltered() ? BLUE : NORMAL;
    }

    @Override
    protected float getDeathMaxRotation(SpellbookEntity entity) {
        return 0;
    }

    @Override
    protected void renderModel(SpellbookEntity entity, float time, float walkSpeed, float stutter, float yaw, float pitch, float increment) {

        float breath = MathHelper.sin(((float)entity.ticksExisted + stutter) / 20) * 0.01F + 0.1F;

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
        super.renderModel(entity, 0, first_page_rot, second_page_rot, open_angle, 0.0F, 0.0625F);

        GlStateManager.popMatrix();
    }

    @Override
    protected void applyRotations(SpellbookEntity entity, float p_77043_2_, float p_77043_3_, float partialTicks) {
        GlStateManager.rotate(-interpolateRotation(entity.prevRotationYaw, entity.rotationYaw, partialTicks), 0, 1, 0);
    }

    @Override
    protected boolean canRenderName(SpellbookEntity targetEntity) {
        return super.canRenderName(targetEntity) && (targetEntity.getAlwaysRenderNameTagForRender() || targetEntity.hasCustomName() && targetEntity == renderManager.pointedEntity);
    }
}