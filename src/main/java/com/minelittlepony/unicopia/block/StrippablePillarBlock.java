package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class StrippablePillarBlock extends PillarBlock implements StrippingLootable {
    @Nullable
    private RegistryKey<LootTable> strippingLootTableKey;

    public StrippablePillarBlock(Settings settings) {
        super(settings);

    }

    @Override
    public final RegistryKey<LootTable> getStrippingLootTableKey() {
        if (strippingLootTableKey == null) {
            strippingLootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, getLootTableKey().getValue().withSuffixedPath("_stripping"));
        }

        return strippingLootTableKey;
    }

    @Override
    public boolean onStripped(ItemUsageContext context) {
        context.getStack().damage(2, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
        if (context.getWorld() instanceof ServerWorld sw) {
            @Nullable
            LootTable table = sw.getServer().getReloadableRegistries().getLootTable(getStrippingLootTableKey());
            if (table != null) {
                table.generateLoot(new LootContextParameterSet.Builder(sw)
                            .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(context.getBlockPos()))
                            .add(LootContextParameters.TOOL, context.getStack())
                            .add(LootContextParameters.BLOCK_STATE, context.getWorld().getBlockState(context.getBlockPos()))
                            .addOptional(LootContextParameters.BLOCK_ENTITY, context.getWorld().getBlockEntity(context.getBlockPos()))
                        .build(LootContextTypes.BLOCK))
                    .forEach(stack -> Block.dropStack(sw, context.getBlockPos(), stack));
            }
        }

        return true;
    }

}
