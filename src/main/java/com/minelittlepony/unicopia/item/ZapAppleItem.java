package com.minelittlepony.unicopia.item;

import java.util.List;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.group.MultiItem;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ZapAppleItem extends Item implements ChameleonItem, MultiItem {
    public ZapAppleItem(Settings settings) {
        super(settings.rarity(Rarity.RARE));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        return TraceHelper.findEntity(player, 5, 1, e -> canFeedTo(stack, e))
                .map(entity -> onFedTo(stack, player, entity))
                .orElseGet(() -> super.use(world, player, hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World w, LivingEntity player) {
        stack = super.finishUsing(stack, w, player);

        player.damage(Living.living(player).damageOf(UDamageTypes.ZAP_APPLE), 120);

        if (w instanceof ServerWorld) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(w);
            lightning.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());

            player.onStruckByLightning((ServerWorld)w, lightning);

            if (player instanceof PlayerEntity) {
                UCriteria.EAT_TRICK_APPLE.trigger(player);
            }
        }

        player.emitGameEvent(GameEvent.LIGHTNING_STRIKE);
        ParticleUtils.spawnParticle(w, LightningBoltParticleEffect.DEFAULT, player.getPos(), Vec3d.ZERO);

        return stack;
    }

    public boolean canFeedTo(ItemStack stack, Entity e) {
        return e instanceof VillagerEntity
                || e instanceof CreeperEntity
                || e instanceof PigEntity;
    }

    public TypedActionResult<ItemStack> onFedTo(ItemStack stack, PlayerEntity player, Entity e) {

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(e.getWorld());
        lightning.refreshPositionAfterTeleport(e.getX(), e.getY(), e.getZ());
        lightning.setCosmetic(true);
        if (player instanceof ServerPlayerEntity) {
            lightning.setChanneler((ServerPlayerEntity)player);
        }

        if (!e.getWorld().isClient) {
            e.onStruckByLightning((ServerWorld)e.getWorld(), lightning);
            UCriteria.FEED_TRICK_APPLE.trigger(player);
        }
        player.getWorld().spawnEntity(lightning);

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return InteractionManager.getInstance().getClientPony().map(Pony::asWorld)
                .stream()
                .flatMap(world -> RegistryUtils.valuesForTag(world, UConventionalTags.Items.APPLES))
                .filter(a -> a != this).map(item -> {
            return ChameleonItem.setAppearance(getDefaultStack(), item.getDefaultStack());
        }).toList();
    }

    @Override
    public Text getName(ItemStack stack) {
        return ChameleonItem.hasAppearance(stack) ? ChameleonItem.getAppearanceStack(stack).getName() : super.getName(stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        if (ChameleonItem.hasAppearance(stack)) {
            return Rarity.EPIC;
        }

        return Rarity.RARE;
    }
}
