package com.koashki.blacksmith.datagen;

import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.loot.AddForgeNoteLootModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BlacksmithMod.MOD_ID);
    }

    @Override
    protected void start() {
        add("forge_note_dungeon", new AddForgeNoteLootModifier(
                new LootItemCondition[]{LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/simple_dungeon")).build()},
                0.25f
        ));
        add("forge_note_mineshaft", new AddForgeNoteLootModifier(
                new LootItemCondition[]{LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")).build()},
                0.20f
        ));
        add("forge_note_bastion", new AddForgeNoteLootModifier(
                new LootItemCondition[]{LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build()},
                0.45f
        ));
    }
}
