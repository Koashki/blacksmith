package com.koashki.blacksmith.core;

import com.koashki.blacksmith.datagen.ModDataGenerators;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(BlacksmithMod.MOD_ID)
public class BlacksmithMod {
    public static final String MOD_ID = "blacksmith";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BlacksmithMod(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(ModDataGenerators::gatherData);

        modContainer.registerConfig(ModConfig.Type.SERVER, ModConfigs.SERVER_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ModConfigs.CLIENT_SPEC);

        LOGGER.info("Blacksmith mod initialized (NeoForge 1.21.1)");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
