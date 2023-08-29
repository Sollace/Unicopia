package com.minelittlepony.unicopia.server.world;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.FloatingArtefactEntity;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public record Altar(BlockPos origin, Set<BlockPos> pillars) {
    private static final Direction[] HORIZONTALS = { Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST };
    private static final Predicate<Entity> IS_PARTICIPANT = EntityPredicates.VALID_ENTITY.and(e -> e instanceof FloatingArtefactEntity || e instanceof SpellbookEntity);
    public static final NbtSerialisable.Serializer<Altar> SERIALIZER = NbtSerialisable.Serializer.of(nbt -> {
        return new Altar(
            NbtSerialisable.BLOCK_POS.read(nbt.getCompound("origin")),
            new HashSet<>(NbtSerialisable.BLOCK_POS.readAll(nbt.getList("pillars", NbtElement.COMPOUND_TYPE)).toList())
        );
    }, altar -> {
        NbtCompound compound = new NbtCompound();
        compound.put("origin", NbtSerialisable.BLOCK_POS.write(altar.origin));
        compound.put("pillars", NbtSerialisable.BLOCK_POS.writeAll(altar.pillars));
        return compound;
    });

    private static final int INNER_RADIUS = 4;
    private static final int PILLAR_OFFSET_FROM_CENTER = 2;

    private static final BlockPos PILLAR_A = new BlockPos(INNER_RADIUS, 0,  PILLAR_OFFSET_FROM_CENTER);
    private static final BlockPos PILLAR_B = new BlockPos(INNER_RADIUS, 0, -PILLAR_OFFSET_FROM_CENTER);

    private static final List<BlockPos> PILLAR_OFFSETS = Arrays.stream(BlockRotation.values()).flatMap(rotation -> {
        return Stream.of(PILLAR_A.rotate(rotation), PILLAR_B.rotate(rotation));
    }).toList();

    public static Optional<Altar> locateAltar(World world, BlockPos startingPoint) {

        BlockPos.Mutable mutable = startingPoint.mutableCopy();

        if (!world.isSkyVisible(mutable)) {
            return Optional.empty();
        }

        mutable.move(Direction.DOWN);
        if (!world.getBlockState(mutable).isOf(Blocks.LODESTONE)) {
            return Optional.empty();
        }

        for (int i = 0; i < 4; i++) {
            mutable.set(startingPoint);
            mutable.move(Direction.DOWN);
            mutable.move(Direction.fromHorizontal(i), 2);

            if (world.getBlockState(mutable).isOf(Blocks.SOUL_SAND)) {
                if (checkSlab(world, mutable)) {
                    mutable.move(Direction.UP);
                    BlockState fireState = world.getBlockState(mutable);
                    if (!(fireState.isAir() || fireState.isIn(BlockTags.FIRE))) {
                        return Optional.empty();
                    }
                    BlockPos firePos = mutable.toImmutable();
                    mutable.move(Direction.DOWN);

                    final Set<BlockPos> pillars = new HashSet<>();

                    if (checkPillarPair(world, mutable, BlockRotation.NONE, pillars::add)
                      && checkPillarPair(world, mutable, BlockRotation.CLOCKWISE_90, pillars::add)
                      && checkPillarPair(world, mutable, BlockRotation.COUNTERCLOCKWISE_90, pillars::add)
                      && checkPillarPair(world, mutable, BlockRotation.CLOCKWISE_180, pillars::add)) {

                        return Optional.of(new Altar(firePos, pillars));
                    }

                }
            }
        }

        return Optional.empty();
    }

    public static Altar of(BlockPos center) {
        return new Altar(center, new HashSet<>(PILLAR_OFFSETS.stream().map(p -> p.add(center).up()).toList()));
    }

    private static boolean checkSlab(World world, BlockPos pos) {
        return !PosHelper.any(pos, p -> !isObsidian(world, p), HORIZONTALS);
    }

    private static boolean checkPillarPair(World world, BlockPos.Mutable center, BlockRotation rotation, Consumer<BlockPos> pillarPosCollector) {
        return checkPillar(world, center, PILLAR_A.rotate(rotation), pillarPosCollector)
            && checkPillar(world, center, PILLAR_B.rotate(rotation), pillarPosCollector);
    }

    private static boolean checkPillar(World world, BlockPos.Mutable pos, Vec3i pillarPos, Consumer<BlockPos> pillarPosCollector) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (isObsidian(world, pos.move(pillarPos))
            && isObsidian(world, pos.move(Direction.UP))
            && isObsidian(world, pos.move(Direction.UP))) {
            pillarPosCollector.accept(pos.toImmutable());
            pos.set(x, y, z);
            return true;
        }

        return false;
    }

    private static boolean isObsidian(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.CRYING_OBSIDIAN);
    }

    private static boolean checkState(World world, BlockPos pos, Block block) {
        return world.getBlockState(pos).isOf(block);
    }

    public void generateDecorations(World world) {
        world.setBlockState(origin, Blocks.SOUL_FIRE.getDefaultState(), Block.FORCE_STATE | Block.NOTIFY_ALL);
        pillars.forEach(pillar -> {
            FloatingArtefactEntity artefact = UEntities.FLOATING_ARTEFACT.create(world);
            artefact.setStack(UItems.ALICORN_BADGE.getDefaultStack());
            artefact.setPosition(pillar.up().toCenterPos());
            artefact.setInvulnerable(true);
            artefact.setAltar(this);
            removeExisting(null, world, pillar);
            world.spawnEntity(artefact);
        });
    }

    public void tearDown(@Nullable Entity except, World world) {
        if (!(except instanceof SpellbookEntity)) {
            world.getOtherEntities(except, new Box(origin).expand(3), IS_PARTICIPANT).forEach(Entity::kill);
        }
        pillars.forEach(pillar -> removeExisting(except, world, pillar));
    }

    private void removeExisting(@Nullable Entity except, World world, BlockPos pillar) {
        world.getOtherEntities(except, new Box(pillar.up()), IS_PARTICIPANT).forEach(Entity::kill);
    }

    public boolean isValid(World world) {
        return checkState(world, origin, Blocks.SOUL_FIRE)
                && checkState(world, origin.down(), Blocks.SOUL_SAND)
                && checkSlab(world, origin.down())
                && pillars.stream().allMatch(pillar -> isObsidian(world, pillar) && isObsidian(world, pillar.down()) && isObsidian(world, pillar.down(2)));
    }
}
