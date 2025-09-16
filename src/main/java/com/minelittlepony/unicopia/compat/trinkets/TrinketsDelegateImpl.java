package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgTrinketBroken;

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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

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
    public ActionResult equipStack(LivingEntity entity, ItemStack stack) {
        return getTrinketComponent(entity).map(component -> component.attemptToEquipAccessory(stack, false)).isPresent() ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    @Override
    public Stream<EquippedStack> getEquipped(LivingEntity entity, Identifier slot, @Nullable Predicate<ItemStack> predicate) {
        return getContainer(entity, slot).stream().flatMap(container -> {
            return IntStream.range(0, container.getSize()).mapToObj(container::createReference)
                    .filter(i -> !i.getStack().isEmpty() && (predicate == null || predicate.test(i.getStack()))).map(i -> {
                ItemStack oldStack = i.getStack().copy();
                return new EquippedStack(i.getStack(), container::markChanged, newStack -> {
                    if (i.setStack(newStack)) {
                        container.markChanged();
                    }
                }, l -> {
                    container.markChanged();
                    Channel.SERVER_TRINKET_BROKEN.sendToSurroundingPlayers(new MsgTrinketBroken(oldStack, entity.getId()), entity);
                });
            });
        });
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

    private Optional<AccessoriesContainer> getContainer(LivingEntity entity, Identifier slot) {
        return getTrinketComponent(entity).map(component -> component.getContainers().get(slot.toString()));
    }

    @Override
    public Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, Identifier slotId, int i, int x, int y) {
        return getContainer(entity, slotId).map(container -> new SpellbookTrinketSlot(handler, AccessoriesBasedSlot.of(container.capability().entity(), container.slotType(), i, x, y)));
    }

    @Override
    public boolean isTrinketSlot(Slot slot) {
        return slot instanceof AccessoriesBasedSlot || slot instanceof SpellbookTrinketSlot;
    }
}
