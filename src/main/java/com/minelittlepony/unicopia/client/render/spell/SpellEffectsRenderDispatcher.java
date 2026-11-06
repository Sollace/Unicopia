package com.minelittlepony.unicopia.client.render.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpellEffectsRenderDispatcher implements SynchronousResourceReloader, IdentifiableResourceReloadListener {
    public static final SpellEffectsRenderDispatcher INSTANCE = new SpellEffectsRenderDispatcher();
    private static final Identifier ID = Unicopia.id("spell_renderers");
    private static final Map<SpellType<?>, SpellRendererFactory<?>> REGISTRY = new HashMap<>();

    public static <T extends Spell> void register(SpellType<T> type, SpellRendererFactory<? super T> rendererFactory) {
        REGISTRY.put(type, rendererFactory);
    }

    static {
        register(SpellType.SHIELD, ShieldSpellRenderer::new);
        register(SpellType.DARK_VORTEX, DarkVortexSpellRenderer::new);
        register(SpellType.BUBBLE, BubbleSpellRenderer::new);
        register(SpellType.PORTAL, PortalSpellRenderer::new);
        register(SpellType.RAINBOOM, EmptySpellRenderer::new);
    }

    @Nullable
    private Map<SpellType<?>, SpellRenderer<?, ?>> renderers = Map.of();
    private final MinecraftClient client = MinecraftClient.getInstance();

    private SpellEffectsRenderDispatcher() {}

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @SuppressWarnings("unchecked")
    public <S extends Spell> SpellRenderer<S, ?> getRenderer(S spell) {
        return (SpellRenderer<S, ?>)renderers.getOrDefault(spell.getTypeAndTraits().type(), SpellRenderer.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public <S extends SpellRenderState> SpellRenderer<?, S> getRenderer(S spell) {
        Objects.requireNonNull(spell.type);
        return (SpellRenderer<?, S>)renderers.getOrDefault(spell.type.type(), SpellRenderer.DEFAULT);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, SpellRenderState spell) {
        var renderer = getRenderer(spell);

        if (spell.casterState.debugLines != null) {
            renderHotspot(matrices, vertices, spell.casterState);
            renderSpellDebugInfo(matrices, vertices, spell.casterState, light);
        }

        if (renderer != SpellRenderer.DEFAULT) {
            client.getBufferBuilders().getEntityVertexConsumers().draw();
            renderer.render(matrices, vertices, spell, light);
        }
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, CasterState caster) {
        if (!((RenderDispatcherAccessor)client.getEntityRenderDispatcher()).shouldRenderShadows()) {
            return;
        }

        caster.spells.forEach(spell -> {
            var renderer = getRenderer(spell);
            if (renderer != SpellRenderer.DEFAULT) {
                client.getBufferBuilders().getEntityVertexConsumers().draw();
                renderer.render(matrices, vertices, spell, light);
            }
        });

        if (caster.debugLines != null) {
            renderHotspot(matrices, vertices, caster);
            renderSpellDebugInfo(matrices, vertices, caster, light);
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        renderers = REGISTRY.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().create()));
    }

    private void renderSpellDebugInfo(MatrixStack matrices, VertexConsumerProvider vertices, CasterState caster, int light) {
        matrices.push();
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());

        float scale = 0.0125F;
        int rotationMultiple = caster.living ? 1 : -1;
        matrices.scale(scale, scale * rotationMultiple, scale * rotationMultiple);
        int backgroundColor = MinecraftClient.getInstance().options.getTextBackgroundColor(0.25F);
        int spacing = client.textRenderer.fontHeight + 1;
        int top = -(spacing * caster.debugLines.size());
        int left = (int)caster.entityState.width * 64;

        Matrix4f transform = matrices.peek().getPositionMatrix();
        for (Text line : caster.debugLines) {
            client.textRenderer.draw(line, left += 1, top += spacing, Colors.WHITE, false, transform, vertices, TextLayerType.SEE_THROUGH, backgroundColor, light);
        }
        matrices.pop();
    }

    private void renderHotspot(MatrixStack matrices, VertexConsumerProvider vertices, CasterState caster) {
        Box boundingBox = Box.of(Vec3d.ZERO, 0.5, 0.5, 0.5);
        VertexConsumer buffer = vertices.getBuffer(RenderLayer.getLines());
        Quaternionf rotation = new Quaternionf();

        for (float i = -1; i < 1; i += 0.2F) {
            matrices.push();
            float angle = i * caster.entityState.age * MathHelper.RADIANS_PER_DEGREE;
            matrices.multiply(rotation.rotationXYZ(angle, angle - caster.yawOffset, angle));
            matrices.scale(i, i, i);
            VertexRendering.drawBox(matrices, buffer, boundingBox, 1, 0, 0, 1);
            matrices.pop();
        }
    }

    public interface RenderDispatcherAccessor {
        boolean shouldRenderShadows();
    }
}
