package com.koashki.blacksmith.core;

import com.koashki.blacksmith.menu.BlastForgeMenu;
import com.koashki.blacksmith.menu.ForgingAnvilMenu;
import com.koashki.blacksmith.menu.SynthesisChamberMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, BlacksmithMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<BlastForgeMenu>> BLAST_FORGE =
            MENUS.register("blast_forge", () -> IMenuTypeExtension.create(BlastForgeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ForgingAnvilMenu>> FORGING_ANVIL =
            MENUS.register("forging_anvil", () -> IMenuTypeExtension.create(ForgingAnvilMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SynthesisChamberMenu>> SYNTHESIS_CHAMBER =
            MENUS.register("synthesis_chamber", () -> IMenuTypeExtension.create(SynthesisChamberMenu::new));

    private ModMenus() {}

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
