package com.minelittlepony.unicopia.item;

import java.util.List;

import com.minelittlepony.unicopia.entity.IItemEntity;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;

public interface TickableItem {
    static <T extends Item> T registerTickCallback(T item, GroundTickCallback callback) {
        ((ItemDuck)item).addGroundTickCallback(callback);
        return item;
    }

    default List<GroundTickCallback> getCallbacks() {
        throw new RuntimeException("Implemented by mixin");
    }

    default void addGroundTickCallback(GroundTickCallback callback) {
        getCallbacks().add(callback);
    }

    default ActionResult onGroundTick(IItemEntity entity) {
        for (var callback : getCallbacks()) {
            ActionResult result = callback.onGroundTick(entity);
            if (result.isAccepted()) {
                return result;
            }
        }
        return ActionResult.PASS;
    }

    default void inFrameTick(ItemFrameEntity entity) {

    }

    public interface GroundTickCallback {
        ActionResult onGroundTick(IItemEntity entity);
    }
}
