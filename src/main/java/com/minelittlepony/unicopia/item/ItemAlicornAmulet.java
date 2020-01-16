package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.items.IDependable;
import com.minelittlepony.unicopia.spell.SpellAffinity;
import com.minelittlepony.unicopia.world.UWorld;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.VecHelper;
import com.minelittlepony.util.lang.ClientLocale;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class ItemAlicornAmulet extends ItemArmor implements IDependable {

    public static final DamageSource DAMAGE_SOURCE = MagicalDamageSource.create("alicorn_amulet")
            .setDamageBypassesArmor()
            .setDamageIsAbsolute();

    private static final UUID[] ARMOR_MODIFIERS = new UUID[] {
            UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
            UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
            UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
            UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
    };

    public ItemAlicornAmulet(String domain, String name) {
        super(ArmorMaterial.GOLD, 1, EntityEquipmentSlot.CHEST);

        setTranslationKey(name);
        setRegistryName(domain, name);
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return toRepair.getItem() == repair.getItem();
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entity) {

        World world = entity.world;

        double x = entity.posX + world.rand.nextFloat() - 0.5;
        double z = entity.posZ + world.rand.nextFloat() - 0.5;
        double y = entity.posY + world.rand.nextFloat();

        EnumParticleTypes particle = world.rand.nextBoolean() ? EnumParticleTypes.SMOKE_LARGE : EnumParticleTypes.FLAME;

        world.spawnParticle(particle, x, y, z, 0, 0, 0);

        if (world.rand.nextInt(500) == 0) {
            world.playSound(null, entity.getPosition(), SoundEvents.AMBIENT_CAVE, SoundCategory.HOSTILE, 0.5F, 1);
        }

        Vec3d position = entity.getPositionVector();
        VecHelper.findAllEntitiesInRange(entity, world, entity.getPosition(), 10)
            .filter(e -> e instanceof PlayerEntity)
            .sorted((a, b) -> (int)(a.getPositionVector().distanceTo(position) - b.getPositionVector().distanceTo(position)))
            .findFirst()
            .ifPresent(player -> interactWithPlayer(entity, (PlayerEntity)player));

        return false;
    }

    protected void interactWithPlayer(EntityItem entity, PlayerEntity player) {
        double diffX = player.posX - entity.posX;
        double diffY = player.posY - entity.posY;
        double diffZ = player.posZ - entity.posZ;

        entity.move(MoverType.SELF, diffX / 50, diffY / 50, diffZ / 50);

        if (!player.world.isClient && !entity.isDead) {
            if (player.getPositionVector().distanceTo(entity.getPositionVector()) < 3) {
               if (entity.world.rand.nextInt(150) == 0) {

                   TypedActionResult<ItemStack> result = onItemRightClick(player.world, player, EnumHand.MAIN_HAND);

                   if (result.getType() == EnumActionResult.SUCCESS) {
                       entity.setPickupDelay(1000);
                       entity.setDead();
                   }
               }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        IPlayer iplayer = UClient.instance().getIPlayer();

        if (iplayer != null) {
            int attachedTime = iplayer.getInventory().getTicksAttached(this);
            if (attachedTime > 0) {
                tooltip.add(ClientLocale.format(getTranslationKey() + ".tagline", StringUtils.ticksToElapsedTime(attachedTime)));
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound compound = stack.getTagCompound();

        int hideFlags = 0;

        if (!compound.hasKey("HideFlags") || ((hideFlags = compound.getInteger("HideFlags")) & 2) == 0) {
            compound.setInteger("HideFlags", hideFlags | 2);
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void onArmorTick(World world, PlayerEntity player, ItemStack itemStack) {
        if (player.getHealth() < player.getMaxHealth()) {
            player.heal(0.5F);
        } else if (player.canEat(false)) {
            player.getFoodStats().addStats(1, 0);
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
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 3, 1);
        }

        if (attachedTime > 0 && attachedTime % 100 == 0) {
            world.playSound(null, player.getPosition(), SoundEvents.MUSIC_NETHER, SoundCategory.PLAYERS, 3, 1);
        }

        if (attachedTime > 1000) {
            if (world.rand.nextInt(700) == 0) {
                player.dropItem(false);
            }
        }

        if (attachedTime > 3000) {
            if (world.rand.nextInt(300) == 0) {
                player.motionX += world.rand.nextFloat() - 0.5F;
            }

            if (world.rand.nextInt(300) == 0) {
                player.motionZ += world.rand.nextFloat() - 0.5F;
            }
        }

        if (attachedTime > 6000) {
            if (world.rand.nextInt(300) == 0) {
                player.rotationYaw += 180;
            }
        }

        if (attachedTime > 13000) {
            if (world.rand.nextInt(300) == 0) {
                player.attackEntityFrom(DAMAGE_SOURCE, 1F);
            }
        }

        if (itemStack.getItemDamage() >= getMaxDamage(itemStack) - 1) {
            itemStack.damageItem(10, player);

            player.attackEntityFrom(DAMAGE_SOURCE, player.getMaxHealth() - 0.01F);
            player.getFoodStats().setFoodLevel(1);

            Vec3d pos = player.getPositionVector();

            player.world.newExplosion(player, pos.x, pos.y, pos.z, 10, false, false);

            UWorld.scheduleTask(w -> {
                w.newExplosion(player, pos.x, pos.y, pos.z, 6, false, true);
            }, 50);
        }

        iplayer.getInventory().enforceDependency(this);
    }

    @Override
    public void onRemoved(IPlayer player, float needfulness) {

        float attachedTime = player.getInventory().getTicksAttached(this) / 100F;

        DifficultyInstance difficulty = player.getWorld().getDifficultyForLocation(player.getOrigin());
        float amount = (attachedTime * (1 + needfulness)) * (1 + difficulty.getClampedAdditionalDifficulty());

        amount = Math.min(amount, player.getOwner().getMaxHealth());

        player.getOwner().attackEntityFrom(DAMAGE_SOURCE, amount);

        if (attachedTime > 120) {
            player.getOwner().knockBack(player.getOwner(), 1, 1, 1);
        }
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "unicopia:textures/models/armor/alicorn_amulet.png";
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();;

        if (equipmentSlot == armorType) {
            UUID modifierId = ARMOR_MODIFIERS[equipmentSlot.getIndex()];

            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(modifierId, "Armor modifier", Integer.MAX_VALUE, 0));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(modifierId, "Armor toughness", 20, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(modifierId, "Strength modifier", 50, 0));
        }

        return multimap;
    }
}
