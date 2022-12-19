package com.minelittlepony.unicopia.item;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.player.*;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.*;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class AlicornAmuletItem extends AmuletItem implements ItemTracker.Trackable, ItemImpl.ClingyItem, ItemImpl.GroundTickCallback {
    private static final UUID EFFECT_UUID = UUID.fromString("c0a870f5-99ef-4716-a23e-f320ee834b26");
    private static final Map<EntityAttribute, Float> EFFECT_SCALES = Map.of(
            EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.2F,
            EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.05F,
            EntityAttributes.GENERIC_ATTACK_SPEED, 0.2F,
            EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 0.001F,
            EntityAttributes.GENERIC_ARMOR, 0.01F
    );

    public AlicornAmuletItem(FabricItemSettings settings) {
        super(settings, 0, ImmutableMultimap.of());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext tooltipContext) {
        Pony iplayer = Pony.of(MinecraftClient.getInstance().player);

        if (iplayer != null) {
            long ticks = iplayer.getArmour().getTicks(this);
            if (ticks > 0) {
                tooltip.add(Text.literal(ItemTracker.formatTicks(ticks).formatted(Formatting.GRAY)));
            }
        }
    }

    @Override
    public ParticleEffect getParticleEffect(IItemEntity entity) {
        return ((ItemEntity)entity).world.random.nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.FLAME;
    }

    @Override
    public boolean isClingy(ItemStack stack) {
        return true;
    }

    @Override
    public boolean damage(DamageSource source) {
        return source.isOutOfWorld();
    }

    @Override
    public float getFollowDistance(IItemEntity entity) {
        return Math.max(20, ItemImpl.ClingyItem.super.getFollowDistance(entity));
    }

    @Override
    public float getFollowSpeed(IItemEntity entity) {
        return Math.max(0.12F, ItemImpl.ClingyItem.super.getFollowSpeed(entity));
    }

    @Override
    public void interactWithPlayer(IItemEntity item, PlayerEntity player) {
        ItemEntity entity = (ItemEntity)item;

        if (!player.world.isClient && !entity.isRemoved() && !player.isCreative()) {
            if (player.getPos().distanceTo(entity.getPos()) < 0.5) {
               if (entity.world.random.nextInt(150) == 0) {
                   entity.setPickupDelay(0);
                   entity.onPlayerCollision(player);

                   if (player.getMainHandStack().getItem() == this) {
                       TypedActionResult<ItemStack> result = use(player.world, player, Hand.MAIN_HAND);

                       if (result.getResult() == ActionResult.SUCCESS) {
                           entity.setPickupDelay(1000);
                           entity.setRemoved(RemovalReason.DISCARDED);
                       }
                   }
               }
            }
        }
    }

    @Override
    public void onEquipped(Living<?> wearer) {
        wearer.playSound(USounds.ITEM_ALICORN_AMULET_CURSE, 3, 1);
    }

    @Override
    public void onUnequipped(Living<?> wearer, long timeWorn) {

        LivingEntity entity = wearer.asEntity();

        if (entity instanceof PlayerEntity player && player.isCreative()) {
            return;
        }

        float attachedTime = timeWorn / 100F;

        LocalDifficulty difficulty = wearer.asWorld().getLocalDifficulty(wearer.getOrigin());
        float amount = attachedTime * (1 + difficulty.getClampedLocalDifficulty());

        amount = Math.min(amount, entity.getMaxHealth());

        if (entity instanceof PlayerEntity player) {
            player.getHungerManager().setFoodLevel(1);
        }
        entity.damage(MagicalDamageSource.ALICORN_AMULET, amount);
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1));
        if (timeWorn > ItemTracker.HOURS) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 3));
        }

        if (attachedTime > 120) {
            entity.takeKnockback(1, 1, 1);
            wearer.updateVelocity();
        }

        EFFECT_SCALES.keySet().forEach(attribute -> {
            EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
            @Nullable
            EntityAttributeModifier modifier = instance.getModifier(EFFECT_UUID);
            if (modifier != null) {
                instance.removeModifier(modifier);
            }
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (world.isClient) {
            return;
        }

        // if we're in the main hand, try to equip ourselves
        if (entity instanceof PlayerEntity player && selected && !isApplicable(player) && world.random.nextInt(320) == 0) {
            use(world, player, Hand.MAIN_HAND);
            return;
        }

        Living<?> living = Living.living(entity);

        if (living == null || !living.getArmour().contains(this)) {
            return;
        }

        long attachedTicks = living.getArmour().getTicks(this);
        boolean poweringUp = attachedTicks < (ItemTracker.DAYS * 4);
        boolean fullSecond = attachedTicks % ItemTracker.SECONDS == 0;

        if (entity instanceof PlayerEntity player) {
            // healing effect
            if (poweringUp) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(0.5F);
                } else if (player.canConsume(false)) {
                    player.getHungerManager().add(1, 0);
                }
            }

            Pony pony = (Pony)living;

            MagicReserves reserves = pony.getMagicalReserves();

            // constantly increase exertion
            if (reserves.getExertion().get() < reserves.getExertion().getMax()) {
                reserves.getExertion().add(2);
            }

            if (fullSecond && world.random.nextInt(12) == 0 && !pony.asEntity().isCreative()) {
                reserves.getEnergy().add(reserves.getEnergy().getMax() / 10F);
                pony.getCorruption().add((int)MathHelper.clamp(attachedTicks / ItemTracker.HOURS, 1, pony.getCorruption().getMax()));
            }

            if (attachedTicks % ItemTracker.HOURS < 90 && world.random.nextInt(900) == 0) {
                player.world.playSound(null, player.getBlockPos(), USounds.ITEM_ALICORN_AMULET_HALLUCINATION, SoundCategory.AMBIENT, 3, 1);
            } else if (attachedTicks < 2 || (attachedTicks % (10 * ItemTracker.SECONDS) < 9 && world.random.nextInt(90) == 0)) {
                if (attachedTicks % 5 == 0) {
                    InteractionManager.INSTANCE.playLoopingSound(player, InteractionManager.SOUND_HEART_BEAT, 0);
                }

                reserves.getExertion().add(reserves.getExertion().getMax());
                reserves.getEnergy().add(reserves.getEnergy().getMax() / 2F);
                living.asEntity().removeStatusEffect(StatusEffects.WEAKNESS);
                living.asEntity().removeStatusEffect(StatusEffects.NAUSEA);
            }

            if (!poweringUp) {
                if (attachedTicks % 100 == 0) {
                    player.getHungerManager().addExhaustion(90F);
                    float healthDrop = MathHelper.clamp(player.getMaxHealth() - player.getHealth(), 2, 5);
                    player.damage(MagicalDamageSource.ALICORN_AMULET, healthDrop);
                }

                return;
            }
        }

        // every 1 second, update modifiers
        if (fullSecond) {
            EFFECT_SCALES.entrySet().forEach(attribute -> {
                float seconds = (float)attachedTicks / ItemTracker.SECONDS;
                EntityAttributeInstance instance = living.asEntity().getAttributeInstance(attribute.getKey());
                @Nullable
                EntityAttributeModifier modifier = instance.getModifier(EFFECT_UUID);
                float desiredValue = attribute.getValue() * seconds;
                if (!MathHelper.approximatelyEquals(desiredValue, modifier == null ? 0 : modifier.getValue())) {
                    if (modifier != null) {
                        instance.removeModifier(modifier);
                    }
                    instance.addTemporaryModifier(new EntityAttributeModifier(EFFECT_UUID, "Alicorn Amulet Modifier", attribute.getValue() * seconds, EntityAttributeModifier.Operation.ADDITION));
                }
            });
        }
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = (ItemEntity)item;

        if (entity.world.random.nextInt(500) == 0) {
            entity.world.playSound(null, entity.getBlockPos(), USounds.ITEM_ALICORN_AMULET_AMBIENT, SoundCategory.HOSTILE, 0.5F, 1);
        }

        return ActionResult.PASS;
    }
}
