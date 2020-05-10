package com.minelittlepony.unicopia.entity.player;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.enchanting.PageOwner;
import com.minelittlepony.unicopia.enchanting.PageState;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public class PlayerPageStats implements NbtSerialisable, PageOwner {
    private final Map<Identifier, PageState> pageStates = new HashMap<>();

    private final Pony pony;

    PlayerPageStats(Pony pony) {
        this.pony = pony;
    }

    @Override
    public Map<Identifier, PageState> getPageStates() {
        return pageStates;
    }

    @Override
    public void sendCapabilities(boolean full) {
        pony.sendCapabilities(full);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        if (!pageStates.isEmpty()) {
            CompoundTag pages = new CompoundTag();
            boolean written = false;

            for (Map.Entry<Identifier, PageState> entry : pageStates.entrySet()) {
                if (entry.getValue() != PageState.LOCKED) {
                    pages.putString(entry.getKey().toString(), entry.getValue().name());
                    written = true;
                }
            }

            if (written) {
                compound.put("pageStates", pages);
            }
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        pageStates.clear();
        if (compound.contains("pageStates")) {
            CompoundTag pages = compound.getCompound("pageStates");

            pages.getKeys().forEach(key -> {
                PageState state = PageState.of(pages.getString(key));

                if (state != PageState.LOCKED) {
                    pageStates.put(new Identifier(key), state);
                }
            });
        }
    }

}
