package com.koashki.blacksmith.loot;

import com.koashki.blacksmith.aspect.ForgeNoteLibrary;
import com.koashki.blacksmith.component.ForgeNoteData;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.core.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddForgeNoteLootModifier extends LootModifier {
    public static final MapCodec<AddForgeNoteLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance)
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .apply(instance, AddForgeNoteLootModifier::new));

    private final float chance;

    public AddForgeNoteLootModifier(LootItemCondition[] conditionsIn, float chance) {
        super(conditionsIn);
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < chance) {
            ItemStack note = new ItemStack(ModItems.TORN_FORGE_NOTE.get());
            ForgeNoteLibrary.NoteEntry entry = ForgeNoteLibrary.randomNote(context.getRandom());
            note.set(ModDataComponents.FORGE_NOTE.get(), new ForgeNoteData(entry.id()));
            generatedLoot.add(note);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
