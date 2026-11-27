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
        handheldSingle("stone_forging_hammer");
        handheldSingle("iron_forging_hammer");
        handheldSingle("gold_forging_hammer");

        generatedTwoLayer("molten_alloy");
        generatedSingle("refining_dust");
        generatedTwoLayer("aspect_essence");
        generatedSingle("smiths_ledger");
        generatedSingle("torn_forge_note");

        handheldThreeLayerWeapon("forged_sword");
        handheldThreeLayerWeapon("forged_greatsword");
        handheldThreeLayerWeapon("forged_dagger");
        handheldThreeLayerWeapon("forged_spear");
        handheldThreeLayerWeapon("forged_war_hammer");
    }

    private void handheldSingle(String name) {
        withExistingParent(name, mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + name));
    }

    private void generatedSingle(String name) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + name));
    }

    private void generatedTwoLayer(String name) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + name + "_base"))
                .texture("layer1", modLoc("item/" + name + "_accent"));
    }

    private void handheldThreeLayerWeapon(String name) {
        withExistingParent(name, mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + name + "_base"))
                .texture("layer1", modLoc("item/" + name + "_accent"))
                .texture("layer2", modLoc("item/" + name + "_handle"));
    }
}
