package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.spell.RainboomAbilitySpell;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class RainboomSpellRenderer extends SpellRenderer<RainboomAbilitySpell, SpellRenderState> {
    @Override
    public SpellRenderState createRenderState() {
        return new SpellRenderState();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, SpellRenderState spell, CasterState caster,  int light) {

    }
}
