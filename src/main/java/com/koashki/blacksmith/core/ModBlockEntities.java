package com.koashki.blacksmith.core;

import com.koashki.blacksmith.blockentity.BlastForgeBlockEntity;
import com.koashki.blacksmith.blockentity.ForgingAnvilBlockEntity;
import com.koashki.blacksmith.blockentity.SynthesisChamberBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BlacksmithMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastForgeBlockEntity>> BLAST_FORGE =
            BLOCK_ENTITIES.register("blast_forge", () -> BlockEntityType.Builder
                    .of(BlastForgeBlockEntity::new, ModBlocks.BLAST_FORGE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgingAnvilBlockEntity>> FORGING_ANVIL =
            BLOCK_ENTITIES.register("forging_anvil", () -> BlockEntityType.Builder
                    .of(ForgingAnvilBlockEntity::new, ModBlocks.FORGING_ANVIL.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SynthesisChamberBlockEntity>> SYNTHESIS_CHAMBER =
            BLOCK_ENTITIES.register("synthesis_chamber", () -> BlockEntityType.Builder
                    .of(SynthesisChamberBlockEntity::new, ModBlocks.SYNTHESIS_CHAMBER.get())
                    .build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
