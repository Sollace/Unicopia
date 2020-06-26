package com.minelittlepony.unicopia.world.recipe.enchanting;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.network.Transmittable;

import net.minecraft.util.Identifier;

/**
 * Interface for things that own and can unlock pages.
 *
 */
public interface PageOwner extends Transmittable {

    @Nonnull
    Map<Identifier, PageState> getPageStates();

    default void setPageState(Page page, PageState state) {
        if (state == PageState.LOCKED) {
            getPageStates().remove(page.getName());
        } else {
            getPageStates().put(page.getName(), state);
        }
        sendCapabilities(true);
    }

    default PageState getPageState(Page page) {
        return getPageStates().getOrDefault(page.getName(), page.getDefaultState());
    }

    default boolean hasPageStateRelative(Page page, PageState state, Function<Page, Page> iter) {
        for (Page prev = null;
                (page = iter.apply(page)) != null && page != prev;
                prev = page
        ) {
            if (getPageState(page) == state) {
                return true;
            }
        }
        return false;
    }
}
