package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.ducks.IItemEntity;
import com.minelittlepony.unicopia.entity.ItemEntityCapabilities;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.IDependable;
import com.minelittlepony.unicopia.util.AwaitTickQueue;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.util.ChatUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class AlicornAmuletItem extends ArmorItem implements IDependable, ItemEntityCapabilities.TickableItem {

    private static final UUID[] MODIFIERS = new UUID[] {
            UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
            UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
            UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
            UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
    };

    public AlicornAmuletItem() {
        super(new Material(), EquipmentSlot.CHEST, new Settings().maxCount(1));
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }


    @Override
    public ActionResult onGroundTick(IItemEntity item) {

        ItemEntity entity = item.getRaceContainer().getOwner();

        World world = entity.world;

        double x = entity.x + world.random.nextFloat() - 0.5;
        double z = entity.z + world.random.nextFloat() - 0.5;
        double y = entity.y + world.random.nextFloat();

        ParticleEffect particle = world.random.nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.FLAME;

        world.addParticle(particle, x, y, z, 0, 0, 0);

        if (world.random.nextInt(500) == 0) {
            world.playSound(null, entity.getBlockPos(), SoundEvents.AMBIENT_CAVE, SoundCategory.HOSTILE, 0.5F, 1);
        }

        Vec3d position = entity.getPos();
        VecHelper.findAllEntitiesInRange(entity, world, entity.getBlockPos(), 10)
            .filter(e -> e instanceof PlayerEntity)
            .sorted((a, b) -> (int)(a.getPos().distanceTo(position) - b.getPos().distanceTo(position)))
            .findFirst()
            .ifPresent(player -> interactWithPlayer(entity, (PlayerEntity)player));

        return ActionResult.PASS;
    }

    protected void interactWithPlayer(ItemEntity entity, PlayerEntity player) {

        entity.move(MovementType.SELF, player.getPos().subtract(entity.getPos()).multiply(0.02));

        if (!player.world.isClient && !entity.removed) {
            if (player.getPos().distanceTo(entity.getPos()) < 3) {
               if (entity.world.random.nextInt(150) == 0) {

                   TypedActionResult<ItemStack> result = use(player.world, player, Hand.MAIN_HAND);

                   if (result.getResult() == ActionResult.SUCCESS) {
                       entity.setPickupDelay(1000);
                       entity.remove();
                   }
               }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

        IPlayer iplayer = SpeciesList.instance().getPlayer(MinecraftClient.getInstance().player);

        if (iplayer != null) {
            int attachedTime = iplayer.getInventory().getTicksAttached(this);
            if (attachedTime > 0) {
                tooltip.add(new TranslatableText(getTranslationKey() + ".tagline", ChatUtil.ticksToString(attachedTime)));
            }
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }

        CompoundTag compound = stack.getTag();

        int hideFlags = 0;

        if (!compound.containsKey("HideFlags") || ((hideFlags = compound.getInt("HideFlags")) & 2) == 0) {
            compound.putInt("HideFlags", hideFlags | 2);
        }

        return super.getName(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity)entity;

        if (player.getHealth() < player.getHealthMaximum()) {
            player.heal(0.5F);
        } else if (player.canConsume(false)) {
            player.getHungerManager().add(1, 0);
        }

        IPlayer iplayer = SpeciesList.instance().getPlayer(player);

        float attachedTime = iplayer.getInventory().getTicksAttached(this);

        if (iplayer.getExertion() < 1) {
            iplayer.addExertion(2);
        }

        if (iplayer.getEnergy() < 0.005F + (attachedTime / 1000000)) {
            iplayer.addEnergy(2);
        }

        if (attachedTime == 1) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 3, 1);
        }

        if (attachedTime > 0 && attachedTime % 100 == 0) {
            world.playSound(null, player.getBlockPos(), SoundEvents.MUSIC_NETHER, SoundCategory.PLAYERS, 3, 1);
        }

        if (attachedTime > 1000) {
            if (world.random.nextInt(700) == 0) {
                player.dropSelectedItem(false);
            }
        }

        if (attachedTime > 3000) {
            if (world.random.nextInt(300) == 0) {
                player.addVelocity(world.random.nextFloat() - 0.5F, 0, 0);
            }

            if (world.random.nextInt(300) == 0) {
                player.addVelocity(0, 0, world.random.nextFloat() - 0.5F);
            }
        }

        if (attachedTime > 6000) {
            if (world.random.nextInt(300) == 0) {
                player.yaw += 180;
            }
        }

        if (attachedTime > 13000) {
            if (world.random.nextInt(300) == 0) {
                player.damage(MagicalDamageSource.ALICORN_AMULET, 1F);
            }
        }

        if (stack.getDamage() >= getMaxDamage() - 1) {
            stack.damage(10, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.CHEST));

            player.damage(MagicalDamageSource.ALICORN_AMULET, player.getHealthMaximum() - 0.01F);
            player.getHungerManager().setFoodLevel(1);

            Vec3d pos = player.getPos();

            player.world.createExplosion(player, pos.x, pos.y, pos.z, 10, DestructionType.NONE);

            AwaitTickQueue.scheduleTask(w -> {
                w.createExplosion(player, pos.x, pos.y, pos.z, 6, DestructionType.BREAK);
            }, 50);
        }

        iplayer.getInventory().enforceDependency(this);
    }

    @Override
    public void onRemoved(IPlayer player, float needfulness) {

        float attachedTime = player.getInventory().getTicksAttached(this) / 100F;

        LocalDifficulty difficulty = player.getWorld().getLocalDifficulty(player.getOrigin());
        float amount = (attachedTime * (1 + needfulness)) * (1 + difficulty.getClampedLocalDifficulty());

        amount = Math.min(amount, player.getOwner().getHealthMaximum());

        player.getOwner().damage(MagicalDamageSource.ALICORN_AMULET, amount);

        if (attachedTime > 120) {
            player.getOwner().takeKnockback(player.getOwner(), 1, 1, 1);
        }
    }

    @Override
    public Multimap<String, EntityAttributeModifier> getModifiers(EquipmentSlot equipmentSlot) {
        Multimap<String, EntityAttributeModifier> multimap = super.getModifiers(equipmentSlot);

        if (equipmentSlot == slot) {
            UUID modifierId = MODIFIERS[equipmentSlot.getEntitySlotId()];
            multimap.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier(modifierId, "Strength modifier", 50, EntityAttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    static class Material implements ArmorMaterial {

        @Override
        public int getDurability(EquipmentSlot slot) {
            return 200;
        }

        @Override
        public int getProtectionAmount(EquipmentSlot slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(UItems.alicorn_amulet);
        }

        @Override
        public String getName() {
            return "alicorn_amulet";
        }

        @Override
        public float getToughness() {
            return 20;
        }
    }
}
