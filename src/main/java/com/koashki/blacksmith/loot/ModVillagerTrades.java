package com.koashki.blacksmith.loot;

import com.koashki.blacksmith.aspect.ForgeNoteLibrary;
import com.koashki.blacksmith.component.ForgeNoteData;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.core.ModItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
public final class ModVillagerTrades {
    private ModVillagerTrades() {}

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.WEAPONSMITH) {
            return;
        }
        // Apprentice Weaponsmith (Level 2) sells a random Torn Forge Note for 6 Emeralds
        event.getTrades().get(2).add((trader, random) -> {
            ItemStack note = new ItemStack(ModItems.TORN_FORGE_NOTE.get());
            ForgeNoteLibrary.NoteEntry entry = ForgeNoteLibrary.randomNote(random);
            note.set(ModDataComponents.FORGE_NOTE.get(), new ForgeNoteData(entry.id()));
            return new MerchantOffer(new ItemCost(Items.EMERALD, 6), note, 3, 10, 0.05f);
        });
    }
}
