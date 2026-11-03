package com.minelittlepony.unicopia.client.render.spell;

import java.util.UUID;

import org.joml.Quaternionf;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public abstract class SpellRenderer<T extends Spell, S extends SpellRenderer.SpellRenderState> {
    public static final SpellRenderer<?, ?> DEFAULT = new SpellRenderer<>() {
        @Override
        public SpellRenderState createRenderState() {
            return new SpellRenderState();
        }
    };

    protected final MinecraftClient client = MinecraftClient.getInstance();

    private final S state = createRenderState();

    public final S getAndUpdateRenderState(T spell, CasterState casterState, Caster<?> caster, float tickDelta) {
        updateRenderState(spell, state, casterState, caster, tickDelta);
        return state;
    }

    public void updateRenderState(T spell, S state, CasterState casterState, Caster<?> caster, float tickDelta) {
        state.casterState = casterState;
        state.uuid = spell.getUuid();
        state.type = spell.getTypeAndTraits();
    }

    public abstract S createRenderState();

    public boolean shouldRenderEffectPass(int pass) {
        return true;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, S spell, int light) {
        if (spell.casterState.isCamera || spell.casterState.isProjectile) {
            return;
        }

        if (EquinePredicates.IS_CASTER.test(client.player)) {
            renderGemstone(matrices, vertices, spell, light);
        }
    }

    private void renderGemstone(MatrixStack matrices, VertexConsumerProvider vertices, S spell, int light) {

        matrices.push();
        float scale = 1/8F;
        matrices.scale(scale, scale, scale);

        transformGemstone(matrices, vertices, spell);
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spell.casterState.entityState.age));

        client.getItemRenderer().renderItem(spell.type.getDefaultStack(), ItemDisplayContext.FIXED, light, 0, matrices, vertices, null, 0);
        matrices.pop();

        if (spell instanceof TimedSpell timed) {
            if (spell.casterState.gemYaw != 0) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spell.casterState.gemYaw));
            }
            renderCountdown(matrices, vertices, timed, spell.casterState.entityState.age);
        }

        matrices.pop();
    }

    protected void renderCountdown(MatrixStack matrices, VertexConsumerProvider vertices, TimedSpell spell, float tickDelta) {
        float radius = 0.6F;
        float timeRemaining = spell.getTimer().getPercentTimeRemaining(tickDelta);

        DrawContext context = new DrawContext(MinecraftClient.getInstance(),
                vertices instanceof Immediate m ? m : MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers()
        );
        context.getMatrices().peek().getPositionMatrix().set(matrices.peek().getPositionMatrix());
        context.getMatrices().peek().getNormalMatrix().set(matrices.peek().getNormalMatrix());
        context.getMatrices().multiply(client.getEntityRenderDispatcher().getRotation().invert(new Quaternionf()));

        DrawableUtil.drawArc(context, radius, radius + 0.3F, 0, DrawableUtil.TAU * timeRemaining,
                ColorHelper.lerp(MathHelper.clamp(timeRemaining * 4, 0, 1), Colors.BLUE, Colors.WHITE)
        );
    }

    protected void transformGemstone(MatrixStack matrices, VertexConsumerProvider vertices, S spell) {
        float y = spell.casterState.isPlacement ? 1 : -spell.casterState.entityState.height;
        matrices.translate(0, y * 8 + MathHelper.sin(spell.casterState.entityState.age / 3F) * 0.2F, 0);
    }

    public static class SpellRenderState {
        public CasterState casterState;
        public UUID uuid;
        public CustomisedSpellType<?> type;
    }
}
