package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DiscoveredBlueprintsData(
        List<ResourceLocation> discoveredItems,
        List<ResourceLocation> discoveredReactions,
        List<ResourceLocation> discoveredNotes,
        List<String> masterpieceHistory
) {
    public static final DiscoveredBlueprintsData EMPTY =
            new DiscoveredBlueprintsData(List.of(), List.of(), List.of(), List.of());

    public static final Codec<DiscoveredBlueprintsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("discovered_items", List.of()).forGetter(DiscoveredBlueprintsData::discoveredItems),
            ResourceLocation.CODEC.listOf().optionalFieldOf("discovered_reactions", List.of()).forGetter(DiscoveredBlueprintsData::discoveredReactions),
            ResourceLocation.CODEC.listOf().optionalFieldOf("discovered_notes", List.of()).forGetter(DiscoveredBlueprintsData::discoveredNotes),
            Codec.STRING.listOf().optionalFieldOf("masterpiece_history", List.of()).forGetter(DiscoveredBlueprintsData::masterpieceHistory)
    ).apply(instance, DiscoveredBlueprintsData::new));

    public static final StreamCodec<ByteBuf, DiscoveredBlueprintsData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), DiscoveredBlueprintsData::discoveredItems,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), DiscoveredBlueprintsData::discoveredReactions,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), DiscoveredBlueprintsData::discoveredNotes,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DiscoveredBlueprintsData::masterpieceHistory,
            DiscoveredBlueprintsData::new
    );

    public DiscoveredBlueprintsData {
        discoveredItems = List.copyOf(discoveredItems);
        discoveredReactions = List.copyOf(discoveredReactions);
        discoveredNotes = List.copyOf(discoveredNotes);
        masterpieceHistory = List.copyOf(masterpieceHistory);
    }

    public boolean hasItem(ResourceLocation itemId) {
        return discoveredItems.contains(itemId);
    }

    public boolean hasReaction(ResourceLocation reactionId) {
        return discoveredReactions.contains(reactionId);
    }

    public boolean hasNote(ResourceLocation noteId) {
        return discoveredNotes.contains(noteId);
    }

    public DiscoveredBlueprintsData withItem(ResourceLocation itemId) {
        if (discoveredItems.contains(itemId)) {
            return this;
        }
        List<ResourceLocation> updated = new ArrayList<>(discoveredItems);
        updated.add(itemId);
        return new DiscoveredBlueprintsData(updated, discoveredReactions, discoveredNotes, masterpieceHistory);
    }

    public DiscoveredBlueprintsData withReaction(ResourceLocation reactionId) {
        if (discoveredReactions.contains(reactionId)) {
            return this;
        }
        List<ResourceLocation> updated = new ArrayList<>(discoveredReactions);
        updated.add(reactionId);
        return new DiscoveredBlueprintsData(discoveredItems, updated, discoveredNotes, masterpieceHistory);
    }

    public DiscoveredBlueprintsData withNote(ResourceLocation noteId) {
        if (discoveredNotes.contains(noteId)) {
            return this;
        }
        List<ResourceLocation> updated = new ArrayList<>(discoveredNotes);
        updated.add(noteId);
        return new DiscoveredBlueprintsData(discoveredItems, discoveredReactions, updated, masterpieceHistory);
    }

    public DiscoveredBlueprintsData withMasterpiece(String entry) {
        List<String> updated = new ArrayList<>(masterpieceHistory);
        updated.add(entry);
        return new DiscoveredBlueprintsData(discoveredItems, discoveredReactions, discoveredNotes, updated);
    }
}
