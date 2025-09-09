package com.minelittlepony.unicopia.client.render.entity.state;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class CasterState {
    public boolean present;
    public EntityType<?> type;

    public boolean living;

    public List<Text> debugLines;
    public float width;
    public boolean isCamera;
    public boolean isProjectile;
    public boolean isPlacement;

    public boolean showDebugInfo;
    public boolean hasDebugInfo;

    public Vec3d originVector = Vec3d.ZERO;

    public final LivingEntityRenderState entityState;
    @Nullable
    public Text masterDisplayName;

    public List<SpellRenderState> spells = new ArrayList<>();

    public CasterState(LivingEntityRenderState entityState) {
        this.entityState = entityState;
    }

    public void clear() {
        present = false;
        living = false;
        debugLines = null;
        originVector = Vec3d.ZERO;
        isCamera = false;
        isProjectile = false;
        isPlacement = false;
        showDebugInfo = false;
        hasDebugInfo = false;
        type = null;
        masterDisplayName = null;
        spells.clear();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void update(@Nullable Caster<?> caster, float tickDelta) {
        clear();
        present = caster != null;
        living = caster instanceof Living;
        hasDebugInfo = caster instanceof Pony || caster instanceof CastSpellEntity;
        if (caster != null) {
            type = caster.asEntity().getType();
            width = caster.asEntity().getWidth();
            originVector = caster.getOriginVector();

            MinecraftClient client = MinecraftClient.getInstance();

            isCamera = caster.asEntity() == client.cameraEntity;
            isProjectile = caster.asEntity() instanceof MagicProjectileEntity;
            isPlacement = caster.asEntity() instanceof CastSpellEntity;

            showDebugInfo = hasDebugInfo
                    && client.getEntityRenderDispatcher().shouldRenderHitboxes()
                    && !client.hasReducedDebugInfo()
                    && !(caster.asEntity() == client.cameraEntity && client.options.getPerspective() == Perspective.FIRST_PERSON);

            spells = caster.getSpellSlot().stream().map(spell -> {
                SpellRenderer<?, ?> renderer = SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell);
                SpellRenderState spellState = renderer.createRenderState();
                ((SpellRenderer)renderer).updateRenderState(spell, spellState, this, tickDelta);
                return spellState;
            }).toList();

            if (client.getEntityRenderDispatcher().shouldRenderHitboxes()
                    && !client.hasReducedDebugInfo()
                    && !(caster.asEntity() == client.cameraEntity && client.options.getPerspective() == Perspective.FIRST_PERSON)) {
                if (caster instanceof Pony || caster instanceof CastSpellEntity) {
                    debugLines = Stream.concat(
                            Stream.of(
                                    caster.asEntity().getDisplayName().copy().append(" (" + Registries.ENTITY_TYPE.getId(caster.asEntity().getType()) + ")"),
                                    caster.getMaster() != null ? Text.literal("Master: ").append(caster.getMaster().getDisplayName()) : Text.empty()
                            ),
                            caster.getSpellSlot().stream(SpellPredicate.ALL).flatMap(spell ->
                                Stream.of(
                                        Text.literal("UUID: " + spell.getUuid()),
                                        Text.literal("|>Type: ").append(Text.literal(spell.getTypeAndTraits().type().getId().toString()).styled(s -> s.withColor(spell.getTypeAndTraits().type().getColor()))),
                                        Text.of("|>Traits: " + spell.getTypeAndTraits().traits()),
                                        Text.literal("|>HasRenderer: ").append(Text.literal((SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell) != null) + "").formatted(SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell) != null ? Formatting.GREEN : Formatting.RED))
                                )
                            )
                    ).toList();
                }
            }

            LivingEntity master = caster.getMaster();
            if (master != null) {
                masterDisplayName = master.getDisplayName();
            }
        }
    }

    public interface Container {
        CasterState getUnicopiaState();
    }

    public static CasterState of(EntityRenderState state) {
        return ((Container)state).getUnicopiaState();
    }
}
