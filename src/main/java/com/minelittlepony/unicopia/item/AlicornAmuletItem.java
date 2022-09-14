package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.PlayerCharmTracker;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class AlicornAmuletItem extends AmuletItem implements PlayerCharmTracker.Charm, ItemImpl.ClingyItem, ItemImpl.GroundTickCallback {

    public AlicornAmuletItem(FabricItemSettings settings) {
        super(settings, 0, new AmuletItem.ModifiersBuilder()
                .add(EntityAttributes.GENERIC_ARMOR, 9000)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 9000)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 9000)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 100)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 20)
            .build());
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext tooltipContext) {
        Pony iplayer = Pony.of(MinecraftClient.getInstance().player);

        if (iplayer != null) {
            int attachedTime = iplayer.getCharms().getArmour().getTicks(this);
            if (attachedTime > 0) {
                tooltip.add(new TranslatableText(getTranslationKey() + ".lore", StringHelper.formatTicks(attachedTime)));
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

        if (!player.world.isClient && !entity.isRemoved()) {
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
    public void onRemoved(Pony pony, int timeWorn) {
        float attachedTime = timeWorn / 100F;

        LocalDifficulty difficulty = pony.getReferenceWorld().getLocalDifficulty(pony.getOrigin());
        float amount = attachedTime * (1 + difficulty.getClampedLocalDifficulty());

        amount = Math.min(amount, pony.getMaster().getMaxHealth());

        pony.getMaster().getHungerManager().setFoodLevel(1);
        pony.getMaster().damage(MagicalDamageSource.ALICORN_AMULET, amount);
        pony.getMaster().addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1));

        if (attachedTime > 120) {
            pony.getMaster().takeKnockback(1, 1, 1);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        if (world.isClient) {
            return;
        }

        PlayerEntity player = (PlayerEntity)entity;

        if (selected && !isApplicable(player) && world.random.nextInt(320) == 0) {
            use(world, player, Hand.MAIN_HAND);
            return;
        }

        Pony pony = Pony.of(player);

        if (!pony.getCharms().getArmour().contains(this)) {
            return;
        }

        float attachedTime = pony.getCharms().getArmour().getTicks(this);

        MagicReserves reserves = pony.getMagicalReserves();

        if (player.getHealth() < player.getMaxHealth()) {
            player.heal(0.5F);
        } else if (player.canConsume(false)) {
            player.getHungerManager().add(1, 0);
        } else {
            player.removeStatusEffect(StatusEffects.NAUSEA);
        }

        if (reserves.getExertion().get() < reserves.getExertion().getMax()) {
            reserves.getExertion().add(2);
        }

        if (reserves.getEnergy().get() < 0.005F + (attachedTime / 1000000)) {
            reserves.getEnergy().add(2);
        }

        if (attachedTime == 1) {
            world.playSound(null, player.getBlockPos(), USounds.ITEM_ALICORN_AMULET_CURSE, SoundCategory.PLAYERS, 3, 1);
        }

        // attempt to play 3 tricks every tick
        Trick.ALL.stream().filter(trick -> trick.play(attachedTime, player)).limit(3).toList();

        if (stack.getDamage() >= getMaxDamage() - 1) {
            stack.damage(10, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.CHEST));

            player.damage(MagicalDamageSource.ALICORN_AMULET, player.getMaxHealth() - 0.01F);
            player.getHungerManager().setFoodLevel(1);

            Vec3d pos = player.getPos();

            player.world.createExplosion(player, pos.x, pos.y, pos.z, 10, DestructionType.NONE);

            AwaitTickQueue.scheduleTask(player.world, w -> {
                w.createExplosion(player, pos.x, pos.y, pos.z, 6, DestructionType.BREAK);
            }, 50);
        }

        pony.findAllEntitiesInRange(10, e -> e instanceof MobEntity && !((MobEntity)e).hasStatusEffect(UEffects.CORRUPT_INFLUENCE)).forEach(e -> {
            ((MobEntity)e).addStatusEffect(new StatusEffectInstance(UEffects.CORRUPT_INFLUENCE, 1300, 1));
        });
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        ItemEntity entity = (ItemEntity)item;

        if (entity.world.random.nextInt(500) == 0) {
            entity.world.playSound(null, entity.getBlockPos(), USounds.ITEM_ALICORN_AMULET_AMBIENT, SoundCategory.HOSTILE, 0.5F, 1);
        }

        return ActionResult.PASS;
    }

    public static class Trick {
        private static final List<Trick> ALL = new ArrayList<>();

        public static final Trick SPOOK = new Trick(0, 1050, player -> player.world.playSound(null, player.getBlockPos(), USounds.ITEM_ALICORN_AMULET_HALLUCINATION, SoundCategory.PLAYERS, 3, 1));
        public static final Trick WITHER = new Trick(20000, 100, player -> {
            StatusEffectInstance effect = new StatusEffectInstance(player.world.random.nextInt(32000) == 0 ? StatusEffects.WITHER : StatusEffects.HUNGER, 300, 3);
            effect.setPermanent(true);
            player.addStatusEffect(effect);
        });
        public static final Trick POKE = new Trick(13000, 300, player -> player.damage(MagicalDamageSource.ALICORN_AMULET, 1F));
        public static final Trick SPIN = new Trick(6000, 300, player -> player.setYaw(player.getYaw() + 180));
        public static final Trick BUTTER_FINGERS = new Trick(1000, 300, player -> player.getInventory().dropSelectedItem(false));
        public static final Trick MOVE = new Trick(3000, 300, player -> {
            float amount = player.world.random.nextFloat() - 0.5F;
            boolean sideways = player.world.random.nextBoolean();
            player.addVelocity(sideways ? 0 : amount, 0, sideways ? amount : 0);
        });
        public static final Trick SWING = new Trick(2000, 100, player -> player.swingHand(Hand.MAIN_HAND));
        public static final Trick BAD_JOO_JOO = new Trick(1000, 10, player -> {
            if (!player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 300, 3));
            }
        });

        private final int minTime;
        private final int chance;
        private final Consumer<PlayerEntity> action;

        public Trick(int minTime, int chance, Consumer<PlayerEntity> action) {
            this.minTime = minTime;
            this.chance = chance;
            this.action = action;
            ALL.add(this);
        }

        public boolean play(float ticks, PlayerEntity player) {
            if (ticks > minTime && (chance <= 0 || player.world.random.nextInt(chance) == 0)) {
                action.accept(player);
                return true;
            }
            return false;
        }
    }
}
