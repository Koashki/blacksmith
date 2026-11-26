package com.koashki.blacksmith.aspect;

import com.koashki.blacksmith.component.ComposedAbilitiesData;
import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.GradeTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SkillComposer {
    private SkillComposer() {}

    public static ComposedAbilitiesData compose(
            ForgedCompositionData composition,
            GradeTier gradeTier,
            float stabilizationRatio,
            RandomSource random
    ) {
        int maxSlots = gradeTier.maxAbilities();
        if (maxSlots <= 0 || composition.aspectWeights().isEmpty()) {
            return ComposedAbilitiesData.EMPTY;
        }

        List<ComposedAbilitiesData.ComposedAbility> abilities = new ArrayList<>();
        Set<ResourceLocation> present = composition.aspectWeights().keySet();

        // 1. Prioritize Emergent Aspect Reactions first
        for (AspectReactionDefinition reaction : AspectRegistryManager.allReactions()) {
            if (abilities.size() >= maxSlots) {
                break;
            }
            if (reaction.matches(present)) {
                int combinedWeight = composition.aspectWeights().getOrDefault(reaction.aspectA(), 1)
                        + composition.aspectWeights().getOrDefault(reaction.aspectB(), 1);
                float power = (0.85f + 0.15f * Math.min(8, combinedWeight)) * reaction.powerMultiplier() * gradeTier.statMultiplier();
                abilities.add(new ComposedAbilitiesData.ComposedAbility(
                        reaction.trigger(),
                        reaction.delivery(),
                        reaction.effect(),
                        power,
                        Optional.of(reaction.id())
                ));
            }
        }

        // 2. Fill remaining ability slots from highest-weight aspects
        List<Map.Entry<ResourceLocation, Integer>> sorted = new ArrayList<>(composition.aspectWeights().entrySet());
        sorted.sort(Comparator.comparingInt((Map.Entry<ResourceLocation, Integer> e) -> e.getValue()).reversed());

        for (Map.Entry<ResourceLocation, Integer> entry : sorted) {
            if (abilities.size() >= maxSlots) {
                break;
            }
            Optional<AspectDefinition> optDef = AspectRegistryManager.getAspect(entry.getKey());
            if (optDef.isEmpty()) {
                continue;
            }
            AspectDefinition def = optDef.get();
            boolean duplicateEffect = abilities.stream().anyMatch(a -> a.effect().equals(def.defaultEffect()) && a.trigger().equals(def.defaultTrigger()));
            if (duplicateEffect) {
                continue;
            }
            float power = (0.75f + 0.15f * Math.min(6, entry.getValue())) * gradeTier.statMultiplier();
            abilities.add(new ComposedAbilitiesData.ComposedAbility(
                    def.defaultTrigger(),
                    def.defaultDelivery(),
                    def.defaultEffect(),
                    power,
                    Optional.empty()
            ));
        }

        // 3. Roll Instability Quirks if the forge remained unstable
        List<ResourceLocation> quirks = new ArrayList<>();
        float residualInstability = Math.max(0.0f, composition.instability() * (1.0f - Math.clamp(stabilizationRatio, 0.0f, 1.0f)));
        if (residualInstability > 0.35f && random.nextFloat() < residualInstability) {
            ResourceLocation[] pool = {AspectIds.QUIRK_RECOIL, AspectIds.QUIRK_HUNGER_DRAIN, AspectIds.QUIRK_OVERHEAT};
            quirks.add(pool[random.nextInt(pool.length)]);
        }

        return new ComposedAbilitiesData(abilities, quirks);
    }
}
