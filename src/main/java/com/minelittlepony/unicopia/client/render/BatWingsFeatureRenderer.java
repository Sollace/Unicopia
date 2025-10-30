package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class BatWingsFeatureRenderer<S extends BipedEntityRenderState, E extends LivingEntity> extends WingsFeatureRenderer<S> {

    private static final Identifier TEXTURE = Unicopia.id("textures/models/wings/bat.png");

    public BatWingsFeatureRenderer(FeatureRendererContext<S, ? extends BipedEntityModel<S>> context) {
        super(context);
    }

    @Override
    protected void createWing(String name, ModelPartData parent, Dilation dilation, int k) {
        ModelPartData base = parent.addChild(name,
                ModelPartBuilder.create().cuboid(0, 0, 0, 2, 10, 2, dilation),
                ModelTransform.pivot(k * 2, 2, 2 + k * 0.5F));

        for (int i = 0; i < FEATHER_COUNT; i++) {
            int texX = (i % 2) * 8;
            int baseLength = 23;
            int featherLength = i < 7 ? baseLength - (i % 4 * 2) : baseLength - (i * 2);
            ModelPartData wing = base.addChild("feather_" + i,
                    ModelPartBuilder.create()
                        .uv(8 + texX, 0)
                        .cuboid(-k * (i % 2) / 90F, 0, 0, 0.02F, featherLength * 0.8F, 4, dilation),
                    ModelTransform.pivot(-i * k / 9F, 7, 0)
            );

            wing.addChild("secondary", ModelPartBuilder.create()
                    .uv(8 + texX, 0)
                    .cuboid(-k * (i % 2) / 90F, 0, 0, 0.02F, featherLength, 4, dilation),
                ModelTransform.rotation(0.2F, 0, 0)
            );
            if (i < 5) {
                wing.addChild("tertiary", ModelPartBuilder.create()
                        .uv(8 + texX, 0)
                        .cuboid(-k * (i % 2) / 90F, 0, 0, 0.02F, featherLength - 1, 4, dilation),
                    ModelTransform.rotation(-0.2F, 0, 0)
                );
            }
        }
    }

    @Override
    protected boolean canRender(S state) {
        return state instanceof PlayerEntityRenderState
                && CasterState.of(state).species.physical() == Race.BAT
                && CasterState.of(state).skinFeatures.showWings()
                && !CasterState.of(state).pegasusAmulet;
    }

    @Override
    protected Identifier getTexture(S state) {
        return TEXTURE;
    }

    @Override
    @Nullable
    protected Identifier getOverlayTexture(S state) {
        return null;
    }
}
