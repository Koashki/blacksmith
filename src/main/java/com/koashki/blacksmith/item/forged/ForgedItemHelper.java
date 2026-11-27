package com.koashki.blacksmith.item.forged;

import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.core.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class ForgedItemHelper {
    private ForgedItemHelper() {}

    public static Optional<Component> gradedDisplayName(ItemStack stack, ForgedArchetype fallbackArchetype) {
        ItemGradeData grade = stack.get(ModDataComponents.ITEM_GRADE.get());
        if (grade == null) {
            return Optional.empty();
        }
        if (grade.customName().isPresent()) {
            return Optional.of(Component.literal(grade.customName().get()).withColor(grade.tier().colorRgb()));
        }
        ForgedCompositionData comp = stack.get(ModDataComponents.FORGED_COMPOSITION.get());
        String formName = comp != null ? ForgedArchetype.byId(comp.archetypeId()).displayName() : fallbackArchetype.displayName();
        return Optional.of(grade.tier().displayName()
                .append(Component.literal(" " + formName).withColor(grade.tier().colorRgb())));
    }

    public static boolean isMasterpieceFoil(ItemStack stack) {
        ItemGradeData grade = stack.get(ModDataComponents.ITEM_GRADE.get());
        return grade != null && grade.tier().isMasterpiece();
    }
}
