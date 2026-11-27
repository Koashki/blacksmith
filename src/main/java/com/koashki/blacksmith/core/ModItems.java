package com.koashki.blacksmith.core;

import com.koashki.blacksmith.item.AspectEssenceItem;
import com.koashki.blacksmith.item.ForgedArchetype;
import com.koashki.blacksmith.item.ForgedWeaponItem;
import com.koashki.blacksmith.item.ForgingHammerItem;
import com.koashki.blacksmith.item.MoltenAlloyItem;
import com.koashki.blacksmith.item.RefiningDustItem;
import com.koashki.blacksmith.item.SmithsLedgerItem;
import com.koashki.blacksmith.item.TornForgeNoteItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BlacksmithMod.MOD_ID);

    // Forging Hammers (Starter Stone + Iron & Gold upgrades)
    public static final DeferredItem<ForgingHammerItem> STONE_FORGING_HAMMER = ITEMS.register("stone_forging_hammer",
            () -> new ForgingHammerItem(96, 0.00f, new Item.Properties()));
    public static final DeferredItem<ForgingHammerItem> IRON_FORGING_HAMMER = ITEMS.register("iron_forging_hammer",
            () -> new ForgingHammerItem(280, 0.04f, new Item.Properties()));
    public static final DeferredItem<ForgingHammerItem> GOLD_FORGING_HAMMER = ITEMS.register("gold_forging_hammer",
            () -> new ForgingHammerItem(120, 0.07f, new Item.Properties()));

    // Intermediate & Synthesis Materials
    public static final DeferredItem<MoltenAlloyItem> MOLTEN_ALLOY = ITEMS.register("molten_alloy",
            () -> new MoltenAlloyItem(new Item.Properties()));
    public static final DeferredItem<RefiningDustItem> REFINING_DUST = ITEMS.register("refining_dust",
            () -> new RefiningDustItem(new Item.Properties()));
    public static final DeferredItem<AspectEssenceItem> ASPECT_ESSENCE = ITEMS.register("aspect_essence",
            () -> new AspectEssenceItem(new Item.Properties()));

    // Discovery & Lore Items
    public static final DeferredItem<SmithsLedgerItem> SMITHS_LEDGER = ITEMS.register("smiths_ledger",
            () -> new SmithsLedgerItem(new Item.Properties()));
    public static final DeferredItem<TornForgeNoteItem> TORN_FORGE_NOTE = ITEMS.register("torn_forge_note",
            () -> new TornForgeNoteItem(new Item.Properties()));

    // Modular Forged Weapon Archetypes
    public static final DeferredItem<ForgedWeaponItem> FORGED_SWORD = ITEMS.register("forged_sword",
            () -> new ForgedWeaponItem(ForgedArchetype.SWORD, new Item.Properties()));
    public static final DeferredItem<ForgedWeaponItem> FORGED_GREATSWORD = ITEMS.register("forged_greatsword",
            () -> new ForgedWeaponItem(ForgedArchetype.GREATSWORD, new Item.Properties()));
    public static final DeferredItem<ForgedWeaponItem> FORGED_DAGGER = ITEMS.register("forged_dagger",
            () -> new ForgedWeaponItem(ForgedArchetype.DAGGER, new Item.Properties()));
    public static final DeferredItem<ForgedWeaponItem> FORGED_SPEAR = ITEMS.register("forged_spear",
            () -> new ForgedWeaponItem(ForgedArchetype.SPEAR, new Item.Properties()));
    public static final DeferredItem<ForgedWeaponItem> FORGED_WAR_HAMMER = ITEMS.register("forged_war_hammer",
            () -> new ForgedWeaponItem(ForgedArchetype.WAR_HAMMER, new Item.Properties()));

    private ModItems() {}

    public static ForgedWeaponItem weaponForArchetype(ForgedArchetype archetype) {
        return switch (archetype) {
            case SWORD -> FORGED_SWORD.get();
            case GREATSWORD -> FORGED_GREATSWORD.get();
            case DAGGER -> FORGED_DAGGER.get();
            case SPEAR -> FORGED_SPEAR.get();
            case WAR_HAMMER -> FORGED_WAR_HAMMER.get();
        };
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
