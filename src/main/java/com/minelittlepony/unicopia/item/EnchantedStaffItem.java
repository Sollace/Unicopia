package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.group.MultiItem;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EnchantedStaffItem extends StaffItem implements EnchantableItem, MultiItem, Charges.ChargeChangeCallback {
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
        super(settings.component(UDataComponentTypes.CHARGES, new Charges(3, 3, 3, true)).maxDamage(500));
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return Stream.concat(
                Stream.of(getDefaultStack()),
                ENTITY_TYPE_TO_SPELL.values().stream().distinct().map(type -> EnchantableItem.enchant(getDefaultStack(), type))
            ).toList();
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        if (EnchantableItem.isEnchanted(stack)) {
            SpellType<?> key = EnchantableItem.getSpellKey(stack);
            textConsumer.accept(Text.translatable(key.getTranslationKey()).formatted(key.getAffinity().getColor()));
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
            stack = EnchantableItem.enchant(stack, type);
            Charges.recharge(stack);
            player.setStackInHand(hand, stack);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft) {
        int i = getMaxUseTime(stack, entity) - timeLeft;

        if (i > 5 && EnchantableItem.isEnchanted(stack)) {
            CustomisedSpellType<?> spellType = EnchantableItem.getSpellEffect(stack);
            if (Charges.discharge(stack, 1)) {
                Pony.of(entity).ifPresent(pony -> {
                    spellType.apply(pony, CastingMethod.STAFF);
                    pony.setAnimation(Animation.ARMS_UP, Animation.Recipient.ANYONE, 10);
                    stack.damage(1, pony.asEntity(), EquipmentSlot.MAINHAND);
                    pony.subtractEnergyCost(4);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean castContainedEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.isSneaking()) {
            CustomisedSpellType<?> spellType = EnchantableItem.getSpellEffect(stack);
            if (Charges.discharge(stack, 1)) {
                stack.damage(50, attacker, EquipmentSlot.MAINHAND);
                Caster.of(attacker).ifPresent(c -> c.subtractEnergyCost(4));
                Caster.of(target).ifPresent(c -> spellType.apply(c, CastingMethod.STAFF));
            }
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

                world.addParticleClient(i > 150 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes.x, eyes.y, eyes.z,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10,
                        (world.random.nextGaussian() - 0.5) / 10
                );
                world.playSound(null, entity.getBlockPos(), USounds.ITEM_MAGIC_STAFF_CHARGE, SoundCategory.PLAYERS, 1, i / 20);

                if (i > 200 && world instanceof ServerWorld sw) {
                    living.clearActiveItem();
                    living.damage(sw, entity.getDamageSources().magic(), 1);
                    if (EnchantableItem.isEnchanted(stack)) {
                        CustomisedSpellType<?> spellType = EnchantableItem.getSpellEffect(stack);
                        if (Charges.discharge(stack, 1)) {
                            Caster.of(entity).ifPresent(c -> spellType.apply(c, CastingMethod.STAFF));
                        }
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
    public Text getName(ItemStack stack) {
        if (EnchantableItem.isEnchanted(stack) && Charges.of(stack).energy() > 0) {
            return Text.translatable(getTranslationKey() + ".enchanted", super.getName(stack), EnchantableItem.getSpellKey(stack).getName());
        }
        return super.getName(stack);
    }

    @Override
    public void onDischarge(ItemStack stack) {
        if (Charges.of(stack).energy() == 0) {
            EnchantableItem.unenchant(stack);
        }
    }
}
