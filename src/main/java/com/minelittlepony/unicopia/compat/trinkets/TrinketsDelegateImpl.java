package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.AccessoryRegistry;
import io.wispforest.accessories.api.DropRule;
import io.wispforest.accessories.api.events.OnDropCallback;
import io.wispforest.accessories.api.menu.AccessoriesBasedSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class TrinketsDelegateImpl implements TrinketsDelegate {
    public static final TrinketsDelegateImpl INSTANCE = new TrinketsDelegateImpl();

    // who tf designed this api?

    @Override
    public void bootstrap() {
        OnDropCallback.EVENT.register((rule, stack, ref, entity) -> {
            if (EnchantmentUtil.getLevel(UEnchantments.HEART_BOUND, stack) > 0) {
                return DropRule.KEEP;
            }
            return rule;
        });
    }

    @Override
    public boolean equipStack(LivingEntity entity, ItemStack stack) {
        return getTrinketComponent(entity).map(component -> component.attemptToEquipAccessory(stack, false)).isPresent();
    }

    @Override
    public void registerTrinket(Item item) {
        AccessoryRegistry.register(item, new UnicopiaTrinket(item));
    }

    private Optional<AccessoriesCapability> getTrinketComponent(LivingEntity entity) {
        try {
            return AccessoriesCapability.getOptionally(entity);
        } catch (Throwable ingnored) {}
        return Optional.empty();
    }

    private Optional<AccessoriesContainer> getContainer(LivingEntity entity, SlotKey slot) {
        return getTrinketComponent(entity).map(component -> component.getContainers().get(slot.toString()));
    }

    @Override
    public Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, SlotKey slotId, int i, int x, int y) {
        return getContainer(entity, slotId).map(container -> new SpellbookTrinketSlot(handler, AccessoriesBasedSlot.of(container.capability().entity(), container.slotType(), i, x, y)));
    }

    @Override
    public boolean isTrinketSlot(Slot slot) {
        return slot instanceof AccessoriesBasedSlot || slot instanceof SpellbookTrinketSlot;
    }
}
