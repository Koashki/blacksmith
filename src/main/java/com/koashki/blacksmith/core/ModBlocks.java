package com.koashki.blacksmith.core;

import com.koashki.blacksmith.block.BlastForgeBlock;
import com.koashki.blacksmith.block.ForgingAnvilBlock;
import com.koashki.blacksmith.block.SynthesisChamberBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BlacksmithMod.MOD_ID);

    public static final DeferredBlock<BlastForgeBlock> BLAST_FORGE = registerBlock("blast_forge",
            () -> new BlastForgeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(BlastForgeBlock.LIT) ? 13 : 0)
                    .sound(SoundType.STONE)));

    public static final DeferredBlock<ForgingAnvilBlock> FORGING_ANVIL = registerBlock("forging_anvil",
            () -> new ForgingAnvilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ANVIL)));

    public static final DeferredBlock<SynthesisChamberBlock> SYNTHESIS_CHAMBER = registerBlock("synthesis_chamber",
            () -> new SynthesisChamberBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(4.5f, 12.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)
                    .sound(SoundType.AMETHYST)));

    private ModBlocks() {}

    private static <T extends net.minecraft.world.level.block.Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
