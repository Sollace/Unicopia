package com.minelittlepony.unicopia.ability;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileCapable;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class UnicornProjectileAbility implements Ability<Hit> {

    /**
     * The icon representing this ability on the UI and HUD.
     */
    @Override
    public Identifier getIcon(Pony player, boolean swap) {
        Identifier id = Abilities.REGISTRY.getId(this);
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + (swap ? "_focused" : "_unfocused") + ".png");
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 4;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canCast();
    }

    @Override
    public Hit tryActivate(Pony player) {
        return Hit.of(getNewSpell(player).getResult() != ActionResult.FAIL);
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 7;
    }

    @Override
    public void apply(Pony player, Hit data) {
        TypedActionResult<SpellType<?>> thrown = getNewSpell(player);

        if (thrown.getResult() != ActionResult.FAIL) {
            @Nullable
            SpellType<?> spell = thrown.getValue();

            if (spell == null) {
                spell = SpellType.VORTEX;
            }

            player.subtractEnergyCost(getCostEstimate(player));
            ((ProjectileCapable)spell.create(SpellTraits.EMPTY)).toss(player);
        }
    }

    private TypedActionResult<SpellType<?>> getNewSpell(Pony player) {
        return Streams.stream(player.getMaster().getItemsHand())
                .filter(GemstoneItem::isEnchanted)
                .map(stack -> GemstoneItem.consumeSpell(stack, player.getMaster(), null, null))
                .findFirst()
                .orElse(TypedActionResult.<SpellType<?>>pass(null));
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExhaustion().multiply(3.3F);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
