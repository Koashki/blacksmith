package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DiscoveredBlueprintsData(
        List<ResourceLocation> discoveredReactions,
        List<String> masterpieceHistory
) {
    public static final DiscoveredBlueprintsData EMPTY = new DiscoveredBlueprintsData(List.of(), List.of());

    public static final Codec<DiscoveredBlueprintsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("discovered_reactions").forGetter(DiscoveredBlueprintsData::discoveredReactions),
            Codec.STRING.listOf().fieldOf("masterpiece_history").forGetter(DiscoveredBlueprintsData::masterpieceHistory)
    ).apply(instance, DiscoveredBlueprintsData::new));

    public DiscoveredBlueprintsData {
        discoveredReactions = List.copyOf(discoveredReactions);
        masterpieceHistory = List.copyOf(masterpieceHistory);
    }

    public DiscoveredBlueprintsData withReaction(ResourceLocation reactionId) {
        if (discoveredReactions.contains(reactionId)) {
            return this;
        }
        List<ResourceLocation> updated = new ArrayList<>(discoveredReactions);
        updated.add(reactionId);
        return new DiscoveredBlueprintsData(updated, masterpieceHistory);
    }

    public DiscoveredBlueprintsData withMasterpiece(String entry) {
        List<String> updated = new ArrayList<>(masterpieceHistory);
        updated.add(entry);
        return new DiscoveredBlueprintsData(discoveredReactions, updated);
    }
}
