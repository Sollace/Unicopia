package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.datasync.Synchronizable;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SpellbookState extends Synchronizable<SpellbookState> implements NbtSerialisable {
    // TODO: SpellbookState needs a packet codec
    public static final PacketCodec<RegistryByteBuf, SpellbookState> PACKET_CODEC = null;

    public static final Identifier CRAFTING_ID = Unicopia.id("crafting");
    public static final Identifier PROFILE_ID = Unicopia.id("profile");
    public static final Identifier TRAIT_DEX_ID = Unicopia.id("traits");

    private Optional<Identifier> currentPageId = Optional.empty();

    private boolean dirty;

    private final Map<Identifier, PageState> states = new HashMap<>();

    public boolean isDirty() {
        boolean isDirty = dirty;
        dirty = false;
        return isDirty;
    }

    public Optional<Identifier> getCurrentPageId() {
        return currentPageId;
    }

    public void setCurrentPageId(Identifier pageId) {
        currentPageId = Optional.ofNullable(pageId);
        synchronize();
    }

    public PageState getState(Identifier pageId) {
        return states.computeIfAbsent(pageId, i -> new PageState(page -> synchronize()));
    }

    @Override
    public void copyFrom(SpellbookState state) {
        currentPageId = state.currentPageId;
        state.states.forEach((id, page) -> getState(id).copyFrom(page));
        dirty = true;
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeOptional(currentPageId, PacketByteBuf::writeIdentifier);
        buf.writeMap(states, PacketByteBuf::writeIdentifier, (b, v) -> v.toPacket(b));
    }

    public SpellbookState fromPacket(PacketByteBuf buf) {
        currentPageId = buf.readOptional(PacketByteBuf::readIdentifier);
        buf.readMap(PacketByteBuf::readIdentifier, b -> new PageState(page -> synchronize()).fromPacket(b)).forEach((id, state) -> {
            getState(id).copyFrom(state);
        });
        return this;
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        currentPageId.ifPresent(id -> compound.putString("current_page", id.toString()));
        NbtCompound states = new NbtCompound();
        compound.put("states", states);
        this.states.forEach((id, page) -> {
            states.put(id.toString(), page.toNBT(lookup));
        });
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        currentPageId = compound.contains("current_page", NbtElement.STRING_TYPE) ? Optional.ofNullable(Identifier.tryParse(compound.getString("current_page"))) : Optional.empty();
        NbtCompound states = compound.getCompound("states");
        states.getKeys().stream().forEach(key -> {
            Identifier id = Identifier.tryParse(key);
            if (id != null) {
                getState(id).fromNBT(states.getCompound(key), lookup);
            }
        });
    }

    public static class PageState extends Synchronizable<PageState> implements NbtSerialisable {
        private int offset;

        public PageState() {}

        PageState(Consumer<PageState> synchronizer) {
            setSynchronizer(synchronizer);
        }

        @Override
        public void copyFrom(PageState other) {
            offset = other.offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
            synchronize();
        }

        public int getOffset() {
            return offset;
        }

        public void swap(int incr, int max) {
            setOffset(MathHelper.clamp(getOffset() + incr, 0, max - 1));
        }

        public void toPacket(PacketByteBuf buf) {
            buf.writeInt(offset);
        }

        public PageState fromPacket(PacketByteBuf buf) {
            offset = buf.readInt();
            return this;
        }

        @Override
        public void toNBT(NbtCompound compound, WrapperLookup lookup) {
            compound.putInt("offset", offset);
        }

        @Override
        public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
            offset = compound.getInt("offset");
        }
    }
}
