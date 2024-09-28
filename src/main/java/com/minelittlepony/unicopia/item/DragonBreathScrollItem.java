package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.server.world.DragonBreathStore;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DragonBreathScrollItem extends Item {

    public DragonBreathScrollItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        ItemStack payload = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

        if (payload.isEmpty() || !stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            return TypedActionResult.fail(stack);
        }

        stack.split(1);
        if (!world.isClient) {
            String recipient = stack.get(DataComponentTypes.CUSTOM_NAME).getString();
            UCriteria.SEND_DRAGON_BREATH.triggerSent(player, payload, recipient, (counterName, count) -> {
               if (count == 1 && "dings_on_celestias_head".equals(counterName)) {
                   UnicopiaWorldProperties properties = UnicopiaWorldProperties.forWorld((ServerWorld)world);
                   properties.setTangentalSkyAngle(properties.getTangentalSkyAngle() + 15);
                   world.playSound(null, player.getBlockPos(), USounds.Vanilla.BLOCK_ANVIL_LAND, SoundCategory.NEUTRAL, 0.2F, (float)world.random.nextTriangular(1, 0.2F));
               }
            });
            DragonBreathStore.get(world).put(recipient, payload.split(1));
        }
        player.playSound(USounds.ITEM_DRAGON_BREATH_SCROLL_USE, 1, 1);
        return TypedActionResult.consume(stack);
    }
}
