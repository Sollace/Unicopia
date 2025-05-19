package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.datasync.Synchronizable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SpellbookState extends Synchronizable<SpellbookState> {
    public static final Codec<SpellbookState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("current_page").forGetter(i -> i.currentPageId),
            Codec.unboundedMap(Identifier.CODEC, PageState.CODEC).fieldOf("states").forGetter(i -> i.states)
    ).apply(instance, SpellbookState::new));
    public static final PacketCodec<RegistryByteBuf, SpellbookState> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(Identifier.PACKET_CODEC), SpellbookState::getCurrentPageId,
            PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PageState.PACKET_CODEC), p -> p.states,
            SpellbookState::new
    );

    public static final Identifier CRAFTING_ID = Unicopia.id("crafting");
    public static final Identifier PROFILE_ID = Unicopia.id("profile");
    public static final Identifier TRAIT_DEX_ID = Unicopia.id("traits");

    private Optional<Identifier> currentPageId = Optional.empty();

    private boolean dirty;

    private final Map<Identifier, PageState> states = new HashMap<>();

    public SpellbookState() {}

    private SpellbookState(Optional<Identifier> currentPageId, Map<Identifier, PageState> states) {
        this.currentPageId = currentPageId;
        states.forEach((id, state) -> {
            getState(id).offset = state.offset;
        });
    }

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

    public SpellbookState createCopy() {
        SpellbookState copy = new SpellbookState();
        copy.currentPageId = currentPageId;
        states.forEach((id, state) -> {
            copy.getState(id).offset = state.offset;
        });
        return copy;
    }

    public static class PageState extends Synchronizable<PageState> {
        public static final Codec<PageState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("offset").forGetter(PageState::getOffset)
        ).apply(instance, PageState::new));
        public static final PacketCodec<ByteBuf, PageState> PACKET_CODEC = PacketCodecs.INTEGER.xmap(PageState::new, PageState::getOffset);

        private int offset;

        public PageState() {}

        PageState(Consumer<PageState> synchronizer) {
            setSynchronizer(synchronizer);
        }

        public PageState(int offset) {
            this.offset = offset;
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
    }
}
