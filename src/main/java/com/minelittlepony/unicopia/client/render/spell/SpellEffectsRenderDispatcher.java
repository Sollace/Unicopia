package com.minelittlepony.unicopia.client.render.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
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
        register(SpellType.RAINBOOM, RainboomSpellRenderer::new);
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

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, CasterState caster, SpellRenderState spell) {
        var renderer = getRenderer(spell);

        if (renderer != SpellRenderer.DEFAULT) {
            client.getBufferBuilders().getEntityVertexConsumers().draw();
            renderer.render(matrices, vertices, spell, caster, light);
        }
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, CasterState caster) {
        if (!((RenderDispatcherAccessor)client.getEntityRenderDispatcher()).shouldRenderShadows()) {
            return;
        }

        caster.spells.forEach(spell -> {
            render(matrices, vertices, light, caster, spell);
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
        if (caster.living) {
            matrices.scale(scale, scale, scale);
        } else {
            matrices.scale(-scale, -scale, scale);
        }
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int j = (int)(g * 255.0f) << 24;

        var debugLines = Stream.concat(
                Stream.of(
                        caster.entityState.displayName.copy().append(" (" + Registries.ENTITY_TYPE.getId(caster.type) + ")"),
                        caster.masterDisplayName != null ? Text.literal("Master: ").append(caster.masterDisplayName) : Text.empty()
                ),
                caster.spells.stream().map(spell ->
                    Stream.of(
                            Text.literal("UUID: " + spell.uuid),
                            Text.literal("|>Type: ").append(Text.literal(spell.type.type().getId().toString()).styled(s -> s.withColor(spell.type.type().getColor()))),
                            Text.of("|>Traits: " + spell.type.traits())
                    )
                )
        ).toList();


        int spacing = client.textRenderer.fontHeight + 1;
        int height = spacing * debugLines.size();
        int top = -height;
        int left = (int)caster.entityState.width * 64;

        for (Text line : caster.debugLines) {
            client.textRenderer.draw(line, left += 1, top += spacing, Colors.WHITE, false, matrices.peek().getPositionMatrix(), vertices, TextLayerType.POLYGON_OFFSET, j, light);
        }
        matrices.pop();
    }

    private void renderHotspot(MatrixStack matrices, VertexConsumerProvider vertices, CasterState caster) {
        Box boundingBox = Box.of(caster.originVector, 1, 1, 1);

        Vec3d pos = caster.originVector;

        double x = - pos.x;
        double y = - pos.y;
        double z = - pos.z;

        VertexConsumer buffer = vertices.getBuffer(RenderLayer.getLines());

        for (float i = -1; i < 1; i += 0.2F) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * caster.entityState.age));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * caster.entityState.age));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * caster.entityState.age));
            matrices.scale(i, i, i);
            VertexRendering.drawBox(matrices, buffer, boundingBox.offset(x, y, z), 1, 0, 0, 1);
            matrices.pop();
        }
    }

    public interface RenderDispatcherAccessor {
        boolean shouldRenderShadows();
    }
}
