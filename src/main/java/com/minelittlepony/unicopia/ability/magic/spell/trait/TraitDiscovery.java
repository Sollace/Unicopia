package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.Pony;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class TraitDiscovery implements NbtSerialisable {
    private final Set<Identifier> unreadTraits = new HashSet<>();

    private final Set<Identifier> traits = new HashSet<>();
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

    public void markRead() {
        unreadTraits.clear();
        pony.setDirty();
    }

    public void unlock(Item item) {
        if (item == Items.AIR) {
            return;
        }
        SpellTraits traits = SpellTraits.of(item);
        items.put(Registry.ITEM.getId(item), traits);
        traits.entries().forEach(e -> {
            if (this.traits.add(e.getKey().getId())) {
                unreadTraits.add(e.getKey().getId());
            }
        });
        pony.setDirty();
    }

    public SpellTraits getKnownTraits(Item item) {
        return items.getOrDefault(Registry.ITEM.getId(item), SpellTraits.EMPTY);
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip) {
        getKnownTraits(stack.getItem()).appendTooltip(tooltip);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        NbtCompound disco = new NbtCompound();
        items.forEach((key, val) -> {
            disco.put(key.toString(), val.toNbt());
        });
        compound.put("items", disco);

        NbtList a = new NbtList();
        this.traits.forEach(id -> a.add(NbtString.of(id.toString())));
        compound.put("traits", a);

        NbtList b = new NbtList();
        unreadTraits.forEach(id -> b.add(NbtString.of(id.toString())));
        compound.put("unreadTraits", b);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        clear();
        NbtCompound disco = compound.getCompound("items");
        disco.getKeys().forEach(key -> {
            Optional.ofNullable(Identifier.tryParse(key)).ifPresent(id -> {
                SpellTraits.fromNbt(disco.getCompound(key)).ifPresent(val -> {
                    items.put(id, val);
                });
            });
        });
        compound.getList("traits", NbtElement.STRING_TYPE).forEach(el -> {
            Optional.ofNullable(Identifier.tryParse(el.asString())).ifPresent(this.traits::add);
        });
        compound.getList("unreadTraits", NbtElement.STRING_TYPE).forEach(el -> {
            Optional.ofNullable(Identifier.tryParse(el.asString())).ifPresent(this.unreadTraits::add);
        });
    }

    public void copyFrom(TraitDiscovery old) {
        clear();
        unreadTraits.addAll(old.unreadTraits);
        traits.addAll(old.traits);
        items.putAll(old.items);
    }
}
