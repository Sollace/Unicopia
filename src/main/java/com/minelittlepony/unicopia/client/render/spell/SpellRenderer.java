package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public interface SpellRenderer<T extends Spell> {
    void render(MatrixStack matrices, VertexConsumerProvider vertices, T spell, Caster<?> caster,  int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch);
}
