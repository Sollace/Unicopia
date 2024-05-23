package com.minelittlepony.unicopia.client.render.spell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
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
    private Map<SpellType<?>, SpellRenderer<?>> renderers = Map.of();
    private final MinecraftClient client = MinecraftClient.getInstance();

    private SpellEffectsRenderDispatcher() {}

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @SuppressWarnings("unchecked")
    public <S extends Spell> SpellRenderer<S> getRenderer(S spell) {
        return (SpellRenderer<S>)renderers.getOrDefault(spell.getTypeAndTraits().type(), SpellRenderer.DEFAULT);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, Spell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        var renderer = getRenderer(spell);

        if (renderer != SpellRenderer.DEFAULT) {
            client.getBufferBuilders().getEntityVertexConsumers().draw();

            renderer.render(matrices, vertices, spell, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        }
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, Caster<?> caster, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!((RenderDispatcherAccessor)client.getEntityRenderDispatcher()).shouldRenderShadows()) {
            return;
        }

        caster.getSpellSlot().stream().forEach(spell -> {
            render(matrices, vertices, spell, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        });

        if (client.getEntityRenderDispatcher().shouldRenderHitboxes()
                && !client.hasReducedDebugInfo()
                && !(caster.asEntity() == client.cameraEntity && client.options.getPerspective() == Perspective.FIRST_PERSON)) {
            if (!(caster instanceof Pony || caster instanceof CastSpellEntity)) {
                return;
            }
            renderHotspot(matrices, vertices, caster, animationProgress);
            renderSpellDebugInfo(matrices, vertices, caster, light);
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        renderers = REGISTRY.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().create()));
    }

    private void renderSpellDebugInfo(MatrixStack matrices, VertexConsumerProvider vertices, Caster<?> caster, int light) {
        matrices.push();
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        float scale = 0.0125F;
        if (caster instanceof Living) {
            matrices.scale(scale, scale, scale);
        } else {
            matrices.scale(-scale, -scale, scale);
        }
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int j = (int)(g * 255.0f) << 24;

        List<Text> debugLines = Stream.concat(
                Stream.of(
                        caster.asEntity().getDisplayName().copy().append(" (" + Registries.ENTITY_TYPE.getId(caster.asEntity().getType()) + ")"),
                        caster.getMaster() != null ? Text.literal("Master: ").append(caster.getMaster().getDisplayName()) : Text.empty()
                ),
                caster.getSpellSlot().stream(SpellPredicate.ALL).flatMap(spell ->
                    Stream.of(
                            Text.literal("UUID: " + spell.getUuid()),
                            Text.literal("|>Type: ").append(Text.literal(spell.getTypeAndTraits().type().getId().toString()).styled(s -> s.withColor(spell.getTypeAndTraits().type().getColor()))),
                            Text.of("|>Traits: " + spell.getTypeAndTraits().traits()),
                            Text.literal("|>HasRenderer: ").append(Text.literal((getRenderer(spell) != null) + "").formatted(getRenderer(spell) != null ? Formatting.GREEN : Formatting.RED))
                    )
                )
        ).toList();

        int spacing = client.textRenderer.fontHeight + 1;
        int height = spacing * debugLines.size();
        int top = -height;
        int left = (int)caster.asEntity().getWidth() * 64;

        for (Text line : debugLines) {
            client.textRenderer.draw(line, left += 1, top += spacing, Colors.WHITE, false, matrices.peek().getPositionMatrix(), vertices, TextLayerType.NORMAL, j, light);
        }
        matrices.pop();
    }

    private void renderHotspot(MatrixStack matrices, VertexConsumerProvider vertices, Caster<?> caster, float animationProgress) {
        Box boundingBox = Box.of(caster.getOriginVector(), 1, 1, 1);

        Vec3d pos = caster.getOriginVector();

        double x = - pos.x;
        double y = - pos.y;
        double z = - pos.z;

        VertexConsumer buffer = vertices.getBuffer(RenderLayer.getLines());

        for (float i = -1; i < 1; i += 0.2F) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * animationProgress));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * animationProgress));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * animationProgress));
            matrices.scale(i, i, i);
            WorldRenderer.drawBox(matrices, buffer, boundingBox.offset(x, y, z), 1, 0, 0, 1);
            matrices.pop();
        }
    }

    public interface RenderDispatcherAccessor {
        boolean shouldRenderShadows();
    }
}
