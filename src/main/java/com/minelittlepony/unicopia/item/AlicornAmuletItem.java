package com.minelittlepony.unicopia.item;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.mob.SombraEntity;
import com.minelittlepony.unicopia.entity.player.*;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.server.world.Altar;
import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.util.VecHelper;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

public class AlicornAmuletItem extends AmuletItem implements ItemTracker.Trackable, ItemImpl.ClingyItem, ItemImpl.GroundTickCallback {
    private static final UUID EFFECT_UUID = UUID.fromString("c0a870f5-99ef-4716-a23e-f320ee834b26");
    private static final Map<EntityAttribute, Float> EFFECT_SCALES = Map.of(
            EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.2F,
            EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.05F,
            EntityAttributes.GENERIC_ATTACK_SPEED, 0.2F,
            EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 0.001F,
            EntityAttributes.GENERIC_ARMOR, 0.01F
    );
    private static final Float2ObjectFunction<EntityAttributeModifier> EFFECT_FACTORY = v -> {
        return new EntityAttributeModifier(EFFECT_UUID, "Alicorn Amulet Modifier", v, EntityAttributeModifier.Operation.ADDITION);
    };

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
        return ((ItemEntity)entity).getWorld().random.nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.FLAME;
    }

    @Override
    public boolean isClingy(ItemStack stack) {
        return true;
    }

    @Override
    public boolean damage(DamageSource source) {
        return source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY);
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

        if (!player.getWorld().isClient && !entity.isRemoved() && !player.isCreative()) {
            if (player.getPos().distanceTo(entity.getPos()) < 0.5) {
               if (entity.getWorld().random.nextInt(150) == 0) {
                   entity.setPickupDelay(0);
                   entity.onPlayerCollision(player);

                   if (player.getMainHandStack().getItem() == this) {
                       TypedActionResult<ItemStack> result = use(player.getWorld(), player, Hand.MAIN_HAND);

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

        float attachedTime = timeWorn / ItemTracker.HOURS;

        LocalDifficulty difficulty = wearer.asWorld().getLocalDifficulty(wearer.getOrigin());
        float amount = Math.min(entity.getMaxHealth() - 1, (attachedTime / 4) * (1 + difficulty.getClampedLocalDifficulty()));

        if (timeWorn > ItemTracker.DAYS) {
            amount++;
        }

        if (entity instanceof PlayerEntity player) {
            player.getHungerManager().setFoodLevel(1);
        }
        entity.damage(wearer.damageOf(UDamageTypes.ALICORN_AMULET), amount);
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1));
        if (timeWorn > ItemTracker.HOURS) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 3));
        }

        if (attachedTime > ItemTracker.HOURS / 2) {
            entity.takeKnockback(1, 1, 1);
            wearer.updateVelocity();
        }

        EFFECT_SCALES.keySet().forEach(attribute -> {
            wearer.updateAttributeModifier(EFFECT_UUID, attribute, 0F, EFFECT_FACTORY, false);
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

        final Living<?> living = Living.living(entity);

        if (living == null || !living.getArmour().contains(this)) {
            return;
        }

        if (entity instanceof PlayerEntity) {
            if (entity.isOnFire() && world.getBlockState(entity.getBlockPos().up()).isOf(Blocks.SOUL_FIRE)) {
                if (Altar.of(entity.getBlockPos().up()).isValid(world)) {
                    if (living.asEntity().getHealth() < 2) {
                        entity.setFireTicks(0);
                        world.removeBlock(entity.getBlockPos().up(), false);
                        stack.decrement(1);
                        world.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), 0, ExplosionSourceType.NONE);
                        world.playSound(null, entity.getBlockPos(), USounds.ENTITY_SOMBRA_LAUGH, SoundCategory.AMBIENT, 10, 1);

                        SombraEntity.startEncounter(world, entity.getBlockPos());
                    }
                    return;
                }
            }
        }

        final long attachedTicks = living.getArmour().getTicks(this);
        final long daysAttached = attachedTicks / ItemTracker.DAYS;
        final boolean fullSecond = attachedTicks % ItemTracker.SECONDS == 0;

        if (entity instanceof PlayerEntity player) {
            // healing effect
            if (daysAttached <= 4) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(0.5F);
                } else if (player.canConsume(false)) {
                    player.getHungerManager().add(1, 0);
                }
            }

            Pony pony = (Pony)living;

            // butterfingers effects
            if (daysAttached >= 2) {
                if (pony.asWorld().random.nextInt(200) == 0 && !pony.asEntity().hasStatusEffect(UEffects.BUTTER_FINGERS)) {
                    pony.asEntity().addStatusEffect(new StatusEffectInstance(UEffects.CORRUPT_INFLUENCE, 2100, 1));
                }

                pony.findAllEntitiesInRange(10, e -> e instanceof LivingEntity && !((LivingEntity)e).hasStatusEffect(UEffects.CORRUPT_INFLUENCE)).forEach(e -> {
                    ((LivingEntity)e).addStatusEffect(new StatusEffectInstance(UEffects.CORRUPT_INFLUENCE, 100, 1));
                });
            }

            // bind to the player after 3 days
            if (daysAttached >= 3 && !pony.asEntity().isCreative()) {
                stack = living.getArmour().getEquippedStack(TrinketsDelegate.NECKLACE);
                if (stack.getItem() == this && !EnchantmentHelper.hasBindingCurse(stack)) {
                    pony.playSound(USounds.ITEM_ALICORN_AMULET_HALLUCINATION, 3, 1);
                    stack = stack.copy();
                    stack.addEnchantment(Enchantments.BINDING_CURSE, 1);
                    pony.getArmour().equipStack(TrinketsDelegate.NECKLACE, stack);
                }
            }

            MagicReserves reserves = pony.getMagicalReserves();

            // constantly increase exertion
            if (reserves.getExertion().get() < reserves.getExertion().getMax()) {
                reserves.getExertion().add(0.02F);
            }

            // gradual corruption accumulation
            if (fullSecond && world.random.nextInt(12) == 0 && !pony.asEntity().isCreative()) {
                reserves.getEnergy().add(10);
                pony.getCorruption().add((int)MathHelper.clamp(attachedTicks / ItemTracker.HOURS, 1, pony.getCorruption().getMax()));
            }

            // ambient effects
            if (attachedTicks % ItemTracker.HOURS < 90 && world.random.nextInt(900) == 0) {
                pony.playSound(USounds.ITEM_ALICORN_AMULET_HALLUCINATION, 3, 1);
            } else if (attachedTicks < 2 || (attachedTicks % (10 * ItemTracker.SECONDS) < 9 && world.random.nextInt(90) == 0)) {
                if (attachedTicks % 5 == 0) {
                    InteractionManager.INSTANCE.playLoopingSound(player, InteractionManager.SOUND_HEART_BEAT, 0);
                }

                reserves.getExertion().addPercent(10);
                reserves.getEnergy().add(10);
                living.asEntity().removeStatusEffect(StatusEffects.WEAKNESS);
                living.asEntity().removeStatusEffect(StatusEffects.NAUSEA);
            }

            // damage effect after 4 days
            if (daysAttached >= 4) {
                if (attachedTicks % 100 == 0) {
                    player.getHungerManager().addExhaustion(90F);
                    float healthDrop = MathHelper.clamp(player.getMaxHealth() - player.getHealth(), 2, 5);
                    player.damage(pony.damageOf(UDamageTypes.ALICORN_AMULET), healthDrop);
                }

                return;
            }
        }

        // every 1 second, update modifiers
        if (fullSecond) {
            EFFECT_SCALES.entrySet().forEach(attribute -> {
                float seconds = (float)attachedTicks / ItemTracker.SECONDS;
                living.updateAttributeModifier(EFFECT_UUID, attribute.getKey(), attribute.getValue() * seconds, EFFECT_FACTORY, false);
            });
        }
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = (ItemEntity)item;

        if (entity.getWorld().random.nextInt(500) == 0) {
            entity.getWorld().playSound(null, entity.getBlockPos(), USounds.ITEM_ALICORN_AMULET_AMBIENT, SoundCategory.HOSTILE, 0.5F, 1);
        }

        return ActionResult.PASS;
    }

    @Override
    public void inFrameTick(ItemFrameEntity entity) {
        Random rng = entity.getWorld().random;

        if (rng.nextInt(1500) == 0) {
            entity.getWorld().playSound(null, entity.getBlockPos(), USounds.ITEM_ALICORN_AMULET_AMBIENT, SoundCategory.HOSTILE, 0.5F, 1);
            for (int i = 0; i < 5; i++) {
                entity.getWorld().addParticle(rng.nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.FLAME,
                        rng.nextTriangular(entity.getX(), 0.5),
                        rng.nextTriangular(entity.getY(), 0.5),
                        rng.nextTriangular(entity.getZ(), 0.5),
                        0, 0, 0
                );
            }
        }

        if ((entity.age / 1000) % 10 == 0 && entity.age % 50 == 0) {
            for (Entity target : VecHelper.findInRange(entity, entity.getWorld(), entity.getPos(), 10, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
                for (ItemStack equipment : target.getItemsEquipped()) {
                    if (equipment.getItem() == UItems.GROGARS_BELL) {
                        ChargeableItem chargeable = (ChargeableItem)UItems.GROGARS_BELL;
                        if (chargeable.hasCharge(equipment)) {
                            ChargeableItem.consumeEnergy(equipment, 3);
                            ParticleUtils.spawnParticle(entity.getWorld(),
                                    new FollowingParticleEffect(UParticles.HEALTH_DRAIN, entity, 0.4F)
                                    .withChild(ParticleTypes.COMPOSTER), target.getEyePos(), Vec3d.ZERO);
                        }
                    }
                }
            }
        }
    }
}
