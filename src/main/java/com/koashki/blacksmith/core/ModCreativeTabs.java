package com.koashki.blacksmith.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BlacksmithMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLACKSMITH_TAB =
            CREATIVE_MODE_TABS.register("blacksmith_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.blacksmith"))
                    .icon(() -> ModBlocks.FORGING_ANVIL.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.BLAST_FORGE.get());
                        output.accept(ModBlocks.FORGING_ANVIL.get());
                        output.accept(ModBlocks.SYNTHESIS_CHAMBER.get());
                        output.accept(ModItems.STONE_FORGING_HAMMER.get());
                        output.accept(ModItems.IRON_FORGING_HAMMER.get());
                        output.accept(ModItems.GOLD_FORGING_HAMMER.get());
                        output.accept(ModItems.MOLTEN_ALLOY.get());
                        output.accept(ModItems.REFINING_DUST.get());
                        output.accept(ModItems.ASPECT_ESSENCE.get());
                        output.accept(ModItems.FORGED_SWORD.get());
                        output.accept(ModItems.FORGED_GREATSWORD.get());
                        output.accept(ModItems.FORGED_DAGGER.get());
                        output.accept(ModItems.FORGED_SPEAR.get());
                        output.accept(ModItems.FORGED_WAR_HAMMER.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
