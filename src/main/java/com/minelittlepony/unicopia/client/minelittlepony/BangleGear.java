package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer.BraceletModel;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

class BangleGear implements Gear {
    @Nullable
    private CasterState.BangleState state;
    private boolean alex;

    private final BraceletModel steveModel;
    private final BraceletModel alexModel;

    private final Identifier slot;

    public BangleGear(Identifier slot) {
        this.slot = slot;
        Dilation dilation = new Dilation(0.3F);
        steveModel = new BraceletModel(BraceletModel.getData(dilation, false, -1, 4, 0).createModel());
        alexModel = new BraceletModel(BraceletModel.getData(dilation, true, -1, 4, 0).createModel());
    }

    @Override
    public boolean canRender(PonyModel<?> model, EntityRenderState state) {
        return (slot == TrinketsDelegate.MAIN_GLOVE ? CasterState.of(state).mainhandBangle : CasterState.of(state).offhandBangle).present();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.LEGS;
    }

    @Override
    public <S extends EntityRenderState> Identifier getTexture(S entity, Context<S, ?> context) {
        return BraceletFeatureRenderer.TEXTURE;
    }

    @Override
    public <S extends EntityRenderState & PonyModel.AttributedHolder> void transform(S state, PonyModel<S> model, MatrixStack matrices) {
        BodyPart part = getGearLocation();
        model.transform(state, part, matrices);
    }

    @Override
    public <S extends BipedEntityRenderState & PonyModel.AttributedHolder> void pose(PonyModel<S> model, S state, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        alex = state instanceof PlayerEntityRenderState player && player.skinTextures.model() == Model.SLIM;
        this.state = slot == TrinketsDelegate.MAIN_GLOVE ? CasterState.of(state).mainhandBangle : CasterState.of(state).offhandBangle;
        BraceletModel m = alex ? alexModel : steveModel;

        if (model instanceof BipedEntityModel<?> biped) {
            m.setAngles(biped);
        }
        Arm mainArm = state.mainArm;
        m.setVisible(slot == TrinketsDelegate.MAIN_GLOVE ? mainArm : mainArm.getOpposite());
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        if (state != null) {
            BraceletModel m = alex ? alexModel : steveModel;
            m.render(stack, consumer, state.glowing ? 0x0F00F0 : light, overlay, state.color);
        }
    }
}
