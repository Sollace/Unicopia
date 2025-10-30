package com.minelittlepony.unicopia.block;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class StrippablePillarBlock extends PillarBlock implements StrippingLootable {
    private Optional<RegistryKey<LootTable>> strippingLootTableKey;

    public StrippablePillarBlock(Settings settings) {
        super(settings);
        strippingLootTableKey = getLootTableKey().map(key ->RegistryKey.of(RegistryKeys.LOOT_TABLE, key.getValue().withSuffixedPath("_stripping")));
    }

    @Override
    public final Optional<RegistryKey<LootTable>> getStrippingLootTableKey() {
        return strippingLootTableKey;
    }

    @Override
    public boolean onStripped(ItemUsageContext context) {
        context.getStack().damage(2, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
        if (context.getWorld() instanceof ServerWorld sw) {
            getStrippingLootTableKey().map(sw.getServer().getReloadableRegistries()::getLootTable).ifPresent(table -> {
                table.generateLoot(new LootWorldContext.Builder(sw)
                            .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(context.getBlockPos()))
                            .add(LootContextParameters.TOOL, context.getStack())
                            .add(LootContextParameters.BLOCK_STATE, context.getWorld().getBlockState(context.getBlockPos()))
                            .addOptional(LootContextParameters.BLOCK_ENTITY, context.getWorld().getBlockEntity(context.getBlockPos()))
                        .build(LootContextTypes.BLOCK))
                    .forEach(stack -> Block.dropStack(sw, context.getBlockPos(), stack));
            });
        }

        return true;
    }

}
