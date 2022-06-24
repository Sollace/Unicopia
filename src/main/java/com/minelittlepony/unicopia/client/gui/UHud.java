package com.minelittlepony.unicopia.client.gui;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.KeyBindingsHandler;
import com.minelittlepony.unicopia.client.sound.LoopingSoundInstance;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.effect.SunBlindnessStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;

public class UHud extends DrawableHelper {

    public static final UHud INSTANCE = new UHud();

    public static final Identifier HUD_TEXTURE = new Identifier("unicopia", "textures/gui/hud.png");

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

    public void render(InGameHud hud, MatrixStack matrices, float tickDelta) {

        if (client.player == null) {
            return;
        }

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        Pony pony = Pony.of(client.player);

        renderViewEffects(pony, matrices, scaledWidth, scaledHeight, tickDelta);

        if (client.currentScreen instanceof HidesHud || client.player.isSpectator() || client.options.hudHidden) {
            return;
        }

        font = client.textRenderer;
        xDirection = client.player.getMainArm() == Arm.LEFT ? -1 : 1;

        matrices.push();

        int hudX = ((scaledWidth - 50) / 2) + (104 * xDirection);
        int hudY = scaledHeight - 50;
        int hudZ = 0;


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
            renderMessage(matrices, tickDelta);
        }

        RenderSystem.setShaderColor(1, 1, 1,1);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, HUD_TEXTURE);

        boolean swap = client.options.sneakKey.isPressed();

        slots.forEach(slot -> slot.renderBackground(matrices, abilities, swap, tickDelta));
        slots.forEach(slot -> slot.renderLabel(matrices, abilities, tickDelta));

        matrices.pop();

        if (pony.getSpecies().canCast()) {
            renderSpell(pony.getCharms().getEquippedSpell(Hand.MAIN_HAND), hudX + 15 - xDirection * 13, hudY + 3);
            renderSpell(pony.getCharms().getEquippedSpell(Hand.OFF_HAND), hudX + 15 - xDirection * 2, hudY - 3);
        }

        RenderSystem.disableBlend();

        if (pony.getSpecies() == Race.CHANGELING && !client.player.isSneaking()) {
            pony.getSpellSlot().get(SpellType.CHANGELING_DISGUISE, false).map(AbstractDisguiseSpell::getDisguise)
                .map(EntityAppearance::getAppearance)
                .ifPresent(appearance -> {

                    float baseHeight = 20;

                    EntityDimensions dims = appearance.getDimensions(appearance.getPose());

                    float entityHeight = Math.max(dims.height, dims.width);
                    int scale = (int)(baseHeight / entityHeight);

                    int x = scaledWidth / 2 + xDirection * 67;
                    int y = (int)(scaledHeight - 18 - dims.height/2F);

                    MatrixStack view = RenderSystem.getModelViewStack();

                    view.push();
                    view.translate(x, y, 0);
                    view.multiply(new Quaternion(-9, xDirection * 45, 0, true));
                    InventoryScreen.drawEntity(0, 0, scale, 0, -20, client.player);
                    view.pop();
                    RenderSystem.applyModelViewMatrix();
                });
        }
    }

    public void renderSpell(CustomisedSpellType<?> spell, double x, double y) {
        if (!spell.isEmpty()) {
            Pony pony = Pony.of(client.player);

            if (spell.isOn(pony)) {
                MatrixStack modelStack = new MatrixStack();

                modelStack.push();
                modelStack.translate(x + 5.5, y + 5.5, 0);

                int color = spell.type().getColor() | 0x000000FF;
                double radius = 2 + Math.sin(client.player.age / 9D) / 4;

                DrawableUtil.drawArc(modelStack, radius, radius + 3, 0, DrawableUtil.TAU, color & 0xFFFFFF2F, false);
                DrawableUtil.drawArc(modelStack, radius + 3, radius + 4, 0, DrawableUtil.TAU, color & 0xFFFFFFAF, false);

                modelStack.pop();
            }

            DrawableUtil.renderItemIcon(spell.getDefaultStack(), x, y, EQUIPPED_GEMSTONE_SCALE);
        }
    }

    private void renderMessage(MatrixStack matrices, float tickDelta) {
        float time = messageTime - tickDelta;
        int progress = Math.min(255, (int)(time * 255F / 20F));

        if (progress > 8) {
            int color = 0xFFFFFF;
            int alpha = progress << 24 & -16777216;

            color |= alpha;

            drawCenteredText(matrices, client.textRenderer, message, 25, -15, color);
        }
    }

    protected void renderViewEffects(Pony pony, MatrixStack matrices, int scaledWidth, int scaledHeight, float tickDelta) {

        boolean hasEffect = client.player.hasStatusEffect(UEffects.SUN_BLINDNESS);

        if (hasEffect || (pony.getSpecies() == Race.BAT && SunBlindnessStatusEffect.hasSunExposure(client.player))) {
            float i = hasEffect ? (client.player.getStatusEffect(UEffects.SUN_BLINDNESS).getDuration() - tickDelta) / SunBlindnessStatusEffect.MAX_DURATION : 0;

            float pulse = (1 + (float)Math.sin(client.player.age / 108F)) * 0.25F;

            float strength = MathHelper.clamp(pulse + i, 0.3F, 1F);

            int alpha1 = (int)(strength * 205) << 24 & -16777216;
            int alpha2 = (int)(alpha1 * 0.6F);

            fillGradient(matrices, 0, 0, scaledWidth, scaledHeight / 2, 0xFFFFFF | alpha1, 0xFFFFFF | alpha2);
            fillGradient(matrices, 0, scaledHeight / 2, scaledWidth, scaledHeight, 0xFFFFFF | alpha2, 0xFFFFFF | alpha1);

            if (hasEffect) {
                matrices.push();
                matrices.translate(scaledWidth, 0, 0);
                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));

                fillGradient(matrices, 0, 0, scaledHeight, scaledWidth / 2, 0xFFFFFF | 0, 0xFFFFFF | alpha2);
                fillGradient(matrices, 0, scaledWidth / 2, scaledHeight, scaledWidth, 0xFFFFFF | alpha2, 0xFFFFFF | 0);

                matrices.pop();
            }
        }

        float exhaustion = pony.getMagicalReserves().getExhaustion().getPercentFill();

        if (exhaustion > 0) {
            if (exhaustion > 0.5F && (heartbeatSound == null || heartbeatSound.isDone())) {
                client.getSoundManager().play(
                        heartbeatSound = new LoopingSoundInstance<>(client.player, player -> {
                            return Pony.of(player).getMagicalReserves().getExhaustion().getPercentFill() > 0.5F;
                        }, USounds.ENTITY_PLAYER_HEARTBEAT, 1, 1, client.world.random)
                );
            }

            int color = 0x880000;

            float rate = exhaustion > 0.5F ? 2.5F : 7F;

            float radius = (1 + (float)Math.sin(client.player.age / rate)) / 2F;
            radius = 0.1F + radius * 0.1F;

            int alpha1 = (int)(MathHelper.clamp(exhaustion * radius * 2, 0, 1) * 205) << 24 & -16777216;
            int alpha2 = 0;

            int halfWidth = (int)(scaledWidth * radius);
            int halfHeight = (int)(scaledHeight * radius);

            fillGradient(matrices, 0, 0, scaledWidth, halfHeight, color | alpha1, color | alpha2);
            fillGradient(matrices, 0, scaledHeight - halfHeight, scaledWidth, scaledHeight, color | alpha2, color | alpha1);

            matrices.push();
            matrices.translate(scaledWidth, 0, 0);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));

            fillGradient(matrices, 0, 0, scaledHeight, halfWidth, color | alpha1, color | alpha2);
            fillGradient(matrices, 0, scaledWidth - halfWidth, scaledHeight, scaledWidth, color | alpha2, color | alpha1);

            matrices.pop();
        }
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

    void renderAbilityIcon(MatrixStack matrices, AbilityDispatcher.Stat stat, int x, int y, int u, int v, int frameWidth, int frameHeight) {
        stat.getAbility(KeyBindingsHandler.INSTANCE.page).ifPresent(ability -> {
            RenderSystem.setShaderTexture(0, ability.getIcon(Pony.of(client.player), client.options.sneakKey.isPressed()));
            drawTexture(matrices, x, y, 0, 0, frameWidth, frameHeight, u, v);
            RenderSystem.setShaderTexture(0, HUD_TEXTURE);
        });
    }
}
