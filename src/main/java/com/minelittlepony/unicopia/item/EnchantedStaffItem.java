package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.group.MultiItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EnchantedStaffItem extends StaffItem implements EnchantableItem, ChargeableItem, MultiItem {

    private static final Map<EntityType<?>, SpellType<?>> ENTITY_TYPE_TO_SPELL = new HashMap<>();
    public static <T extends Spell> SpellType<T> register(EntityType<?> entityType, SpellType<T> spellType) {
        ENTITY_TYPE_TO_SPELL.put(entityType, spellType);
        return spellType;
    }

    public static SpellType<?> getSpellType(Entity entity, boolean remove) {
        if (entity instanceof CastSpellEntity cast) {
            return cast.getSpellSlot().get(SpellType.PLACE_CONTROL_SPELL.negate())
                    .map(Spell::getTypeAndTraits)
                    .map(CustomisedSpellType::type)
                    .orElse(SpellType.empty());
        }
        if (entity instanceof PlayerEntity player) {
            if (remove) {
                return Pony.of(player).getCharms().equipSpell(Hand.MAIN_HAND, SpellType.EMPTY_KEY.withTraits()).type();
            }
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
        register(EntityType.POLAR_BEAR, SpellType.FROST);
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
    public List<ItemStack> getDefaultStacks() {
        return Stream.concat(
                Stream.of(getDefaultStack()),
                ENTITY_TYPE_TO_SPELL.values().stream().distinct().map(type -> EnchantableItem.enchant(getDefaultStack(), type))
            ).toList();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> lines, TooltipType type) {

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

        boolean remove = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(target);
        SpellType<?> type = getSpellType(target, remove);
        if (!type.isEmpty()) {
            if (remove) {
                target.setHealth(1);
                target.setFrozenTicks(9000);
            }
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
        int i = getMaxUseTime(stack, entity) - timeLeft;

        if (EnchantableItem.isEnchanted(stack) && hasCharge(stack)) {
            if (i > 20) {
                Pony.of(entity).ifPresent(pony -> {
                    pony.subtractEnergyCost(4);
                    stack.damage(1, pony.asEntity(), EquipmentSlot.MAINHAND);
                    getSpellEffect(stack).create().toThrowable().throwProjectile(pony);
                    pony.setAnimation(Animation.ARMS_UP, Animation.Recipient.ANYONE, 10);
                });
                ChargeableItem.consumeEnergy(stack, 1);
            } else if (i > 5) {
                Pony.of(entity).ifPresent(pony -> {
                    pony.subtractEnergyCost(4);
                    stack.damage(1, pony.asEntity(), EquipmentSlot.MAINHAND);
                    getSpellEffect(stack).create().toThrowable().throwProjectile(pony);
                    pony.setAnimation(Animation.ARMS_UP, Animation.Recipient.ANYONE, 10);
                });
                ChargeableItem.consumeEnergy(stack, 1);
            }
        }
    }

    @Override
    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.isSneaking() && hasCharge(stack)) {
            stack.damage(50, attacker, EquipmentSlot.MAINHAND);
            Caster.of(attacker).ifPresent(c -> c.subtractEnergyCost(4));
            Caster.of(target).ifPresent(c -> getSpellEffect(stack).apply(c, CastingMethod.STAFF));
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

                float i = getMaxUseTime(stack, entity) - ticksRemaining;

                world.addParticle(i > 150 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes.x, eyes.y, eyes.z,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10
                );
                world.playSound(null, entity.getBlockPos(), USounds.ITEM_MAGIC_STAFF_CHARGE, SoundCategory.PLAYERS, 1, i / 20);

                if (i > 200) {
                    living.clearActiveItem();
                    living.damage(entity.getDamageSources().magic(), 1);
                    if (EnchantableItem.isEnchanted(stack) && hasCharge(stack)) {
                        Caster.of(entity).ifPresent(c -> getSpellEffect(stack).apply(c, CastingMethod.STAFF));
                        ChargeableItem.consumeEnergy(stack, 1);
                    }
                }
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
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
    public Text getName(ItemStack stack) {
        if (EnchantableItem.isEnchanted(stack) && hasCharge(stack)) {
            return Text.translatable(this.getTranslationKey(stack) + ".enchanted", super.getName(stack), EnchantableItem.getSpellKey(stack).getName());
        }
        return super.getName(stack);
    }

    @Override
    public void onDischarge(ItemStack stack) {
        if ((stack.hasNbt() && stack.getNbt().contains("energy") ? stack.getNbt().getFloat("energy") : 0) == 0) {
            EnchantableItem.unenchant(stack);
        }
    }
}
