package com.minelittlepony.unicopia.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class HorseShoeItem extends HeavyProjectileItem {

    private final float projectileInnacuracy;
    private final float baseProjectileSpeed;

    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public HorseShoeItem(Settings settings, float projectileDamage, float projectileInnacuracy, float baseProjectileSpeed) {
        super(settings, projectileDamage);
        this.projectileInnacuracy = projectileInnacuracy;
        this.baseProjectileSpeed = baseProjectileSpeed;

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(UEntityAttributes.EXTENDED_ATTACK_DISTANCE, new EntityAttributeModifier(PolearmItem.ATTACK_RANGE_MODIFIER_ID, "Weapon modifier", -3F, EntityAttributeModifier.Operation.ADDITION));
        attributeModifiers = builder.build();
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(this);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        float degradation = (stack.getDamage() / (float)stack.getMaxDamage());
        float inaccuracy = projectileInnacuracy + degradation * 30;
        tooltip.add(Text.empty());

        Pony pony = Unicopia.SIDE.getPony().orElse(null);
        float speed = baseProjectileSpeed;
        if (pony != null) {
            var race = pony.getCompositeRace();

            if (race.any(Race::canUseEarth)) {
                speed += 0.5F;
            }
            if (!race.includes(Race.ALICORN) && race.physical().canFly()) {
                speed /= 1.5F;
            }

        }
        speed /= 1.5F;
        speed *= 1 - (0.6F * degradation);

        tooltip.add(Text.translatable("item.unicopia.horse_shoe.accuracy", 100 * (30 - inaccuracy) / 30).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.unicopia.horse_shoe.speed", Math.max(0.2F, speed)).formatted(Formatting.GRAY));
    }

    @Override
    protected PhysicsBodyProjectileEntity createProjectile(ItemStack stack, World world, @Nullable PlayerEntity player) {
        PhysicsBodyProjectileEntity projectile = super.createProjectile(stack, world, player);

        float degradation = (stack.getDamage() / (float)stack.getMaxDamage());

        if (player != null) {
            Pony pony = Pony.of(player);
            var race = pony.getCompositeRace();
            float speed = baseProjectileSpeed + 0.1F;
            if (race.any(Race::canUseEarth)) {
                speed += 0.5F;
            }
            if (!race.includes(Race.ALICORN) && race.physical().canFly()) {
                speed /= 1.5F;
            }
            speed /= 1.5F;
            speed *= 1 - (0.6F * degradation);
            float inaccuracy = projectileInnacuracy + degradation * 30;
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0, Math.max(0.2F, speed), inaccuracy);
        }
        return projectile;
    }

    @Override
    protected SoundEvent getThrowSound(ItemStack stack) {
        return USounds.Vanilla.ITEM_TRIDENT_THROW;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return attributeModifiers;
        }
        return super.getAttributeModifiers(slot);
    }

}
