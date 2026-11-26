package com.koashki.blacksmith.datagen;

import com.koashki.blacksmith.core.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ModLootTableProvider {
    private ModLootTableProvider() {}

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)),
                registries
        );
    }

    private static final class ModBlockLoot extends BlockLootSubProvider {
        ModBlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.BLAST_FORGE.get());
            dropSelf(ModBlocks.FORGING_ANVIL.get());
            dropSelf(ModBlocks.SYNTHESIS_CHAMBER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                    ModBlocks.BLAST_FORGE.get(),
                    ModBlocks.FORGING_ANVIL.get(),
                    ModBlocks.SYNTHESIS_CHAMBER.get()
            );
        }
    }
}
