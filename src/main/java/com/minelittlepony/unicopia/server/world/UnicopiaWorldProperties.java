package com.minelittlepony.unicopia.server.world;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSkyAngle;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.serialization.Codec;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;

public class UnicopiaWorldProperties extends PersistentState {
    static final Codec<Set<BlockPos>> POS_CODEC = CodecUtils.setOf(BlockPos.CODEC);

    private final ServerWorld world;

    private Race defaultRace = Race.UNSET;
    private float tangentalSkyAngle;

    private final Set<BlockPos> activeAltarPositions = new HashSet<>();

    public static UnicopiaWorldProperties forWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new Type<>(() -> new UnicopiaWorldProperties(world), (nbt, lookup) -> new UnicopiaWorldProperties(world, nbt), DataFixTypes.LEVEL), "unicopia_tribes"
        );
    }

    private UnicopiaWorldProperties(ServerWorld world) {
        this.world = world;
    }

    private UnicopiaWorldProperties(ServerWorld world, NbtCompound tag) {
        this(world);
        defaultRace = Race.fromName(tag.getString("defaultRace"), Race.HUMAN);
        tangentalSkyAngle = tag.getFloat("tangentalSkyAngle");
        NbtSerialisable.decode(POS_CODEC, tag.getList("activeAltars", NbtElement.COMPOUND_TYPE)).ifPresent(activeAltarPositions::addAll);
    }

    public Race getDefaultRace() {
        return defaultRace;
    }

    public Race setDefaultRace(Race race) {
        defaultRace = race;
        markDirty();
        return defaultRace;
    }

    public float getTangentalSkyAngle() {
        return tangentalSkyAngle;
    }

    public void setTangentalSkyAngle(float angle) {
        tangentalSkyAngle = MathHelper.wrapDegrees(angle);
        markDirty();
        Channel.SERVER_SKY_ANGLE.sendToAllPlayers(new MsgSkyAngle(tangentalSkyAngle), world);
    }

    public void removeAltar(BlockPos center) {
        activeAltarPositions.remove(center);
        markDirty();
    }

    public void addAltar(BlockPos center) {
        activeAltarPositions.add(center);
        markDirty();
    }

    public boolean isActiveAltar(BlockPos center) {
        return activeAltarPositions.contains(center);
    }

    public boolean isActiveAltar(Entity entity) {
        for (int i = 0; i < entity.getHeight(); i++) {
            if (isActiveAltar(entity.getBlockPos().up(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag, WrapperLookup lookup) {
        tag.putString("defaultRace", Race.REGISTRY.getId(defaultRace).toString());
        tag.putFloat("tangentalSkyAngle", tangentalSkyAngle);
        tag.put("activeAltars", NbtSerialisable.encode(POS_CODEC, activeAltarPositions));
        return tag;
    }
}
