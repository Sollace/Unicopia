package com.minelittlepony.unicopia.client.gui;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.*;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.sound.*;
import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.effect.SunBlindnessStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

public class UHud {

    public static final UHud INSTANCE = new UHud();

    public static final Identifier HUD_TEXTURE = Unicopia.id("textures/gui/hud.png");

    public static final int PRIMARY_SLOT_SIZE = 49;

    private static final float EQUIPPED_GEMSTONE_SCALE = 0.7F;

    public TextRenderer font;

    final MinecraftClient client = MinecraftClient.getInstance();

    private final List<Slot> slots = List.of(
        new ManaRingSlot(this, AbilitySlot.PRIMARY, AbilitySlot.PASSIVE, 0, 0),
        new Slot(this, AbilitySlot.SECONDARY, AbilitySlot.SECONDARY, 26, -5),
        new Slot(this, AbilitySlot.TERTIARY, AbilitySlot.TERTIARY, 36, 19)
    );

    @Nullable
    private Text message;
    private int messageTime;

    int xDirection;

    @Nullable
    private LoopingSoundInstance<PlayerEntity> heartbeatSound;
    @Nullable
    private LoopingSoundInstance<PlayerEntity> partySound;

    private boolean prevPointed;
    private boolean prevReplacing;
    private SpellType<?> focusedType = SpellType.empty();

    public void render(InGameHud hud, DrawContext context, float tickDelta) {

        // TODO: Check this when backporting!
        // InGameHud#renderHotbar line 460
        // context.getMatrices().translate(0.0f, 0.0f, -90.0f);
        final int hotbarZ = -90;

        if (client.player == null) {
            return;
        }

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        MatrixStack matrices = context.getMatrices();

        Pony pony = Pony.of(client.player);

        matrices.push();
        matrices.translate(0, 0, hotbarZ);
        renderViewEffects(pony, context, scaledWidth, scaledHeight, tickDelta);
        matrices.pop();

        if (client.currentScreen instanceof HidesHud || client.player.isSpectator() || client.options.hudHidden) {
            return;
        }

        font = client.textRenderer;
        xDirection = client.player.getMainArm() == Arm.LEFT ? -1 : 1;


        matrices.push();
        matrices.translate(scaledWidth / 2, scaledHeight / 2, 0);

        float flapCooldown = pony.getPhysics().getFlapCooldown(tickDelta);
        if (flapCooldown > 0) {
            float angle = MathHelper.TAU * flapCooldown;
            DrawableUtil.drawArc(context.getMatrices(), 3, 6, -angle / 2F, angle, 0x888888AF, false);
        }

        matrices.pop();
        matrices.push();

        int hudX = ((scaledWidth - 50) / 2) + (104 * xDirection);
        int hudY = scaledHeight - 50;
        int hudZ = hotbarZ;


        float exhaustion = pony.getMagicalReserves().getExhaustion().getPercentFill();

        if (exhaustion > 0.5F) {
            Random rng = client.world.random;
            hudX += rng.nextFloat() - 0.5F;
            hudY += rng.nextFloat() - 0.5F;
            hudZ += rng.nextFloat() - 0.5F;
        }

        matrices.translate(hudX, hudY, hudZ);

        AbilityDispatcher abilities = pony.getAbilities();

        if (message != null && messageTime > 0) {
            renderMessage(context, tickDelta);
        }

        RenderSystem.setShaderColor(1, 1, 1,1);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, HUD_TEXTURE);

        boolean swap = client.options.sneakKey.isPressed();

        slots.forEach(slot -> slot.renderBackground(context, abilities, swap, tickDelta));

        if (pony.getObservedSpecies().canCast()) {
            Ability<?> ability = pony.getAbilities().getStat(AbilitySlot.PRIMARY)
                    .getAbility(Unicopia.getConfig().hudPage.get())
                    .orElse(null);

            if (ability == Abilities.CAST || ability == Abilities.SHOOT) {
                matrices.push();
                matrices.translate(PRIMARY_SLOT_SIZE / 2F, PRIMARY_SLOT_SIZE / 2F, 0);
                boolean first = !pony.asEntity().isSneaking();
                TypedActionResult<CustomisedSpellType<?>> inHand = pony.getCharms().getSpellInHand(false);
                boolean replacing = inHand.getResult().isAccepted() && pony.getAbilities().getStat(AbilitySlot.PRIMARY).getActiveAbility().isEmpty();
                if (first != prevPointed || replacing != prevReplacing || inHand.getValue().type() != focusedType) {
                    focusedType = inHand.getValue().type();
                    prevPointed = first;
                    prevReplacing = replacing;
                    setMessage(ability.getName(pony));
                }
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(first ? 37 : 63));
                matrices.translate(-23, 0, 0);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-26));
                matrices.scale(0.8F, 0.8F, 1);
                int u = replacing ? 16 : 3;
                context.drawTexture(HUD_TEXTURE, 0, 0, u, 120, 13, 7, 128, 128);
                matrices.pop();
            }
        }

        slots.forEach(slot -> slot.renderLabel(context, abilities, tickDelta));

        matrices.pop();

        if (pony.getObservedSpecies().canCast()) {
            renderSpell(context, pony.getCharms().getEquippedSpell(Hand.MAIN_HAND), hudX + 10 - xDirection * 13, hudY + 2);
            renderSpell(context, pony.getCharms().getEquippedSpell(Hand.OFF_HAND), hudX + 8 - xDirection * 2, hudY - 6);
        }

        RenderSystem.disableBlend();
    }

    public void renderSpell(DrawContext context, CustomisedSpellType<?> spell, double x, double y) {
        if (spell.isEmpty()) {
            return;
        }

        Pony pony = Pony.of(client.player);

        if (spell.isOn(pony)) {
            MatrixStack modelStack = context.getMatrices();

            modelStack.push();
            modelStack.translate(x + 5.5, y + 5.5, 0);

            int color = spell.type().getColor() | 0x000000FF;
            double radius = 2 + Math.sin(client.player.age / 9D) / 4;

            DrawableUtil.drawArc(modelStack, radius, radius + 3, 0, DrawableUtil.TAU, color & 0xFFFFFF2F, false);
            DrawableUtil.drawArc(modelStack, radius + 3, radius + 4, 0, DrawableUtil.TAU, color & 0xFFFFFFAF, false);
            pony.getSpellSlot().get(spell.and(SpellPredicate.IS_TIMED), false).map(TimedSpell::getTimer).ifPresent(timer -> {
                DrawableUtil.drawArc(modelStack, radius, radius + 3, 0, DrawableUtil.TAU * timer.getPercentTimeRemaining(client.getTickDelta()), 0xFFFFFFFF, false);
            });

            long count = pony.getSpellSlot().stream(spell, false).count();
            if (count > 1) {
                modelStack.push();
                modelStack.translate(1, 1, 900);
                modelStack.scale(0.8F, 0.8F, 0.8F);
                context.drawText(font, count > 64 ? "64+" : String.valueOf(count), 0, 0, 0xFFFFFFFF, true);
                modelStack.pop();
            }

            modelStack.pop();
        }

        DrawableUtil.renderItemIcon(context, spell.getDefaultStack(), x, y, EQUIPPED_GEMSTONE_SCALE);
    }

    private void renderMessage(DrawContext context, float tickDelta) {
        float time = messageTime - tickDelta;
        int progress = Math.min(255, (int)(time * 255F / 20F));

        if (progress > 8) {
            int color = 0xFFFFFF;
            int alpha = progress << 24 & -16777216;

            color |= alpha;

            context.drawCenteredTextWithShadow(font, message, 25, -15, color);
        }
    }

    protected void renderViewEffects(Pony pony, DrawContext context, int scaledWidth, int scaledHeight, float tickDelta) {

        boolean hasEffect = client.player.hasStatusEffect(UEffects.SUN_BLINDNESS);

        ItemStack glasses = GlassesItem.getForEntity(client.player);
        boolean hasSunglasses = glasses.getItem() == UItems.SUNGLASSES;

        if (hasEffect || (!hasSunglasses && pony.getObservedSpecies() == Race.BAT && SunBlindnessStatusEffect.hasSunExposure(client.player))) {
            float i = hasEffect ? (client.player.getStatusEffect(UEffects.SUN_BLINDNESS).getDuration() - tickDelta) / SunBlindnessStatusEffect.MAX_DURATION : 0;

            float pulse = (1 + (float)Math.sin(client.player.age / 108F)) * 0.25F;

            float strength = MathHelper.clamp(pulse + i, 0.3F, 1F);

            int alpha1 = (int)(strength * 205);
            int alpha2 = (int)(alpha1 * 0.6F);
            int color = 0xFFFFFF;

            if (hasEffect) {
                GradientUtil.fillRadialGradient(context.getMatrices(), 0, 0, scaledWidth, scaledHeight,
                        color | (alpha1 << 24),
                        color | (alpha2 << 24),
                        0, 1);
            } else {
                GradientUtil.fillVerticalGradient(context.getMatrices(), 0, 0, scaledHeight / 2, scaledWidth, scaledHeight,
                        color | (alpha1 << 24),
                        color | (alpha2 << 24),
                        color | (alpha1 << 24),
                        0);
            }
        }

        if (hasSunglasses) {

            if (glasses.hasCustomName() && "Cool Shades".equals(glasses.getName().getString())) {
                final int delay = 7;
                final int current = client.player.age / delay;
                final int tint = DyeColor.byId(current % DyeColor.values().length).getSignColor();
                context.fillGradient(0, 0, scaledWidth, scaledHeight, 0x1F000000 | tint, 0x5F000000 | tint);

                if (partySound == null || partySound.isDone()) {
                    client.getSoundManager().play(
                            partySound = new LoopingSoundInstance<>(client.player, player -> {
                                return UItems.SUNGLASSES.isApplicable(player) || true;
                            }, SoundEvents.MUSIC_DISC_PIGSTEP, 1, 1, client.world.random)
                    );
                } else if (partySound != null) {
                    partySound.setMuted(false);
                }
            } else {
                if (partySound != null) {
                    partySound.setMuted(true);
                }
                context.fillGradient(0, 0, scaledWidth, scaledHeight, 0x0A000088, 0x7E000000);
            }
        } else {
            if (partySound != null) {
                partySound.setMuted(true);
            }
        }

        if (UItems.ALICORN_AMULET.isApplicable(client.player)) {
            float radius = (float)pony.getArmour().getTicks(UItems.ALICORN_AMULET) / (5 * ItemTracker.DAYS);
            renderVignette(context, 0x000000, radius, radius, scaledWidth, scaledHeight);
        }

        float exhaustion = MathHelper.clamp(pony.getMagicalReserves().getExhaustion().getPercentFill(), 0, 0.6F);

        if (exhaustion > 0) {
            if (exhaustion > 0.5F && (heartbeatSound == null || heartbeatSound.isDone())) {
                client.getSoundManager().play(
                        heartbeatSound = new LoopingSoundInstance<>(client.player, player -> {
                            return partySound == null && Pony.of(player).getMagicalReserves().getExhaustion().getPercentFill() > 0.5F;
                        }, USounds.ENTITY_PLAYER_HEARTBEAT, 1, 1, client.world.random)
                );
            }

            float rate = exhaustion > 0.5F ? 2.5F : 7F;
            float radius = (1 + (float)Math.sin(client.player.age / rate)) / 2F;

            renderVignette(context, 0x880000, exhaustion * radius, 0.1F + radius * 0.3F, scaledWidth, scaledHeight);
        }
    }

    private void renderVignette(DrawContext context, int color, float alpha, float radius, int scaledWidth, int scaledHeight) {
        if (radius <= 0) {
            return;
        }

        color &= 0xFFFFFF;
        float alpha2 = MathHelper.clamp(radius - 1, 0, 1) * 255;
        float alpha1 = Math.max(alpha2, MathHelper.clamp(alpha * 2, 0, 1) * 205);
        GradientUtil.fillRadialGradient(context.getMatrices(), 0, 0, scaledWidth, scaledHeight,
                color | (int)alpha1 << 24,
                color | (int)alpha2 << 24,
                0, Math.min(1, radius));
    }

    public void setMessage(Text message) {
        this.message = message;
        this.messageTime = 60;
    }

    public void tick() {
        if (messageTime > 0) {
            messageTime--;
        }
    }

    void renderAbilityIcon(DrawContext context, AbilityDispatcher.Stat stat, int x, int y, int u, int v, int frameWidth, int frameHeight) {
        stat.getAbility(Unicopia.getConfig().hudPage.get()).ifPresent(ability -> {
            context.drawTexture(ability.getIcon(Pony.of(client.player)), x, y, 0, 0, frameWidth, frameHeight, u, v);
        });
    }
}
