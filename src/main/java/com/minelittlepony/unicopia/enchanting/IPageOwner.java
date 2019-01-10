package com.minelittlepony.unicopia.enchanting;

import java.util.List;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.network.ITransmittable;

public interface IPageOwner extends ITransmittable {

    @Nonnull
    List<Integer> getUnlockedPages();

    default boolean hasPageUnlock(int pageIndex) {
        return getUnlockedPages().contains(pageIndex);
    }

    default boolean unlockPage(int pageIndex) {
        if (!hasPageUnlock(pageIndex)) {
            if (getUnlockedPages().add(pageIndex)) {
                sendCapabilities(true);

                return true;
            }
        }
        return false;
    }

    default boolean hasUnlockedPages() {
        return getUnlockedPages().size() > 0;
    }
}
