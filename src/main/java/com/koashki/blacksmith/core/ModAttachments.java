package com.koashki.blacksmith.core;

import com.koashki.blacksmith.component.BlacksmithProficiencyData;
import com.koashki.blacksmith.component.DiscoveredBlueprintsData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BlacksmithMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BlacksmithProficiencyData>> PROFICIENCY =
            ATTACHMENTS.register("proficiency", () -> AttachmentType.builder(() -> BlacksmithProficiencyData.INITIAL)
                    .serialize(BlacksmithProficiencyData.CODEC)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DiscoveredBlueprintsData>> DISCOVERED_BLUEPRINTS =
            ATTACHMENTS.register("discovered_blueprints", () -> AttachmentType.builder(() -> DiscoveredBlueprintsData.EMPTY)
                    .serialize(DiscoveredBlueprintsData.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {}

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
