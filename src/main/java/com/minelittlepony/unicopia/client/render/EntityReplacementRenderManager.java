package com.minelittlepony.unicopia.client.render;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;

class EntityReplacementRenderManager implements Disguise {
    public static final EntityReplacementRenderManager INSTANCE = new EntityReplacementRenderManager();

    @Nullable
    private EntityType<?> currentType;
    private final EntityAppearance disguise = new EntityAppearance();
    private final List<EntityType<?>> defaultTypes = List.of(
            EntityType.ZOMBIE,
            EntityType.CREEPER,
            EntityType.HUSK,
            EntityType.SKELETON,
            EntityType.BLAZE,
            EntityType.DROWNED
    );
    private final Map<EntityType<?>, List<EntityType<?>>> pools = Map.of(
            EntityType.VILLAGER, List.of(EntityType.PILLAGER),
            EntityType.PLAYER, List.of(EntityType.ZOMBIE),
            EntityType.PIGLIN, List.of(EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE),
            EntityType.SQUID, List.of(EntityType.DROWNED),
            EntityType.DOLPHIN, List.of(EntityType.DROWNED),
            EntityType.ENDER_DRAGON, List.of(EntityType.CHICKEN)
    );

    public Optional<Disguise> getAppearanceFor(Caster<?> caster) {

        if (isRageApplicable(caster.asEntity()) && EquinePredicates.RAGING.test(MinecraftClient.getInstance().player)) {
            List<EntityType<?>> mobTypes = getMobTypePool(caster.asEntity().getType());
            EntityType<?> type = mobTypes.get(Math.abs((int)caster.asEntity().getUuid().getMostSignificantBits()) % mobTypes.size());
            if (type != currentType) {
                currentType = type;
                disguise.setAppearance(type.create(caster.asWorld()));
            }
            return Optional.of(this);
        }

        return caster.getSpellSlot().get(SpellPredicate.IS_DISGUISE, false).map(Disguise.class::cast);
    }

    private List<EntityType<?>> getMobTypePool(EntityType<?> type) {
        return pools.getOrDefault(type, defaultTypes);
    }

    private boolean isRageApplicable(Entity entity) {
        return entity != MinecraftClient.getInstance().player && (entity instanceof PassiveEntity || entity instanceof PlayerEntity || pools.containsKey(entity.getType()));
    }

    @Override
    public EntityAppearance getDisguise() {
        return disguise;
    }

    @Override
    public void setDirty() {
    }

    @Override
    public boolean isDead() {
        return false;
    }
}
