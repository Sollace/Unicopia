package com.minelittlepony.unicopia.client.render.entity.state;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.HeldEntityFeatureRenderer;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.AmuletSelectors;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.item.GlowableItem;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CasterState {
    public boolean present;
    public EntityType<?> type;

    public boolean living;

    public boolean ponified;

    public List<Text> debugLines;
    public float width;
    public boolean isCamera;
    public boolean isProjectile;
    public boolean isPlacement;

    public boolean showDebugInfo;
    public boolean hasDebugInfo;

    public Vec3d originVector = Vec3d.ZERO;

    public final EntityRenderState entityState;

    @Nullable
    public Text masterDisplayName;

    public List<SpellRenderState> spells = new ArrayList<>();

    public Race.Composite species = Race.UNSET.composite();

    public float wingsAngle;
    /**
     * Only use if necessary
     */
    @Deprecated @Nullable
    public AbilityDispatcher abilities;

    public final AbilityState activeAbility = new AbilityState();
    public int activeMagicColor;

    public TrinketsDelegate.EquippedStack amulet = TrinketsDelegate.EquippedStack.EMPTY;
    public boolean pegasusAmulet;
    public boolean inHell;

    public final BangleState mainhandBangle = new BangleState();
    public final BangleState offhandBangle = new BangleState();
    public TrinketsDelegate.EquippedStack eyewear = TrinketsDelegate.EquippedStack.EMPTY;

    public float leanAmount;
    public float yawOffset;
    public float gemYaw;

    public PassengerState<?, ?> carriedEntity = new PassengerState<>();

    @Nullable
    public Entity appearance;

    public CasterState(EntityRenderState entityState) {
        this.entityState = entityState;
    }

    public void clear() {
        present = false;
        living = false;
        debugLines = null;
        originVector = Vec3d.ZERO;
        isCamera = false;
        isProjectile = false;
        ponified = false;
        isPlacement = false;
        showDebugInfo = false;
        hasDebugInfo = false;
        pegasusAmulet = false;
        inHell = false;
        leanAmount = 0;
        yawOffset = 0;
        gemYaw = 0;
        type = null;
        masterDisplayName = null;
        appearance = null;
        wingsAngle = 0;
        carriedEntity = null;
        spells.clear();
        species = Race.UNSET.composite();
        amulet = TrinketsDelegate.EquippedStack.EMPTY;
        mainhandBangle.update(TrinketsDelegate.EquippedStack.EMPTY);
        offhandBangle.update(TrinketsDelegate.EquippedStack.EMPTY);
        eyewear = TrinketsDelegate.EquippedStack.EMPTY;
        activeAbility.update(null, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void update(@Nullable Caster<?> caster, float tickDelta) {
        clear();
        if (caster != null) {
            present = true;
            living = caster instanceof Living;
            hasDebugInfo = caster instanceof Pony || caster instanceof CastSpellEntity;
            type = caster.asEntity().getType();
            width = caster.asEntity().getWidth();
            originVector = caster.getOriginVector();

            MinecraftClient client = MinecraftClient.getInstance();

            isCamera = caster.asEntity() == client.cameraEntity;
            isProjectile = caster.asEntity() instanceof MagicProjectileEntity;
            isPlacement = caster.asEntity() instanceof CastSpellEntity;
            inHell = caster.asEntity().getWorld().getDimension().ultrawarm();

            showDebugInfo = hasDebugInfo
                    && client.getEntityRenderDispatcher().shouldRenderHitboxes()
                    && !client.hasReducedDebugInfo()
                    && !(caster.asEntity() == client.cameraEntity && client.options.getPerspective() == Perspective.FIRST_PERSON);

            spells = caster.getSpellSlot().stream().map(spell -> {
                SpellRenderer<?, ?> renderer = SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell);
                SpellRenderState spellState = renderer.createRenderState();
                ((SpellRenderer)renderer).updateRenderState(spell, spellState, caster, tickDelta);
                return spellState;
            }).toList();
            activeMagicColor = spells.stream().filter(i -> i.type.type() != SpellType.PLACE_CONTROL_SPELL).map(spell -> spell.type.type().getColor()).findFirst().orElse(0);

            appearance = caster.getSpellSlot().get(SpellPredicate.IS_DISGUISE)
                    .map(Disguise.class::cast)
                    .flatMap(Disguise::getAppearance)
                    .map(EntityAppearance::getAppearance)
                    .orElse(null);

            if (caster instanceof Pony pony) {
                species = pony.getCompositeRace();
                abilities = pony.getAbilities();
                wingsAngle = pony.getMotion().getWingAngle();
                activeAbility.update(pony.getAbilities().getActiveStat().orElse(null), pony);
                ponified = MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()).isEquine();
            }

            if (caster instanceof Living l) {
                amulet = AmuletItem.get(l.asEntity());
                pegasusAmulet = AmuletSelectors.PEGASUS_AMULET.test(l.asEntity());
                mainhandBangle.update(FriendshipBraceletItem.getWornBangles(l.asEntity(), TrinketsDelegate.MAIN_GLOVE).findFirst().orElse(null));
                offhandBangle.update(FriendshipBraceletItem.getWornBangles(l.asEntity(), TrinketsDelegate.SECONDARY_GLOVE).findFirst().orElse(null));
                eyewear = GlassesItem.getForEntity(l.asEntity());
                leanAmount = ((LivingEntityDuck)l.asEntity()).getLeaningPitch();
                yawOffset = -(((LivingEntityRenderState)this.entityState).yawDegrees + ((LivingEntityRenderState)this.entityState).bodyYaw);
                gemYaw = l.asEntity().isSleeping() ? 0 : 180 - ((LivingEntityRenderState)this.entityState).bodyYaw;
            }

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

    public static CasterState of(Entity entity, float tickDelta) {
        return of(MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity).getAndUpdateRenderState(entity, tickDelta));
    }

    public static class BangleState {
        public TrinketsDelegate.EquippedStack stack = TrinketsDelegate.EquippedStack.EMPTY;
        public int color;
        public boolean glowing;

        public void update(@Nullable TrinketsDelegate.EquippedStack stack) {
            this.stack = stack;
            color = stack == null ? Colors.WHITE : DyedColorComponent.getColor(stack.stack(), Colors.WHITE);
            glowing = stack != null && GlowableItem.isGlowing(stack.stack());
        }

        public boolean present() {
            return !stack.stack().isEmpty();
        }
    }

    public static class AbilityState {
        public Identifier id;
        public Text name;
        public int color;
        public float fillProgress;

        public void update(@Nullable AbilityDispatcher.Stat stat, Pony pony) {
            Ability<?> ability = stat == null ? null : stat.getActiveAbility().orElse(null);
            color = ability == null ? 0 : ability.getColor(pony);
            id = ability == null ? null : ability.getId();
            name = ability == null ? null : ability.getName(pony);
            fillProgress = stat == null ? 0 : stat.getFillProgress();
        }
    }

    public static class PassengerState<T extends LivingEntity, S extends LivingEntityRenderState> {
        public Vec3d carryPosition = Vec3d.ZERO;
        public Vector3f viewportPosition = new Vector3f();
        public boolean isPony;

        @Nullable
        private EntityRenderer<T, S> renderer;
        @Nullable
        public S state;

        @SuppressWarnings("unchecked")
        public void update(CasterState state, Living<T> entity, Living<T> passenger, float tickDelta) {
            if (passenger != null) {
                isPony = passenger instanceof Pony;
                carryPosition = HeldEntityFeatureRenderer.getCarryPosition(entity, passenger)
                        .rotateX(-state.leanAmount * MathHelper.PI / 4F)
                        .add(new Vec3d(0, -0.5F, 0).multiply(state.leanAmount));
                viewportPosition = HeldEntityFeatureRenderer.getViewportPosition(entity, passenger, tickDelta);
                this.renderer = (EntityRenderer<T, S>)MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity.asEntity());
                this.state = renderer.createRenderState();
                renderer.updateRenderState(entity.asEntity(), this.state, tickDelta);
                this.state.yawDegrees = 0;
                this.state.bodyYaw = 0;
                this.state.limbAmplitudeMultiplier = 0;
            } else {
                this.renderer = null;
                this.state = null;
            }
        }

        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
            renderer.render(state, matrices, vertexConsumers, light);
        }
    }
}
