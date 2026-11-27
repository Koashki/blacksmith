package com.koashki.blacksmith.datagen;

import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModBlocks;
import com.koashki.blacksmith.core.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, BlacksmithMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.blacksmith", "Blacksmith");

        // Workstations
        addBlock(ModBlocks.BLAST_FORGE, "Blast Forge");
        addBlock(ModBlocks.FORGING_ANVIL, "Forging Anvil");
        addBlock(ModBlocks.SYNTHESIS_CHAMBER, "Synthesis Chamber");

        // Items
        addItem(ModItems.STONE_FORGING_HAMMER, "Stone Forging Hammer");
        addItem(ModItems.IRON_FORGING_HAMMER, "Iron Forging Hammer");
        addItem(ModItems.GOLD_FORGING_HAMMER, "Gold Forging Hammer");
        addItem(ModItems.MOLTEN_ALLOY, "Molten Alloy Billet");
        addItem(ModItems.REFINING_DUST, "Refining Dust");
        addItem(ModItems.ASPECT_ESSENCE, "Aspect Essence");
        addItem(ModItems.SMITHS_LEDGER, "Smith's Ledger");
        addItem(ModItems.TORN_FORGE_NOTE, "Torn Forge Note");
        addItem(ModItems.FORGED_SWORD, "Forged Sword");
        addItem(ModItems.FORGED_GREATSWORD, "Forged Greatsword");
        addItem(ModItems.FORGED_DAGGER, "Forged Dagger");
        addItem(ModItems.FORGED_SPEAR, "Forged Spear");
        addItem(ModItems.FORGED_WAR_HAMMER, "Forged War Hammer");

        // Grade Tiers
        add("grade.blacksmith.normal", "Normal");
        add("grade.blacksmith.rare", "Rare");
        add("grade.blacksmith.epic", "Epic");
        add("grade.blacksmith.unique", "Unique");
        add("grade.blacksmith.legendary", "Legendary");
        add("grade.blacksmith.mythic", "Mythic");
    }
}
