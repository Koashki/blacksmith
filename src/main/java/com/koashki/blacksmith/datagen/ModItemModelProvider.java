package com.koashki.blacksmith.datagen;

import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BlacksmithMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        handheldVanilla("stone_forging_hammer", "item/stone_axe");
        handheldVanilla("iron_forging_hammer", "item/iron_axe");
        handheldVanilla("gold_forging_hammer", "item/golden_axe");

        generatedVanilla("molten_alloy", "item/iron_ingot");
        generatedVanilla("refining_dust", "item/glowstone_dust");
        generatedVanilla("aspect_essence", "item/amethyst_shard");
        generatedVanilla("smiths_ledger", "item/writable_book");
        generatedVanilla("torn_forge_note", "item/paper");

        handheldVanilla("forged_sword", "item/iron_sword");
        handheldVanilla("forged_greatsword", "item/iron_sword");
        handheldVanilla("forged_dagger", "item/iron_sword");
        handheldVanilla("forged_spear", "item/iron_shovel");
        handheldVanilla("forged_war_hammer", "item/iron_axe");
    }

    private void handheldVanilla(String name, String vanillaTexturePath) {
        withExistingParent(name, mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.withDefaultNamespace(vanillaTexturePath));
    }

    private void generatedVanilla(String name, String vanillaTexturePath) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.withDefaultNamespace(vanillaTexturePath));
    }
}
