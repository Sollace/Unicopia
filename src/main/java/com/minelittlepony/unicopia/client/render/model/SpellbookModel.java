package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.entity.SpellbookEntity;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

public class SpellbookModel extends EntityModel<SpellbookEntity> {
    private final Cuboid leftCover = (new Cuboid(this)).setTextureOffset(0, 0).addBox(-6.0F, -5.0F, 0.0F, 6, 10, 0);
    private final Cuboid rightCover = (new Cuboid(this)).setTextureOffset(16, 0).addBox(0.0F, -5.0F, 0.0F, 6, 10, 0);
    private final Cuboid leftBlock = (new Cuboid(this)).setTextureOffset(0, 10).addBox(0.0F, -4.0F, -0.99F, 5, 8, 1);
    private final Cuboid rightBlock = (new Cuboid(this)).setTextureOffset(12, 10).addBox(0.0F, -4.0F, -0.01F, 5, 8, 1);
    private final Cuboid leftPage = (new Cuboid(this)).setTextureOffset(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
    private final Cuboid rightPage = (new Cuboid(this)).setTextureOffset(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
    private final Cuboid spine = (new Cuboid(this)).setTextureOffset(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2, 10, 0);

    public SpellbookModel() {
       this.leftCover.setRotationPoint(0.0F, 0.0F, -1.0F);
       this.rightCover.setRotationPoint(0.0F, 0.0F, 1.0F);
       this.spine.yaw = 1.5707964F;
    }

    @Override
    public void render(SpellbookEntity entity, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6) {
       this.leftCover.render(float_6);
       this.rightCover.render(float_6);
       this.spine.render(float_6);
       this.leftBlock.render(float_6);
       this.rightBlock.render(float_6);
       this.leftPage.render(float_6);
       this.rightPage.render(float_6);
    }

    @Override
    public void setAngles(SpellbookEntity entity, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6) {
       float float_7 = (MathHelper.sin(float_1 * 0.02F) * 0.1F + 1.25F) * float_4;
       this.leftCover.yaw = 3.1415927F + float_7;
       this.rightCover.yaw = -float_7;
       this.leftBlock.yaw = float_7;
       this.rightBlock.yaw = -float_7;
       this.leftPage.yaw = float_7 - float_7 * 2.0F * float_2;
       this.rightPage.yaw = float_7 - float_7 * 2.0F * float_3;
       this.leftBlock.rotationPointX = MathHelper.sin(float_7);
       this.rightBlock.rotationPointX = MathHelper.sin(float_7);
       this.leftPage.rotationPointX = MathHelper.sin(float_7);
       this.rightPage.rotationPointX = MathHelper.sin(float_7);
    }
}
