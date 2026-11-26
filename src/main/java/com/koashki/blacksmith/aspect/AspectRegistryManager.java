package com.koashki.blacksmith.aspect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
public final class AspectRegistryManager {
    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<ResourceLocation, AspectDefinition> ASPECTS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ItemAspectMapping> ITEM_MAPPINGS = new HashMap<>();
    private static final Map<ResourceLocation, AspectReactionDefinition> REACTIONS = new LinkedHashMap<>();

    static {
        registerBuiltinDefaults();
    }

    private AspectRegistryManager() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimpleJsonResourceReloadListener(GSON, "blacksmith/aspects") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager rm, ProfilerFiller profiler) {
                ASPECTS.clear();
                registerBuiltinAspects();
                objects.forEach((key, json) -> AspectDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(err -> BlacksmithMod.LOGGER.warn("Failed to parse aspect {}: {}", key, err))
                        .ifPresent(def -> ASPECTS.put(def.id(), def)));
            }
        });

        event.addListener(new SimpleJsonResourceReloadListener(GSON, "blacksmith/item_aspects") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager rm, ProfilerFiller profiler) {
                ITEM_MAPPINGS.clear();
                registerBuiltinStarterMappings();
                objects.forEach((key, json) -> ItemAspectMapping.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(err -> BlacksmithMod.LOGGER.warn("Failed to parse item aspect mapping {}: {}", key, err))
                        .ifPresent(map -> ITEM_MAPPINGS.put(map.itemId(), map)));
            }
        });

        event.addListener(new SimpleJsonResourceReloadListener(GSON, "blacksmith/reactions") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager rm, ProfilerFiller profiler) {
                REACTIONS.clear();
                registerBuiltinReactions();
                objects.forEach((key, json) -> AspectReactionDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(err -> BlacksmithMod.LOGGER.warn("Failed to parse reaction {}: {}", key, err))
                        .ifPresent(def -> REACTIONS.put(def.id(), def)));
            }
        });
    }

    public static Optional<AspectDefinition> getAspect(ResourceLocation id) {
        return Optional.ofNullable(ASPECTS.get(id));
    }

    public static Collection<AspectDefinition> allAspects() {
        return Collections.unmodifiableCollection(ASPECTS.values());
    }

    public static Collection<AspectReactionDefinition> allReactions() {
        return Collections.unmodifiableCollection(REACTIONS.values());
    }

    /**
     * Universal Material Analyzer:
     * Uses curated datapack mappings first, then deterministically derives Aspects,
     * instability, and color tint from any vanilla or modded ItemStack so nothing is unforgeable.
     */
    public static ItemAspectMapping analyzeItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ItemAspectMapping(BuiltInRegistries.ITEM.getKey(Items.AIR), Map.of(), 0x888888, 0.0f);
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ItemAspectMapping curated = ITEM_MAPPINGS.get(itemId);
        if (curated != null) {
            return curated;
        }

        Map<ResourceLocation, Integer> derived = new LinkedHashMap<>();
        float instability = 0.12f;

        if (stack.getBurnTime(null) > 0) {
            derived.merge(AspectIds.THERMAL, 2, Integer::sum);
            instability += 0.08f;
        }
        if (stack.has(DataComponents.FOOD)) {
            derived.merge(AspectIds.SIPHONING, 2, Integer::sum);
            derived.merge(AspectIds.HARMONIC, 1, Integer::sum);
        }
        if (stack.has(DataComponents.POTION_CONTENTS)) {
            derived.merge(AspectIds.CAUSTIC, 2, Integer::sum);
            derived.merge(AspectIds.VOLATILE, 1, Integer::sum);
            instability += 0.15f;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            float resistance = block.getExplosionResistance();
            if (resistance >= 6.0f) {
                derived.merge(AspectIds.DENSE, 2, Integer::sum);
                derived.merge(AspectIds.KINETIC, 1, Integer::sum);
            } else if (resistance <= 0.6f) {
                derived.merge(AspectIds.FEATHERWEIGHT, 1, Integer::sum);
                derived.merge(AspectIds.BRITTLE, 1, Integer::sum);
            }
            if (block.defaultBlockState().getLightEmission() > 0) {
                derived.merge(AspectIds.RADIANT, 2, Integer::sum);
            }
        }

        Rarity rarity = stack.getRarity();
        if (rarity == Rarity.UNCOMMON) {
            derived.merge(AspectIds.HARMONIC, 1, Integer::sum);
            instability += 0.08f;
        } else if (rarity == Rarity.RARE) {
            derived.merge(AspectIds.SPATIAL, 2, Integer::sum);
            instability += 0.15f;
        } else if (rarity == Rarity.EPIC) {
            derived.merge(AspectIds.EGO, 2, Integer::sum);
            derived.merge(AspectIds.TEMPORAL, 2, Integer::sum);
            instability += 0.25f;
        }

        // Deterministic fallback so literally every item contributes a distinct aspect
        if (derived.isEmpty()) {
            List<AspectDefinition> list = new ArrayList<>(ASPECTS.values());
            if (!list.isEmpty()) {
                int idx = Math.floorMod(itemId.toString().hashCode(), list.size());
                derived.put(list.get(idx).id(), 2);
            }
        }

        int derivedColor = deriveColorFromAspects(derived, itemId);
        return new ItemAspectMapping(itemId, derived, derivedColor, instability);
    }

    private static int deriveColorFromAspects(Map<ResourceLocation, Integer> aspects, ResourceLocation itemId) {
        for (ResourceLocation id : aspects.keySet()) {
            AspectDefinition def = ASPECTS.get(id);
            if (def != null) {
                return def.colorRgb();
            }
        }
        int hash = itemId.toString().hashCode();
        int r = 90 + Math.floorMod(hash, 140);
        int g = 90 + Math.floorMod(hash >> 8, 140);
        int b = 90 + Math.floorMod(hash >> 16, 140);
        return (r << 16) | (g << 8) | b;
    }

    private static void registerBuiltinDefaults() {
        registerBuiltinAspects();
        registerBuiltinStarterMappings();
        registerBuiltinReactions();
    }

    private static void registerBuiltinAspects() {
        // Physical / Elemental
        addAspect(AspectIds.THERMAL, AspectDimension.PHYSICAL, 0xFF6A22, 0.14f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_TARGET, AspectIds.EFFECT_IGNITE);
        addAspect(AspectIds.CRYO, AspectDimension.PHYSICAL, 0x66D9FF, 0.10f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_TARGET, AspectIds.EFFECT_FROST);
        addAspect(AspectIds.VOLTAIC, AspectDimension.PHYSICAL, 0xFFE14A, 0.18f, AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_CHAIN_ARC, AspectIds.EFFECT_LIGHTNING);
        addAspect(AspectIds.KINETIC, AspectDimension.PHYSICAL, 0xD4B28C, 0.10f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_LAUNCH);
        addAspect(AspectIds.RESONANT, AspectDimension.PHYSICAL, 0xC68CFF, 0.14f, AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_SONIC_STUN);
        addAspect(AspectIds.RADIANT, AspectDimension.PHYSICAL, 0xFFF4B8, 0.08f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_SELF_BUFF, AspectIds.EFFECT_HASTE);
        addAspect(AspectIds.ABYSSAL, AspectDimension.PHYSICAL, 0x4A2B7C, 0.22f, AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_VOID_PULL);
        addAspect(AspectIds.CAUSTIC, AspectDimension.PHYSICAL, 0x68CC33, 0.14f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_TARGET, AspectIds.EFFECT_POISON);

        // Structural
        addAspect(AspectIds.RAZOR, AspectDimension.STRUCTURAL, 0xE2E8F0, 0.06f, AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_TARGET, AspectIds.EFFECT_ARMOR_SHRED);
        addAspect(AspectIds.DENSE, AspectDimension.STRUCTURAL, 0x6C7A89, 0.08f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_LAUNCH);
        addAspect(AspectIds.ELASTIC, AspectDimension.STRUCTURAL, 0x7AE56B, 0.10f, AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_LAUNCH);
        addAspect(AspectIds.BRITTLE, AspectDimension.STRUCTURAL, 0xB8E6FF, 0.16f, AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_ARMOR_SHRED);
        addAspect(AspectIds.FEATHERWEIGHT, AspectDimension.STRUCTURAL, 0xF5F5FA, 0.04f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_SELF_BUFF, AspectIds.EFFECT_HASTE);
        addAspect(AspectIds.ADAPTIVE, AspectDimension.STRUCTURAL, 0x4FD1A5, 0.06f, AspectIds.TRIGGER_ON_KILL, AspectIds.DELIVERY_SELF_BUFF, AspectIds.EFFECT_LIFESTEAL);

        // Conceptual
        addAspect(AspectIds.SPATIAL, AspectDimension.CONCEPTUAL, 0x26C6B0, 0.20f, AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_BLINK_DASH, AspectIds.EFFECT_VOID_PULL);
        addAspect(AspectIds.TEMPORAL, AspectDimension.CONCEPTUAL, 0xF2C94C, 0.22f, AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_DELAYED_ECHO, AspectIds.EFFECT_ARMOR_SHRED);
        addAspect(AspectIds.SIPHONING, AspectDimension.CONCEPTUAL, 0xC92A49, 0.16f, AspectIds.TRIGGER_ON_KILL, AspectIds.DELIVERY_SELF_BUFF, AspectIds.EFFECT_LIFESTEAL);
        addAspect(AspectIds.VOLATILE, AspectDimension.CONCEPTUAL, 0xFF4522, 0.28f, AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_EXPLODE);
        addAspect(AspectIds.HARMONIC, AspectDimension.CONCEPTUAL, 0x88E5FF, 0.05f, AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_SELF_BUFF, AspectIds.EFFECT_HASTE);
        addAspect(AspectIds.EXECUTION, AspectDimension.CONCEPTUAL, 0x991B1B, 0.16f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_TARGET, AspectIds.EFFECT_ARMOR_SHRED);
        addAspect(AspectIds.PHASING, AspectDimension.CONCEPTUAL, 0x9F8CFF, 0.18f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_TARGET, AspectIds.EFFECT_ARMOR_SHRED);
        addAspect(AspectIds.EGO, AspectDimension.CONCEPTUAL, 0xFF9F1C, 0.20f, AspectIds.TRIGGER_ON_KILL, AspectIds.DELIVERY_SELF_BUFF, AspectIds.EFFECT_HASTE);

        // Delivery / Behavior
        addAspect(AspectIds.STORING, AspectDimension.DELIVERY, 0xE63946, 0.12f, AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_LIGHTNING);
        addAspect(AspectIds.TETHERING, AspectDimension.DELIVERY, 0xD8D8D8, 0.10f, AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_VOID_PULL);
        addAspect(AspectIds.SPLITTING, AspectDimension.DELIVERY, 0xA8DADC, 0.14f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_CHAIN_ARC, AspectIds.EFFECT_ARMOR_SHRED);
        addAspect(AspectIds.RETURNING, AspectDimension.DELIVERY, 0x457B9D, 0.10f, AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_BLINK_DASH, AspectIds.EFFECT_LAUNCH);
        addAspect(AspectIds.SEEKING, AspectDimension.DELIVERY, 0x52B788, 0.12f, AspectIds.TRIGGER_ON_HIT, AspectIds.DELIVERY_CHAIN_ARC, AspectIds.EFFECT_VOID_PULL);
    }

    private static void registerBuiltinStarterMappings() {
        // Lean starter pool for base metals + iconic vanilla wildcard materials (expandable via JSON datapacks)
        addMapping(Items.IRON_INGOT, Map.of(AspectIds.DENSE, 2, AspectIds.RAZOR, 1), 0xD8D8DC, 0.04f);
        addMapping(Items.GOLD_INGOT, Map.of(AspectIds.HARMONIC, 2, AspectIds.RADIANT, 2, AspectIds.TEMPORAL, 1), 0xFFD24A, 0.08f);
        addMapping(Items.COPPER_INGOT, Map.of(AspectIds.VOLTAIC, 3, AspectIds.STORING, 1), 0xE07A52, 0.12f);
        addMapping(Items.REDSTONE, Map.of(AspectIds.VOLTAIC, 2, AspectIds.STORING, 2), 0xE52521, 0.15f);
        addMapping(Items.SLIME_BALL, Map.of(AspectIds.ELASTIC, 3, AspectIds.KINETIC, 1), 0x7AE56B, 0.10f);
        addMapping(Items.GUNPOWDER, Map.of(AspectIds.VOLATILE, 3, AspectIds.THERMAL, 1), 0x626262, 0.25f);
        addMapping(Items.FEATHER, Map.of(AspectIds.FEATHERWEIGHT, 3, AspectIds.RETURNING, 1), 0xF5F5FA, 0.04f);
        addMapping(Items.FLINT, Map.of(AspectIds.RAZOR, 3, AspectIds.BRITTLE, 1), 0x4C4C52, 0.08f);
        addMapping(Items.BELL, Map.of(AspectIds.RESONANT, 4, AspectIds.HARMONIC, 2), 0xF7C948, 0.12f);
        addMapping(Items.AMETHYST_SHARD, Map.of(AspectIds.RESONANT, 3, AspectIds.BRITTLE, 2), 0xB86BFF, 0.14f);
        addMapping(Items.CLOCK, Map.of(AspectIds.TEMPORAL, 4, AspectIds.HARMONIC, 1), 0xF2C94C, 0.22f);
        addMapping(Items.ENDER_PEARL, Map.of(AspectIds.SPATIAL, 4, AspectIds.PHASING, 2), 0x1E9E89, 0.22f);
        addMapping(Items.SPIDER_EYE, Map.of(AspectIds.CAUSTIC, 3, AspectIds.SIPHONING, 1), 0x992244, 0.16f);
        addMapping(Items.ROTTEN_FLESH, Map.of(AspectIds.SIPHONING, 2, AspectIds.VOLATILE, 1), 0x8A5A3C, 0.18f);
        addMapping(Items.BLAZE_POWDER, Map.of(AspectIds.THERMAL, 4, AspectIds.VOLATILE, 2), 0xFF7B1C, 0.24f);
        addMapping(Items.BONE, Map.of(AspectIds.EXECUTION, 2, AspectIds.RAZOR, 2), 0xEDEBD6, 0.08f);
    }

    private static void registerBuiltinReactions() {
        addReaction(AspectIds.REACTION_PHANTOM_CUT, AspectIds.TEMPORAL, AspectIds.RAZOR,
                AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_DELAYED_ECHO, AspectIds.EFFECT_ARMOR_SHRED, 1.45f, 0xF2C94C);
        addReaction(AspectIds.REACTION_THUNDERSTEP, AspectIds.VOLTAIC, AspectIds.SPATIAL,
                AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_BLINK_DASH, AspectIds.EFFECT_LIGHTNING, 1.50f, 0x5CE1E6);
        addReaction(AspectIds.REACTION_SHATTERWAVE, AspectIds.RESONANT, AspectIds.BRITTLE,
                AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_SONIC_STUN, 1.40f, 0xC68CFF);
        addReaction(AspectIds.REACTION_SEISMIC_REBOUND, AspectIds.DENSE, AspectIds.ELASTIC,
                AspectIds.TRIGGER_ON_RIGHT_CLICK, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_LAUNCH, 1.35f, 0x7AE56B);
        addReaction(AspectIds.REACTION_BLOOD_BURST, AspectIds.SIPHONING, AspectIds.VOLATILE,
                AspectIds.TRIGGER_ON_KILL, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_LIFESTEAL, 1.50f, 0xD9264A);
        addReaction(AspectIds.REACTION_FORGED_DETONATION, AspectIds.THERMAL, AspectIds.KINETIC,
                AspectIds.TRIGGER_ON_CRIT, AspectIds.DELIVERY_AOE_SHOCKWAVE, AspectIds.EFFECT_EXPLODE, 1.45f, 0xFF5A1F);
    }

    private static void addAspect(ResourceLocation id, AspectDimension dim, int color, float instability,
                                  ResourceLocation trigger, ResourceLocation delivery, ResourceLocation effect) {
        ASPECTS.put(id, new AspectDefinition(id, dim, color, instability, trigger, delivery, effect));
    }

    private static void addMapping(Item item, Map<ResourceLocation, Integer> aspects, int color, float instability) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        ITEM_MAPPINGS.put(id, new ItemAspectMapping(id, aspects, color, instability));
    }

    private static void addReaction(ResourceLocation id, ResourceLocation a, ResourceLocation b,
                                    ResourceLocation trigger, ResourceLocation delivery, ResourceLocation effect,
                                    float powerMult, int color) {
        REACTIONS.put(id, new AspectReactionDefinition(id, a, b, trigger, delivery, effect, powerMult, color));
    }
}
