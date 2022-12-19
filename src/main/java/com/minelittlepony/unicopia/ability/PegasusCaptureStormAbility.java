package com.minelittlepony.unicopia.ability;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

/**
 * Pegasus ability to capture a storm and store it in a jar
 */
public class PegasusCaptureStormAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 9;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 6;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canInteractWithClouds();
    }

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {

        if (!player.asEntity().isCreative() && player.getMagicalReserves().getMana().getPercentFill() < 0.2F) {
            return null;
        }

        return Hit.INSTANCE;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return player.getMagicalReserves().getMana().getMax() * 0.9F;
    }

    @Override
    public void apply(Pony player, Hit data) {

        World w = player.asWorld();
        ItemStack stack = player.asEntity().getStackInHand(Hand.MAIN_HAND);
        boolean thundering = w.isThundering();

        if (stack.getItem() != UItems.EMPTY_JAR) {
            tell(player, "ability.unicopia.empty_hooves");
        } else if (!w.isSkyVisible(player.getOrigin())) {
            tell(player, "ability.unicopia.indoors");
        } else if (!(w.isRaining() || thundering)) {
            tell(player, "ability.unicopia.clear_skies");
        } else if (player.getOrigin().getY() < 120) {
            tell(player, "ability.unicopia.too_low");
        } else {
            if (!player.asEntity().getAbilities().creativeMode) {
                stack.decrement(1);
            }

            if (thundering && w.random.nextBoolean()) {
                player.asEntity().giveItemStack(UItems.STORM_CLOUD_JAR.getDefaultStack());

                if (w instanceof ServerWorld) {
                    ServerWorldProperties props = (ServerWorldProperties)w.getLevelProperties();

                    int timer = (int)Math.floor(props.getRainTime() * 0.1);

                    props.setThundering(timer > 0);
                    props.setClearWeatherTime((int)(w.random.nextFloat() * 150000));
                    props.setThunderTime(timer);
                    ((ServerWorld)w).getServer().getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, w.getRainGradient(1)), w.getRegistryKey());
                }
            } else {
                player.asEntity().giveItemStack(UItems.RAIN_CLOUD_JAR.getDefaultStack());

                if (w instanceof ServerWorld) {
                    ServerWorldProperties props = (ServerWorldProperties)w.getLevelProperties();

                    int timer = (int)Math.floor(props.getRainTime() * 0.1);

                    props.setRaining(timer > 0);
                    props.setClearWeatherTime((int)(w.random.nextFloat() * 150000));
                    props.setRainTime(timer);
                    ((ServerWorld)w).getServer().getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, w.getRainGradient(1)), w.getRegistryKey());
                }
            }
        }

    }

    private void tell(Pony player, String translation) {
        player.asEntity().sendMessage(Text.translatable(translation), true);
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().add(6);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
