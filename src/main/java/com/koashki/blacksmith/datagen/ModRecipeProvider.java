package com.koashki.blacksmith.datagen;

import com.koashki.blacksmith.core.ModBlocks;
import com.koashki.blacksmith.core.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Starter Stone Forging Hammer (available before iron!)
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STONE_FORGING_HAMMER.get())
                .pattern("CCC")
                .pattern("CSC")
                .pattern(" S ")
                .define('C', Items.COBBLESTONE)
                .define('S', Items.STICK)
                .unlockedBy("has_cobblestone", has(Items.COBBLESTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_FORGING_HAMMER.get())
                .pattern("III")
                .pattern("ISI")
                .pattern(" S ")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.GOLD_FORGING_HAMMER.get())
                .pattern("GGG")
                .pattern("GSG")
                .pattern(" S ")
                .define('G', Items.GOLD_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        // Affordable Early-Game Blast Forge (Furnace + Smooth Stone + 3 Iron)
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.BLAST_FORGE.get())
                .pattern("III")
                .pattern("SFS")
                .pattern("SSS")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.SMOOTH_STONE)
                .define('F', Items.FURNACE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        // Affordable Early-Game Forging Anvil (Smooth Stone + 7 Iron)
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FORGING_ANVIL.get())
                .pattern("III")
                .pattern(" I ")
                .pattern("ISI")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.SMOOTH_STONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        // Synthesis Chamber (Amethyst + Gold + Obsidian + Redstone)
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SYNTHESIS_CHAMBER.get())
                .pattern("AGA")
                .pattern("ORO")
                .pattern("OOO")
                .define('A', Items.AMETHYST_SHARD)
                .define('G', Items.GOLD_INGOT)
                .define('O', Items.OBSIDIAN)
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        // Smith's Ledger (Book + Iron Nugget)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SMITHS_LEDGER.get())
                .requires(Items.BOOK)
                .requires(Items.IRON_NUGGET)
                .unlockedBy("has_book", has(Items.BOOK))
                .save(output);
    }
}
