package com.koashki.blacksmith.datagen;

import com.koashki.blacksmith.block.BlastForgeBlock;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BlacksmithMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile forgeOff = models().orientable(
                "blast_forge",
                ResourceLocation.withDefaultNamespace("block/blast_furnace_side"),
                ResourceLocation.withDefaultNamespace("block/blast_furnace_front"),
                ResourceLocation.withDefaultNamespace("block/blast_furnace_top")
        );
        ModelFile forgeOn = models().orientable(
                "blast_forge_on",
                ResourceLocation.withDefaultNamespace("block/blast_furnace_side"),
                ResourceLocation.withDefaultNamespace("block/blast_furnace_front_on"),
                ResourceLocation.withDefaultNamespace("block/blast_furnace_top")
        );
        horizontalBlock(ModBlocks.BLAST_FORGE.get(), state -> state.getValue(BlastForgeBlock.LIT) ? forgeOn : forgeOff);
        simpleBlockItem(ModBlocks.BLAST_FORGE.get(), forgeOff);

        ModelFile anvilModel = models().cubeBottomTop(
                "forging_anvil",
                ResourceLocation.withDefaultNamespace("block/smithing_table_side"),
                ResourceLocation.withDefaultNamespace("block/smithing_table_bottom"),
                ResourceLocation.withDefaultNamespace("block/smithing_table_top")
        );
        horizontalBlock(ModBlocks.FORGING_ANVIL.get(), anvilModel);
        simpleBlockItem(ModBlocks.FORGING_ANVIL.get(), anvilModel);

        ModelFile chamberModel = models().cubeBottomTop(
                "synthesis_chamber",
                ResourceLocation.withDefaultNamespace("block/amethyst_block"),
                ResourceLocation.withDefaultNamespace("block/obsidian"),
                ResourceLocation.withDefaultNamespace("block/enchanting_table_top")
        );
        simpleBlockWithItem(ModBlocks.SYNTHESIS_CHAMBER.get(), chamberModel);
    }
}
