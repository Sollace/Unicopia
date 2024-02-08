package com.minelittlepony.unicopia.server.world;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSkyAngle;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;

public class UnicopiaWorldProperties extends PersistentState {

    private final ServerWorld world;

    private Race defaultRace = Race.UNSET;
    private float tangentalSkyAngle;

    private final Set<BlockPos> activeAltarPositions = new HashSet<>();

    public UnicopiaWorldProperties(ServerWorld world) {
        this.world = world;
    }

    public UnicopiaWorldProperties(ServerWorld world, NbtCompound tag) {
        this(world);
        defaultRace = Race.fromName(tag.getString("defaultRace"), Race.HUMAN);
        tangentalSkyAngle = tag.getFloat("tangentalSkyAngle");
        activeAltarPositions.addAll(NbtSerialisable.BLOCK_POS.readAll(tag.getList("activeAltars", NbtElement.COMPOUND_TYPE)).toList());
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
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("defaultRace", Race.REGISTRY.getId(defaultRace).toString());
        tag.putFloat("tangentalSkyAngle", tangentalSkyAngle);
        tag.put("activeAltars", NbtSerialisable.BLOCK_POS.writeAll(activeAltarPositions));
        return tag;
    }

    public static UnicopiaWorldProperties forWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new Type<>(() -> new UnicopiaWorldProperties(world), nbt -> new UnicopiaWorldProperties(world, nbt), DataFixTypes.LEVEL), "unicopia_tribes"
        );
    }
}
