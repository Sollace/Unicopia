package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class BatWingsFeatureRenderer<E extends LivingEntity> extends WingsFeatureRenderer<E> {

    private static final Identifier TEXTURE = Unicopia.id("textures/models/wings/bat.png");

    public BatWingsFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
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
    protected boolean canRender(E entity) {
        return entity instanceof PlayerEntity && Pony.of((PlayerEntity)entity).getSpecies() == Race.BAT;
    }

    @Override
    protected Identifier getTexture(E entity) {
        return TEXTURE;
    }
}
