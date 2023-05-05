package com.minelittlepony.unicopia.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BellItem extends Item implements ChargeableItem {
    public BellItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext tooltipContext) {
        list.add(Text.translatable(getTranslationKey() + ".charges", (int)Math.floor(ChargeableItem.getEnergy(stack)), getMaxCharge()));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 3000;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        player.setCurrentHand(hand);
        Pony pony = Pony.of(player);
        pony.getCorruption().add(1);
        pony.playSound(SoundEvents.BLOCK_BELL_USE, 0.4F, 0.2F);
        Living<?> targetLiving = target instanceof MobEntity || target instanceof PlayerEntity ? Living.getOrEmpty(target)
                .filter(living -> !(living instanceof Creature c && c.isDiscorded()))
                .orElse(null) : null;
        pony.setTarget(targetLiving);
        return targetLiving == null ? ActionResult.FAIL : ActionResult.CONSUME_PARTIAL;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        ItemStack offhandStack = AmuletItem.getForEntity(player);

        if (!(offhandStack.getItem() instanceof ChargeableItem)) {
            offhandStack = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        }

        Pony pony = Pony.of(player);

        if (hasCharge(stack)) {
            pony.playSound(SoundEvents.BLOCK_BELL_RESONATE, 0.6F, 1);
            pony.getCorruption().add(1);
            if (offhandStack.getItem() instanceof ChargeableItem chargeable) {
                float maxChargeBy = chargeable.getMaxCharge() - ChargeableItem.getEnergy(offhandStack);
                float energyTransferred = Math.min(ChargeableItem.getEnergy(stack), maxChargeBy);
                chargeable.recharge(offhandStack, energyTransferred);
                ChargeableItem.consumeEnergy(stack, energyTransferred);
            } else {
                pony.getMagicalReserves().getMana().add(ChargeableItem.getEnergy(stack));
                ChargeableItem.setEnergy(stack, 0);
            }

            pony.spawnParticles(pony.getPhysics().getHeadPosition().toCenterPos(), new Sphere(false, 0.5F), 7, p -> {
                pony.addParticle(new MagicParticleEffect(0xAAFFFF), p, Vec3d.ZERO);
            });

            return TypedActionResult.consume(stack);
        }

        pony.playSound(SoundEvents.BLOCK_BELL_USE, 0.01F, 0.9F);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity userEntity, ItemStack stack, int remainingUseTicks) {
        if (userEntity.age % 5 != 0) {
            return;
        }

        Living.getOrEmpty(userEntity).ifPresent(user -> {
            user.getTarget().ifPresent(living -> {
                float maxUseTime = getMaxUseTime(stack);
                float progress = (maxUseTime - remainingUseTicks) / maxUseTime;

                if (tickDraining(user, living, stack, progress)) {
                    onStoppedDraining(user, living, true);
                }
            });
        });
    }

    private void onStoppedDraining(Living<?> user, Living<?> target, boolean completed) {
        user.setTarget(null);
        user.playSound(SoundEvents.BLOCK_BELL_USE, 0.2F, 0.3F);
        if (target instanceof Creature creature && (completed || target.asEntity().getHealth() < (target.asEntity().getMaxHealth() * 0.5F) + 1)) {
            creature.setDiscorded(true);
        }
    }

    private boolean tickDraining(Living<?> user, Living<?> living, ItemStack stack, float progress) {
        if (living.getOrigin().getSquaredDistance(user.getOrigin()) > 25 || living.asEntity().isRemoved()) {
            return true;
        }

        float amountDrawn;
        ParticleEffect particleType = ParticleTypes.COMPOSTER;

        if (living instanceof Pony pony) {
            amountDrawn = pony.getMagicalReserves().getMana().get() * 0.2F;
            pony.getMagicalReserves().getMana().multiply(0.8F);
            if (pony.getActualSpecies() == Race.CHANGELING) {
                particleType = ParticleTypes.HEART;
            }
        } else {
            float damageAmount = Math.min(Math.max(1, living.asEntity().getMaxHealth() / 25F), living.asEntity().getHealth() - 1);
            living.asEntity().damage(MagicalDamageSource.EXHAUSTION, damageAmount);
            living.asEntity().setAttacker(user.asEntity());
            if (living.asEntity() instanceof MobEntity mob) {
                mob.setTarget(null);
            }
            amountDrawn = Math.max(living.asEntity().getWidth(), living.asEntity().getHeight()) + living.asEntity().getHealth();

            if (living.asEntity() instanceof CreeperEntity creeper) {
                creeper.setFuseSpeed(-1);

                if (creeper.getDataTracker().get(CreeperEntity.CHARGED)) {
                    creeper.getDataTracker().set(CreeperEntity.CHARGED, false);
                    amountDrawn += 60;
                }
                creeper.getDataTracker().set(CreeperEntity.IGNITED, false);
            }

            if (living.asEntity() instanceof IllagerEntity) {
                particleType = ParticleTypes.ANGRY_VILLAGER;
            }

            if (living.asEntity().getHealth() <= 1) {
                return true;
            }
        }
        ChargeableItem.consumeEnergy(stack, -amountDrawn);

        user.playSound(SoundEvents.ENTITY_GUARDIAN_ATTACK, 0.2F, progress);

        for (int i = 0; i < 4; i++) {
            living.addParticle(
                    new FollowingParticleEffect(UParticles.HEALTH_DRAIN, user.asEntity(), 0.4F)
                        .withChild(particleType),
                    living.getOriginVector().add(0, living.getPhysics().getGravitySignum() * living.asEntity().getHeight() / 2, 0)
                        .add(VecHelper.supply(() -> user.asWorld().random.nextTriangular(0, 0.2))),
                    Vec3d.ZERO
            );
        }

        return false;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        Living.getOrEmpty(user).ifPresent(living -> {
            living.getTarget().ifPresent(target -> {
                onStoppedDraining(living, target, true);
            });
        });
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        Living.getOrEmpty(user).ifPresent(living -> {
            living.getTarget().ifPresent(target -> {
                onStoppedDraining(living, target, false);
            });
        });
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.hasEnchantments() || ChargeableItem.getEnergy(stack) > 0;
    }

    @Override
    public int getMaxCharge() {
        return 1000;
    }
}
