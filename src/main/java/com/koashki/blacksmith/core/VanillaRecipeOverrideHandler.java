package com.koashki.blacksmith.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
public final class VanillaRecipeOverrideHandler {
    private static final Set<ResourceLocation> REPLACED_METAL_RECIPES = Set.of(
            ResourceLocation.withDefaultNamespace("iron_sword"),
            ResourceLocation.withDefaultNamespace("iron_pickaxe"),
            ResourceLocation.withDefaultNamespace("iron_axe"),
            ResourceLocation.withDefaultNamespace("iron_shovel"),
            ResourceLocation.withDefaultNamespace("iron_hoe"),
            ResourceLocation.withDefaultNamespace("golden_sword"),
            ResourceLocation.withDefaultNamespace("golden_pickaxe"),
            ResourceLocation.withDefaultNamespace("golden_axe"),
            ResourceLocation.withDefaultNamespace("golden_shovel"),
            ResourceLocation.withDefaultNamespace("golden_hoe"),
            ResourceLocation.withDefaultNamespace("shield")
    );

    private VanillaRecipeOverrideHandler() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!ModConfigs.SERVER.replaceVanillaMetalRecipes.get()) {
            return;
        }
        RecipeManager manager = event.getServer().getRecipeManager();
        List<RecipeHolder<?>> filtered = manager.getRecipes().stream()
                .filter(holder -> !REPLACED_METAL_RECIPES.contains(holder.id()))
                .toList();
        int removed = manager.getRecipes().size() - filtered.size();
        if (removed > 0) {
            manager.replaceRecipes(filtered);
            BlacksmithMod.LOGGER.info("Replaced {} vanilla Iron/Gold tool, weapon, and shield crafting table recipes in favor of the Forging Anvil.", removed);
        }
    }
}
