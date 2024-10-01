package com.minelittlepony.unicopia.item.component;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public record Charges(int energy, int maximum, int baseline, boolean showInTooltip) implements TooltipAppender {
    public static final Charges DEFAULT = new Charges(0, 0, 0, false);
    public static final Codec<Charges> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("energy").forGetter(Charges::energy),
            Codec.INT.optionalFieldOf("maximum", 0).forGetter(Charges::maximum),
            Codec.INT.optionalFieldOf("baseline", 0).forGetter(Charges::baseline),
            Codec.BOOL.fieldOf("showInTooltip").forGetter(Charges::showInTooltip)
    ).apply(instance, Charges::new));
    public static final PacketCodec<ByteBuf, Charges> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, Charges::energy,
            PacketCodecs.INTEGER, Charges::maximum,
            PacketCodecs.INTEGER, Charges::baseline,
            PacketCodecs.BOOL, Charges::showInTooltip,
            Charges::new
    );

    public static Charges of(int initial, int maximum) {
        return new Charges(initial, maximum, initial, true);
    }

    public static Charges of(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.CHARGES, DEFAULT);
    }

    public static boolean discharge(ItemStack stack, int energy) {
        Charges charges = of(stack);
        if (charges.energy() < energy) {
            return false;
        }
        stack.set(UDataComponentTypes.CHARGES, charges.withEnergy(charges.energy() - energy));
        if (stack.getItem() instanceof ChargeChangeCallback callback) {
            callback.onDischarge(stack);
        }
        return true;
    }

    public static boolean recharge(ItemStack stack, int energy) {
        Charges charges = of(stack);
        if (charges.energy() >= charges.maximum()) {
            return false;
        }
        stack.set(UDataComponentTypes.CHARGES, charges.withEnergy(charges.energy() + energy));
        return true;
    }

    public static boolean recharge(ItemStack stack) {
        Charges charges = of(stack);
        if (charges.energy() >= charges.maximum()) {
            return false;
        }

        stack.set(UDataComponentTypes.CHARGES, charges.ofDefault());
        return true;
    }

    public boolean canHoldCharge() {
        return maximum > 0;
    }

    public float getPercentage() {
        return canHoldCharge() ? energy() / (float)maximum() : 0;
    }

    public Charges withEnergy(int energy) {
        return new Charges(MathHelper.clamp(energy, 0, maximum), maximum, baseline, showInTooltip);
    }

    public Charges ofDefault() {
        return withEnergy(baseline);
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (showInTooltip() && maximum() > 0) {
            tooltip.accept(Text.translatable("item.unicopia.amulet.energy", (int)Math.floor(energy()), maximum()));
        }
    }

    public interface ChargeChangeCallback {
        void onDischarge(ItemStack stack);
    }
}
