package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgMarkTraitRead;
import com.minelittlepony.unicopia.network.MsgUnlockTraits;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class TraitDiscovery implements NbtSerialisable, Copyable<TraitDiscovery> {
    private final Set<Trait> unreadTraits = new HashSet<>();

    private final Set<Trait> traits = new HashSet<>();
    private final Map<Identifier, SpellTraits> items = new HashMap<>();

    private final Pony pony;

    public TraitDiscovery(Pony pony) {
        this.pony = pony;
    }

    public void clear() {
        unreadTraits.clear();
        items.clear();
        traits.clear();
        pony.setDirty();
    }

    @Environment(EnvType.CLIENT)
    public void markRead(Trait trait) {
        Channel.MARK_TRAIT_READ.sendToServer(new MsgMarkTraitRead(Set.of(trait)));
    }

    public void markRead(Set<Trait> traits) {
        if (unreadTraits.removeAll(traits)) {
            pony.setDirty();
        }
    }

    public void unlock(Item item) {
        if (item == Items.AIR) {
            return;
        }
        SpellTraits traits = SpellTraits.of(item);
        items.put(Registry.ITEM.getId(item), traits);
        Set<Trait> newTraits = new HashSet<>();
        traits.entries().forEach(e -> {
            if (this.traits.add(e.getKey())) {
                newTraits.add(e.getKey());
            }
        });
        unreadTraits.addAll(newTraits);
        pony.setDirty();
        if (!newTraits.isEmpty() && !pony.asWorld().isClient) {
            Channel.UNLOCK_TRAITS.sendToPlayer(new MsgUnlockTraits(newTraits), (ServerPlayerEntity)pony.asEntity());
        }
    }

    public SpellTraits getKnownTraits(Item item) {
        return items.getOrDefault(Registry.ITEM.getId(item), SpellTraits.EMPTY);
    }

    public Stream<Item> getKnownItems(Trait trait) {
        return items.entrySet().stream()
                .filter(entry -> entry.getValue().get(trait) > 0)
                .flatMap(entry -> Registry.ITEM.getOrEmpty(entry.getKey()).stream());
    }

    public boolean isUnread() {
        return !unreadTraits.isEmpty();
    }

    public boolean isUnread(Trait trait) {
        return unreadTraits.contains(trait);
    }

    public boolean isKnown(Trait trait) {
        return traits.contains(trait);
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip) {
        SpellTraits.getEmbeddedTraits(stack)
            .orElseGet(() -> getKnownTraits(stack.getItem()))
            .appendTooltip(tooltip);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        NbtCompound disco = new NbtCompound();
        items.forEach((key, val) -> {
            disco.put(key.toString(), val.toNbt());
        });
        compound.put("items", disco);

        NbtList a = new NbtList();
        this.traits.forEach(id -> a.add(NbtString.of(id.getId().toString())));
        compound.put("traits", a);

        NbtList b = new NbtList();
        unreadTraits.forEach(id -> b.add(NbtString.of(id.getId().toString())));
        compound.put("unreadTraits", b);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        clear();
        NbtCompound disco = compound.getCompound("items");
        disco.getKeys().forEach(key -> {
            Optional.ofNullable(Identifier.tryParse(key)).ifPresent(id -> {
                loadTraits(id, disco.getCompound(key)).filter(SpellTraits::isPresent).ifPresent(val -> {
                    items.put(id, val);
                });
            });
        });
        Trait.fromNbt(compound.getList("traits", NbtElement.STRING_TYPE)).forEach(traits::add);
        Trait.fromNbt(compound.getList("unreadTraits", NbtElement.STRING_TYPE)).forEach(unreadTraits::add);
    }

    private Optional<SpellTraits> loadTraits(Identifier itemId, NbtCompound nbt) {
        if (!pony.isClient()) {
            return Registry.ITEM.getOrEmpty(itemId)
                    .flatMap(item -> Optional.of(SpellTraits.of(item)))
                    .filter(SpellTraits::isPresent)
                    .or(() -> SpellTraits.fromNbt(nbt));
        }

        return SpellTraits.fromNbt(nbt);
    }

    @Override
    public void copyFrom(TraitDiscovery old, boolean alive) {
        clear();
        unreadTraits.addAll(old.unreadTraits);
        traits.addAll(old.traits);
        items.putAll(old.items);
    }
}
