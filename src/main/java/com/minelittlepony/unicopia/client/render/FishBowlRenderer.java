package com.minelittlepony.unicopia.client.render;

import static net.minecraft.util.math.ColorHelper.fromFloats;

import java.util.Set;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.ModelPartHooks.EnqueudHeadRender;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class FishBowlRenderer {
    private static final int DIVER_HELMET_COLOR = fromFloats(0.1F, 0.5F, 0.5F, 0.5F);

    public boolean shouldRender(Equine<?> equine) {
        return equine instanceof Pony pony && pony.getCompositeRace().includes(Race.SEAPONY)
                && equine.asEntity().isSubmergedInWater()
                && MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()) != Race.SEAPONY;
    }

    public void render(
            Set<EnqueudHeadRender> headParts,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light) {
        RenderLayer layer = RenderLayers.getMagicColored();
        for (var part : headParts) {
            matrices.push();
            part.transform(matrices, 1F);
            float scale = 0.9F;

            SphereModel.SPHERE.render(matrices, vertices.getBuffer(layer), light, 0, scale, DIVER_HELMET_COLOR);
            SphereModel.SPHERE.render(matrices, vertices.getBuffer(layer), light, 0, scale + 0.2F, DIVER_HELMET_COLOR);

            matrices.pop();
        }
    }
}
