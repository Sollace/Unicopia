package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class EmptySpellRenderer<T extends Spell> extends SpellRenderer<T, SpellRenderState> {
    @Override
    public SpellRenderState createRenderState() {
        return new SpellRenderState();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, SpellRenderState spell,  int light) {

    }
}
