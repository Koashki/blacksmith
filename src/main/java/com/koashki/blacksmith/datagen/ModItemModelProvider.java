package com.koashki.blacksmith.datagen;

import com.koashki.blacksmith.core.BlacksmithMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BlacksmithMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Forging Hammers
        handheldSingle("stone_forging_hammer", "hammer/stone_forging_hammer");
        handheldSingle("iron_forging_hammer", "hammer/iron_forging_hammer");
        handheldSingle("gold_forging_hammer", "hammer/gold_forging_hammer");

        // Materials & Catalysts
        generatedTwoLayer("molten_alloy", "material/molten_alloy");
        generatedSingle("refining_dust", "material/refining_dust");
        generatedTwoLayer("aspect_essence", "material/aspect_essence");

        // Discovery & Lore
        generatedSingle("smiths_ledger", "lore/smiths_ledger");
        generatedSingle("torn_forge_note", "lore/torn_forge_note");

        // Modular Forged Weapons
        handheldThreeLayer("forged_sword", "weapon/forged_sword");
        handheldThreeLayer("forged_greatsword", "weapon/forged_greatsword");
        handheldThreeLayer("forged_dagger", "weapon/forged_dagger");
        handheldThreeLayer("forged_spear", "weapon/forged_spear");
        handheldThreeLayer("forged_war_hammer", "weapon/forged_war_hammer");

        // Modular Forged Survival Tools & Shield
        handheldThreeLayer("forged_pickaxe", "tool/forged_pickaxe");
        handheldThreeLayer("forged_axe", "tool/forged_axe");
        handheldThreeLayer("forged_shovel", "tool/forged_shovel");
        handheldThreeLayer("forged_hoe", "tool/forged_hoe");
        generatedThreeLayer("forged_shield", "tool/forged_shield");
    }

    private void handheldSingle(String itemName, String texturePath) {
        withExistingParent(itemName, mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + texturePath));
    }

    private void generatedSingle(String itemName, String texturePath) {
        withExistingParent(itemName, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + texturePath));
    }

    private void generatedTwoLayer(String itemName, String texturePrefix) {
        withExistingParent(itemName, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + texturePrefix + "_base"))
                .texture("layer1", modLoc("item/" + texturePrefix + "_accent"));
    }

    private void handheldThreeLayer(String itemName, String texturePrefix) {
        withExistingParent(itemName, mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + texturePrefix + "_base"))
                .texture("layer1", modLoc("item/" + texturePrefix + "_accent"))
                .texture("layer2", modLoc("item/" + texturePrefix + "_handle"));
    }

    private void generatedThreeLayer(String itemName, String texturePrefix) {
        withExistingParent(itemName, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + texturePrefix + "_base"))
                .texture("layer1", modLoc("item/" + texturePrefix + "_accent"))
                .texture("layer2", modLoc("item/" + texturePrefix + "_handle"));
    }
}
