package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EnchantedStaffItem extends StaffItem implements EnchantableItem, ChargeableItem {

    private static final Map<EntityType<?>, SpellType<?>> ENTITY_TYPE_TO_SPELL = new HashMap<>();
    public static <T extends Spell> SpellType<T> register(EntityType<?> entityType, SpellType<T> spellType) {
        ENTITY_TYPE_TO_SPELL.put(entityType, spellType);
        return spellType;
    }

    public static SpellType<?> getSpellType(Entity entity) {
        if (entity instanceof CastSpellEntity cast) {
            return cast.getSpellSlot().get(c -> !SpellPredicate.IS_PLACED.test(c), true).map(Spell::getType).orElse(SpellType.empty());
        }
        if (entity instanceof PlayerEntity player) {
            return Pony.of(player).getCharms().getEquippedSpell(Hand.MAIN_HAND).type();
        }
        return ENTITY_TYPE_TO_SPELL.getOrDefault(entity.getType(), SpellType.empty());
    }

    static {
        register(EntityType.DROWNED, SpellType.BUBBLE);
        register(EntityType.DOLPHIN, SpellType.BUBBLE);
        register(EntityType.BLAZE, SpellType.FIRE_BOLT);
        register(EntityType.CHICKEN, SpellType.FEATHER_FALL);
        register(EntityType.CREEPER, SpellType.CATAPULT);
        register(EntityType.HUSK, SpellType.HYDROPHOBIC);
        register(EntityType.SNOW_GOLEM, SpellType.FROST);
        register(EntityType.FIREBALL, SpellType.FLAME);
        register(EntityType.SMALL_FIREBALL, SpellType.FLAME);
        register(EntityType.ENDER_DRAGON, SpellType.DISPLACEMENT);
        register(EntityType.GUARDIAN, SpellType.AWKWARD);
        register(EntityType.ELDER_GUARDIAN, SpellType.AWKWARD);
        register(EntityType.DRAGON_FIREBALL, SpellType.INFERNAL);
        register(EntityType.CAVE_SPIDER, SpellType.REVEALING);
        register(EntityType.ZOMBIE, SpellType.NECROMANCY);
        register(EntityType.VEX, SpellType.NECROMANCY);
        register(EntityType.SKELETON, SpellType.CATAPULT);
        register(EntityType.WITHER_SKELETON, SpellType.CATAPULT);
        register(EntityType.SKELETON_HORSE, SpellType.CATAPULT);
        register(UEntities.TWITTERMITE, SpellType.LIGHT);
    }

    public EnchantedStaffItem(Settings settings) {
        super(settings.maxDamage(500));
    }

    @Override
    public ItemStack getDefaultStack() {
        return EnchantableItem.enchant(super.getDefaultStack(), SpellType.FIRE_BOLT);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> lines, TooltipContext context) {

        if (EnchantableItem.isEnchanted(stack)) {
            SpellType<?> key = EnchantableItem.getSpellKey(stack);
            lines.add(Text.translatable(key.getTranslationKey()).formatted(key.getAffinity().getColor()));
            lines.add(Text.translatable(getTranslationKey(stack) + ".charges", (int)Math.floor(ChargeableItem.getEnergy(stack)), getMaxCharge()));
        }
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (EnchantableItem.isEnchanted(stack)) {
            return ActionResult.PASS;
        }

        super.useOnEntity(stack, player, target, hand);

        SpellType<?> type = getSpellType(target);
        if (!type.isEmpty()) {
            target.setHealth(1);
            target.setFrozenTicks(9000);
            player.setStackInHand(hand, recharge(EnchantableItem.enchant(stack, type)));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack =  player.getStackInHand(hand);
        player.setCurrentHand(hand);
        return TypedActionResult.consume(itemstack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft) {
        int i = getMaxUseTime(stack) - timeLeft;

        if (EnchantableItem.isEnchanted(stack) && hasCharge(stack)) {
            if (i > 20) {
                Pony.of(entity).ifPresent(pony -> {
                    pony.subtractEnergyCost(4);
                    stack.damage(1, pony.asEntity(), p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                    getSpellEffect(stack).create().toThrowable().throwProjectile(pony);
                });
                ChargeableItem.consumeEnergy(stack, 1);
            } else if (i > 5) {
                Pony.of(entity).ifPresent(pony -> {
                    pony.subtractEnergyCost(4);
                    stack.damage(1, pony.asEntity(), p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                    getSpellEffect(stack).create().toThrowable().throwProjectile(pony);
                });
                ChargeableItem.consumeEnergy(stack, 1);
            }
        }
    }

    @Override
    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.isSneaking() && hasCharge(stack)) {
            stack.damage(50, attacker, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            Caster.of(attacker).ifPresent(c -> c.subtractEnergyCost(4));
            Caster.of(target).ifPresent(c -> getSpellEffect(stack).create().apply(c));
            ChargeableItem.consumeEnergy(stack, 1);

            return true;
        }

        return false;
    }

    @Override
    public void usageTick(World world, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = entity;

            if (living.getActiveItem().getItem() == this) {
                Vec3d eyes = entity.getCameraPosVec(1);

                float i = getMaxUseTime(stack) - ticksRemaining;

                world.addParticle(i > 150 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes.x, eyes.y, eyes.z,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10
                );
                world.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.PLAYERS, 1, i / 20);

                if (i > 200) {
                    living.clearActiveItem();
                    living.damage(DamageSource.MAGIC, 1);
                    if (EnchantableItem.isEnchanted(stack) && hasCharge(stack)) {
                        Caster.of(entity).ifPresent(c -> getSpellEffect(stack).create().apply(c));
                        ChargeableItem.consumeEnergy(stack, 1);
                    }
                }
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public int getMaxCharge() {
        return 3;
    }

    @Override
    public int getDefaultCharge() {
        return 3;
    }

    @Override
    public void onDischarge(ItemStack stack) {
        if ((stack.hasNbt() && stack.getNbt().contains("energy") ? stack.getNbt().getFloat("energy") : 0) == 0) {
            EnchantableItem.unenchant(stack);
        }
    }
}
