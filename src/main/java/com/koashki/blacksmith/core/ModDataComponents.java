package com.koashki.blacksmith.core;

import com.koashki.blacksmith.component.ComposedAbilitiesData;
import com.koashki.blacksmith.component.ForgeNoteData;
import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.ForgingHeatData;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.component.SynthesisStarData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BlacksmithMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemGradeData>> ITEM_GRADE =
            DATA_COMPONENTS.registerComponentType("item_grade", builder -> builder
                    .persistent(ItemGradeData.CODEC)
                    .networkSynchronized(ItemGradeData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ForgingHeatData>> FORGING_HEAT =
            DATA_COMPONENTS.registerComponentType("forging_heat", builder -> builder
                    .persistent(ForgingHeatData.CODEC)
                    .networkSynchronized(ForgingHeatData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SynthesisStarData>> SYNTHESIS_STAR =
            DATA_COMPONENTS.registerComponentType("synthesis_star", builder -> builder
                    .persistent(SynthesisStarData.CODEC)
                    .networkSynchronized(SynthesisStarData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ForgedCompositionData>> FORGED_COMPOSITION =
            DATA_COMPONENTS.registerComponentType("forged_composition", builder -> builder
                    .persistent(ForgedCompositionData.CODEC)
                    .networkSynchronized(ForgedCompositionData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ComposedAbilitiesData>> COMPOSED_ABILITIES =
            DATA_COMPONENTS.registerComponentType("composed_abilities", builder -> builder
                    .persistent(ComposedAbilitiesData.CODEC)
                    .networkSynchronized(ComposedAbilitiesData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ForgeNoteData>> FORGE_NOTE =
            DATA_COMPONENTS.registerComponentType("forge_note", builder -> builder
                    .persistent(ForgeNoteData.CODEC)
                    .networkSynchronized(ForgeNoteData.STREAM_CODEC));

    private ModDataComponents() {}

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
