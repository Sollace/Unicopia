package com.minelittlepony.unicopia.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class HorseShoeItem extends HeavyProjectileItem {

    private final float projectileInnacuracy;
    private final float baseProjectileSpeed;

    public HorseShoeItem(Item.Settings settings, float projectileDamage, float projectileInnacuracy, float baseProjectileSpeed) {
        super(settings.attributeModifiers(AttributeModifiersComponent.builder().add(
            UEntityAttributes.EXTENDED_ATTACK_DISTANCE,
            new EntityAttributeModifier(PolearmItem.ATTACK_RANGE_MODIFIER_ID, -3F, EntityAttributeModifier.Operation.ADD_VALUE),
            AttributeModifierSlot.MAINHAND
    ).build()), projectileDamage);
        this.projectileInnacuracy = projectileInnacuracy;
        this.baseProjectileSpeed = baseProjectileSpeed;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(this);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        float degradation = (stack.getDamage() / (float)stack.getMaxDamage());
        float inaccuracy = projectileInnacuracy + degradation * 30;
        tooltip.add(Text.empty());

        var race = InteractionManager.getInstance().getClientPony().map(Pony::getCompositeRace).orElse(null);
        float speed = baseProjectileSpeed;
        if (race != null) {
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
    public PhysicsBodyProjectileEntity createProjectile(ItemStack stack, World world, @Nullable PlayerEntity player) {
        PhysicsBodyProjectileEntity projectile = super.createProjectile(stack, world, player);
        projectile.setDamageType(UDamageTypes.HORSESHOE);

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
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.Vanilla.ITEM_TRIDENT_THROW.value();
    }
}
